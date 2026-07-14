package cn.edu.scnu.controller;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.MapObj;
import cn.edu.scnu.element.Play;
import cn.edu.scnu.element.RoleObj;
import cn.edu.scnu.element.effect.HitEffect;
import cn.edu.scnu.element.weapon.Bullet;
import cn.edu.scnu.element.weapon.EnemyBullet;
import cn.edu.scnu.element.weapon.Grenade;
import cn.edu.scnu.manager.Camera;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

/**
 * @说明 游戏的主线程，用于控制游戏加载，游戏关卡，游戏运行时自动化
 *      游戏判定;游戏地图切换 资源释放和重新读取。....
 * @继承 使用继承的方式实现多线程(一般建议使用接口实现)
 */

public class GameThread extends Thread {
    //游戏当前所处的流程状态
    public enum GameState {
        MAIN_MENU, //主菜单
        LEVEL_SELECT, //关卡选择
        CONTROLS, //操作说明
        START, //关卡已加载，等待开始
        RUNNING, //关卡正常运行
        PAUSED, //暂停后的游戏设置界面
        FAILED //玩家死亡后的失败界面
    }

    private ElementManager em;
    private Camera camera; //游戏线程和绘制面板共用的摄像机
    private volatile GameState gameState=GameState.MAIN_MENU; //当前游戏流程状态
    private volatile boolean loadRequested; //是否需要加载当前选择的关卡
    private volatile boolean startAfterLoad; //加载完成后是否直接进入运行状态
    private volatile int currentLevel=1; //当前选择的逻辑关卡编号

    public GameThread() {
        em = ElementManager.getManager();
    }

    //注入当前游戏使用的唯一摄像机
    public void setCamera(Camera camera) {
        this.camera=camera;
    }

    private volatile boolean running=true; //整个游戏是否运行
    private volatile boolean levelRunning=true; //当前关卡是否运行

    //等待关卡加载请求，并在加载完成后运行当前关卡
    @Override
    public void run() {
        while(running) {
            if(!loadRequested) {
                try {
                    Thread.sleep(10);
                }catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            loadRequested=false;
            levelRunning=true;
            gameLoad(); //加载当前关卡
            if(startAfterLoad) {
                gameState=GameState.RUNNING;
            }else {
                gameState=GameState.START;
            }
            startAfterLoad=false;
            gameRun();  //运行当前关卡
            gameOver(); //清理当前关卡
        }
    }

    //根据当前关卡清空旧对象并统一加载地图、摄像机范围和玩家
    private void gameLoad() {
        em.clearAll();
        camera.reset();

        String mapKey="map.level"+currentLevel;
        String groundKey=mapKey+".groundSourceY";
        int mapX=0;
        int mapY=0;
        ImageIcon mapIcon=GameLoad.getImage(mapKey);
        int mapScale=GameLoad.getInt("map.scale");
        int mapWidth=mapIcon.getIconWidth()*mapScale;
        int mapHeight=mapIcon.getIconHeight()*mapScale;
        int groundSourceY=GameLoad.getInt(groundKey);
        //配置保存的是原图地面行，乘地图倍率后得到世界坐标中的地面位置
        int groundY=mapY+groundSourceY*mapScale;
        ElementObj mapTemplate=GameLoad.getObj("map");
        MapObj map=(MapObj)mapTemplate.createElement(
                mapKey+","+mapX+","+mapY+","+mapWidth+","+mapHeight+","+groundY);
        em.addElement(map,GameElement.MAPS);
        camera.setWorldSize(map.getW(),map.getH());

        int playerHeight=GameLoad.getImages("player.stand").get(0).getIconHeight();
        int playerY=map.getGroundY()-playerHeight;
        GameLoad.loadPlay("100,"+playerY+",100,10");
        Play play=(Play)em.getElementByKey(GameElement.PLAY).get(0);
        play.setWorldWidth(map.getW());
        play.placeOnGround(map.getGroundY());
        loadEnemies(map);

    }

    //根据当前关卡配置创建普通敌人并与地图地面对齐
    private void loadEnemies(MapObj map) {
        int count=GameLoad.getInt("level"+currentLevel+".enemy.count");
        for(int i=1;i<=count;i++) {
            String value=GameLoad.getString(
                    "level"+currentLevel+".enemy."+i);
            String[] data=value.split("\\|",2);
            if(data.length!=2) {
                throw new IllegalArgumentException("敌人配置格式应为 对象键|创建参数");
            }
            ElementObj template=GameLoad.getObj(data[0].trim());
            ElementObj enemy=template.createElement(data[1].trim());
            enemy.setY(map.getGroundY()-enemy.getH());
            em.addElement(enemy,GameElement.ENEMY);
        }
    }

    /**
     * @说明 游戏进行时
     */
    private void gameRun() {
        long gameTime=0L;
        int sleep=GameLoad.getInt("game.logicInterval");

        while(running && levelRunning) {
            if(gameState!=GameState.RUNNING) { //非运行状态只保持绘制线程刷新
                try {Thread.sleep(sleep);
                }catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            Map<GameElement,List<ElementObj>> all= em.getGameElements();
            List<ElementObj> enemys=em.getElementByKey(GameElement.ENEMY);
            List<ElementObj> bosses=em.getElementByKey(GameElement.BOSS);
            List<ElementObj> plays=em.getElementByKey(GameElement.PLAY);
            List<ElementObj> playFiles=em.getElementByKey(GameElement.PLAYFILE);
            List<ElementObj> enemyFiles=em.getElementByKey(GameElement.ENEMYFILE);

            moveAndUpdate(all,gameTime);
            updateFailedState(plays);
            if(gameState!=GameState.RUNNING) {
                gameTime++;
                continue;
            }
            updateCamera(plays);

            playerProjectilePk(enemys,playFiles);   //玩家发射物攻击敌人
            playerProjectilePk(bosses,playFiles);   //玩家发射物攻击Boss
            applyGrenadeBlasts(playFiles,enemys,bosses);
            enemyProjectilePk(plays,enemyFiles);   //敌方发射物攻击玩家

            gameTime++;

            try {
                Thread.sleep(sleep);
            }catch (InterruptedException e) {
                return;
            }
        }
    }

    //玩家死亡动画结束并被移除后进入失败状态
    private void updateFailedState(List<ElementObj> plays) {
        if(plays.isEmpty()) {
            gameState=GameState.FAILED;
        }
    }

    //玩家完成本帧移动后更新摄像机横向位置
    private void updateCamera(List<ElementObj> plays) {
        if(plays.isEmpty()) {
            return;
        }
        ElementObj play=plays.get(0);
        camera.follow(play.getX(),play.getW());
    }

    /**
     * 游戏元素自动化方法
     */
    public void moveAndUpdate(Map<GameElement, List<ElementObj>> all,long gameTime) {
        for(GameElement ge:GameElement.values()){
            List<ElementObj> list=all.get(ge);
            //编写这样直接操作集合数据的代码建议不要使用迭代器
            for (int i=list.size()-1;i>=0;i--){
                ElementObj obj = list.get(i);
                if (!obj.isLive()) { //如果死亡
                    //启动一个死亡方法，方法中可以做很多事情，如死亡动画  掉装备
                    obj.die();
                    list.remove(i);
                    continue;
                }
                obj.model(gameTime);
            }
        }
    }

    //判断角色是否仍可接受发射物碰撞
    private boolean canReceiveProjectile(ElementObj target) {
        if(!target.isLive()) {
            return false;
        }
        if(target instanceof RoleObj) {
            return ((RoleObj)target).getHp()>0;
        }
        return true;
    }

    //处理玩家发射物与敌方目标的碰撞
    private void playerProjectilePk(List<ElementObj> targets,
                                    List<ElementObj> projectiles) {
        for(int i=0;i<targets.size();i++) {
            ElementObj target=targets.get(i);
            if(!canReceiveProjectile(target)) {
                continue;
            }
            for(int j=0;j<projectiles.size();j++) {
                ElementObj projectile=projectiles.get(j);
                if(!projectile.isLive()) {
                    continue;
                }
                if(target.pk(projectile)) {
                    if(projectile instanceof Grenade) {
                        ((Grenade)projectile).triggerExplosion();
                    }else {
                        target.hurt(projectile.getAttack());
                        projectile.setLive(false);
                        if(projectile instanceof Bullet) {
                            addHitEffect(target,projectile);
                        }
                    }
                    break;
                }
            }
        }
    }

    //处理敌方发射物与玩家的碰撞
    private void enemyProjectilePk(List<ElementObj> players,
                                   List<ElementObj> projectiles) {
        for(int i=0;i<players.size();i++) {
            ElementObj player=players.get(i);
            if(!canReceiveProjectile(player)) {
                continue;
            }
            for(int j=0;j<projectiles.size();j++) {
                ElementObj projectile=projectiles.get(j);
                if(!projectile.isLive()) {
                    continue;
                }
                if(player.pk(projectile)) {
                    player.hurt(projectile.getAttack());
                    projectile.setLive(false);
                    if(projectile instanceof EnemyBullet) {
                        addHitEffect(player,projectile);
                    }
                    break;
                }
            }
        }
    }

    //在两个碰撞矩形的交集中心生成普通命中特效
    private void addHitEffect(ElementObj target,ElementObj projectile) {
        Rectangle intersection=target.getRectangle()
                .intersection(projectile.getRectangle());
        int centerX=intersection.x+intersection.width/2;
        int centerY=intersection.y+intersection.height/2;
        em.addElement(new HitEffect(centerX,centerY),GameElement.EFFECT);
    }

    //结算本帧新发生的手雷爆炸范围伤害
    private void applyGrenadeBlasts(List<ElementObj> projectiles,
                                    List<ElementObj> enemies,
                                    List<ElementObj> bosses) {
        for(ElementObj projectile:projectiles) {
            if(!(projectile instanceof Grenade)) {
                continue;
            }
            Grenade grenade=(Grenade)projectile;
            if(!grenade.claimBlastDamage()) {
                continue;
            }
            applyGrenadeBlast(grenade,enemies);
            applyGrenadeBlast(grenade,bosses);
        }
    }

    //对手雷爆炸半径内的每个存活目标造成一次范围伤害
    private void applyGrenadeBlast(Grenade grenade,List<ElementObj> targets) {
        int grenadeX=grenade.getX()+grenade.getW()/2;
        int grenadeY=grenade.getY()+grenade.getH()/2;
        int radius=grenade.getBlastRadius();
        long radiusSquared=(long)radius*radius;
        for(ElementObj target:targets) {
            if(!target.isLive()) {
                continue;
            }
            if(target instanceof RoleObj && ((RoleObj)target).getHp()<=0) {
                continue;
            }
            Rectangle rectangle=target.getRectangle();
            int targetX=rectangle.x+rectangle.width/2;
            int targetY=rectangle.y+rectangle.height/2;
            long dx=targetX-grenadeX;
            long dy=targetY-grenadeY;
            if(dx*dx+dy*dy<=radiusSquared) {
                target.hurt(grenade.getBlastDamage());
            }
        }
    }

    //当前关卡退出后统一清理世界对象和摄像机
    private void gameOver() {
        em.clearAll();
        camera.reset();
    }

    //运行状态下暂停当前游戏
    public void pauseGame() {
        if(gameState==GameState.RUNNING) {
            gameState=GameState.PAUSED;
        }
    }

    //暂停状态下继续当前游戏
    public void continueGame() {
        if(gameState==GameState.PAUSED) {
            gameState=GameState.RUNNING;
        }
    }

    //获取当前游戏流程状态
    public GameState getGameState() {
        return gameState;
    }

    //获取当前逻辑关卡编号
    public int getCurrentLevel() {
        return currentLevel;
    }

    //开始已经加载完成的当前关卡
    public void startGame() {
        if(gameState==GameState.START) {
            gameState=GameState.RUNNING;
        }
    }

    //从主菜单进入关卡选择界面
    public void openLevelSelect() {
        if(gameState==GameState.MAIN_MENU) {
            gameState=GameState.LEVEL_SELECT;
        }
    }

    //从主菜单进入操作说明界面
    public void openControls() {
        if(gameState==GameState.MAIN_MENU) {
            gameState=GameState.CONTROLS;
        }
    }

    //从子界面返回主菜单
    public void backToMainMenu() {
        if(gameState==GameState.LEVEL_SELECT
                || gameState==GameState.CONTROLS) {
            gameState=GameState.MAIN_MENU;
        }
    }

    //根据当前流程状态处理 Esc 返回行为
    public void handleEscape() {
        switch(gameState) {
            case LEVEL_SELECT:
            case CONTROLS:
                gameState=GameState.MAIN_MENU;
                break;
            case START:
            case FAILED:
                gameState=GameState.LEVEL_SELECT;
                levelRunning=false;
                break;
            case RUNNING:
                gameState=GameState.PAUSED;
                break;
            case PAUSED:
                gameState=GameState.RUNNING;
                break;
            default:
                break;
        }
    }

    //运行中打开暂停设置界面
    public void openSettings() {
        if(gameState==GameState.RUNNING) {
            gameState=GameState.PAUSED;
        }
    }

    //从暂停设置界面返回当前游戏
    public void returnToGame() {
        if(gameState==GameState.PAUSED) {
            gameState=GameState.RUNNING;
        }
    }

    //从游戏设置界面退出当前关卡并返回主菜单
    public void returnToMainMenu() {
        if(gameState==GameState.PAUSED) {
            gameState=GameState.MAIN_MENU;
            levelRunning=false;
        }
    }

    //选择关卡；鼠标事件只提交请求，元素清理和对象创建仍由游戏线程完成
    public void selectLevel(int level) {
        if(gameState!=GameState.LEVEL_SELECT) {
            return;
        }
        if(level!=1 && level!=2) {
            return;
        }

        currentLevel=level;
        startAfterLoad=false;
        loadRequested=true;
        levelRunning=false;
    }

    //在运行和暂停状态之间切换
    public void togglePause() {
        if(gameState==GameState.RUNNING) {
            gameState=GameState.PAUSED;
        }else if(gameState==GameState.PAUSED) {
            gameState=GameState.RUNNING;
        }
    }

    //失败后请求重新加载当前关卡
    public void restartLevel() {
        if(gameState!=GameState.FAILED) {
            return;
        }

        startAfterLoad=true;
        loadRequested=true;
        levelRunning=false;
    }

    //结束当前关卡
    public void finishLevel() {
        levelRunning=false;
    }

    //结束整个游戏
    public void stopGame() {
        running=false;
        levelRunning=false;
        interrupt();
    }

    //判断当前游戏是否暂停
    public boolean isPaused() {
        return gameState==GameState.PAUSED;
    }

    //判断整个游戏逻辑线程是否仍需继续运行
    public boolean isRunning() {
        return running;
    }
}
