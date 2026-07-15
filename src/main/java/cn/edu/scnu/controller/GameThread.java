package cn.edu.scnu.controller;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.MapObj;
import cn.edu.scnu.element.Play;
import cn.edu.scnu.element.RoleObj;
import cn.edu.scnu.element.effect.HitEffect;
import cn.edu.scnu.element.boss.AbstractBoss;
import cn.edu.scnu.element.enemy.AbstractEnemy;
import cn.edu.scnu.element.hostage.Hostage;
import cn.edu.scnu.element.hostage.HostageState;
import cn.edu.scnu.element.weapon.Bullet;
import cn.edu.scnu.element.weapon.EnemyBullet;
import cn.edu.scnu.element.weapon.Grenade;
import cn.edu.scnu.manager.Camera;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameAudio;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.Rectangle;
import java.util.List;

/** 负责关卡加载、游戏循环和流程切换。 */

public class GameThread extends Thread {
    private static final int ATTACK_VIEW_MARGIN=24; //攻击前要求进入视口的水平内边距
    //游戏流程状态
    public enum GameState {
        MAIN_MENU, //主菜单
        LEVEL_SELECT, //关卡选择
        CONTROLS, //操作说明
        START, //关卡已加载，等待开始
        RUNNING, //关卡正常运行
        PAUSED, //暂停后的游戏设置界面
        FAILED, //玩家死亡后的失败界面
        LEVEL_CLEAR, //第一关 Boss 被击败后的完成界面
        VICTORY //全部关卡完成后的胜利界面
    }

    private ElementManager em;
    private Camera camera; //游戏线程和绘制面板共用的摄像机
    private volatile GameState gameState=GameState.MAIN_MENU; //当前游戏流程状态
    private volatile boolean loadRequested; //是否需要加载当前选择的关卡
    private volatile boolean startAfterLoad; //加载完成后是否直接进入运行状态
    private volatile int currentLevel=1; //当前选择的逻辑关卡编号
    private boolean levelBossLoaded; //当前关卡是否已经成功创建 Boss
    private int levelEnemyTotal; //当前关卡普通敌人与 Boss 的总数
    private int levelEnemyDefeated; //当前关卡完成死亡生命周期的敌人数
    private int levelHostageTotal; //当前关卡配置的人质总数
    private int levelHostageRescued; //当前关卡首次进入营救状态的人质数
    private int levelElapsedFrames; //当前关卡处于运行状态的累计逻辑帧数

    public GameThread() {
        em = ElementManager.getManager();
    }

    public void setCamera(Camera camera) {
        this.camera=camera;
    }

    private volatile boolean running=true; //整个游戏是否运行
    private volatile boolean levelRunning=true; //当前关卡是否运行

    //等待加载请求并运行关卡
    @Override
    public void run() {
        //游戏线程持续等待和执行关卡
        while(running) {
            //没有加载请求时保持等待
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
            //按请求决定是否直接开始关卡
            if(startAfterLoad) {
                enterRunningState();
            }else {
                gameState=GameState.START;
            }
            startAfterLoad=false;
            gameRun();  //运行当前关卡
            gameOver(); //清理当前关卡
        }
    }

    //清理旧关卡并加载当前关卡对象
    private void gameLoad() {
        em.clearAll();
        camera.reset();
        levelBossLoaded=false;
        resetLevelStatistics();

        //加载地图并设置摄像机范围
        MapObj map=loadMap();
        camera.setWorldSize(map.getW(),map.getH());

        int playerX=GameLoad.getInt("level"+currentLevel+".player.x");
        int playerHp=GameLoad.getInt("player.hp");
        int playerAttack=GameLoad.getInt("player.attack");
        //创建玩家并放到地图地面
        GameLoad.loadPlay(playerX+",0,"+playerHp+","+playerAttack);
        Play play=(Play)em.getElementByKey(GameElement.PLAY).get(0);
        play.setWorldWidth(map.getW());
        play.placeOnGround(map.getGroundY());
        loadEnemies(map);
        loadBoss(map);
        loadHostages(map);
    }

    //重置关卡统计
    private void resetLevelStatistics() {
        levelEnemyTotal=GameLoad.getInt(
                "level"+currentLevel+".enemy.count")+1;
        levelEnemyDefeated=0;
        levelHostageTotal=GameLoad.getInt(
                "level"+currentLevel+".hostage.count");
        levelHostageRescued=0;
        levelElapsedFrames=0;
    }

    //按素材尺寸和视口高度创建地图
    private MapObj loadMap() {
        String mapKey="map.level"+currentLevel;
        ImageIcon source=GameLoad.getImage(mapKey);
        int baseScale=GameLoad.getInt("map.scale");
        int viewportHeight=GameLoad.getInt("window.height");

        int baseHeight=source.getIconHeight()*baseScale;
        int mapHeight=Math.max(baseHeight,viewportHeight);
        double actualScale=(double)mapHeight/source.getIconHeight();
        int mapWidth=(int)Math.round(source.getIconWidth()*actualScale);
        int groundY=(int)Math.round(
                GameLoad.getInt(mapKey+".groundSourceY")*actualScale);

        ElementObj mapTemplate=GameLoad.getObj("map");
        MapObj map=(MapObj)mapTemplate.createElement(
                mapKey+",0,0,"+mapWidth+","+mapHeight+","+groundY);
        em.addElement(map,GameElement.MAPS);
        return map;
    }

    //加载普通敌人
    private void loadEnemies(MapObj map) {
        int count=GameLoad.getInt("level"+currentLevel+".enemy.count");
        //按配置逐个创建敌人
        for(int i=1;i<=count;i++) {
            String value=GameLoad.getString(
                    "level"+currentLevel+".enemy."+i);
            ElementObj enemy=createConfiguredElement(value,"敌人");
            enemy.setY(map.getGroundY()-enemy.getH());
            em.addElement(enemy,GameElement.ENEMY);
        }
    }

    //加载 Boss
    private void loadBoss(MapObj map) {
        String value=GameLoad.getString("level"+currentLevel+".boss");
        ElementObj boss=createConfiguredElement(value,"Boss");
        boss.setY(map.getGroundY()-boss.getH());
        em.addElement(boss,GameElement.BOSS);
        levelBossLoaded=true;
    }

    //加载人质
    private void loadHostages(MapObj map) {
        int count=GameLoad.getInt("level"+currentLevel+".hostage.count");
        //按配置逐个创建人质
        for(int i=1;i<=count;i++) {
            String value=GameLoad.getString("level"+currentLevel+".hostage."+i);
            Hostage hostage=(Hostage)createConfiguredElement(value,"人质");
            hostage.setY(map.getGroundY()-hostage.getH());
            em.addElement(hostage,GameElement.HOSTAGE);
        }
    }

    //解析“对象键|参数”配置
    private ElementObj createConfiguredElement(String value,String configName) {
        String[] data=value.split("\\|",2);
        //配置必须包含对象键和创建参数
        if(data.length!=2) {
            throw new IllegalArgumentException(configName+"配置格式应为 对象键|创建参数");
        }
        ElementObj template=GameLoad.getObj(data[0].trim());
        return template.createElement(data[1].trim());
    }

    //运行当前关卡的逻辑循环
    private void gameRun() {
        long gameTime=0L;
        int sleep=GameLoad.getInt("game.logicInterval");

        //关卡有效期间持续更新逻辑
        while(running && levelRunning) {
            //非运行状态只保持界面刷新
            if(gameState!=GameState.RUNNING) {
                try {Thread.sleep(sleep);
                }catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            countRunningFrame();

            List<ElementObj> enemys=em.getElementByKey(GameElement.ENEMY);
            List<ElementObj> bosses=em.getElementByKey(GameElement.BOSS);
            List<ElementObj> plays=em.getElementByKey(GameElement.PLAY);
            List<ElementObj> playFiles=em.getElementByKey(GameElement.PLAYFILE);
            List<ElementObj> enemyFiles=em.getElementByKey(GameElement.ENEMYFILE);
            List<ElementObj> hostages=em.getElementByKey(GameElement.HOSTAGE);

            //先更新元素、摄像机和流程状态
            updateCamera(plays);
            updateAttackVisibility(enemys,bosses);
            moveAndUpdate(gameTime);
            rescueHostages(plays,hostages);
            updateFailedState(plays);
            updateLevelCompletion(bosses);
            //关卡结束后跳过本帧碰撞
            if(gameState!=GameState.RUNNING) {
                gameTime++;
                continue;
            }
            updateCamera(plays);
            discardPlayerBulletsOutsideViewport(playFiles);

            //结算玩家发射物
            playerProjectilePk(enemys,playFiles);
            playerProjectilePk(bosses,playFiles);
            applyGrenadeBlasts(playFiles,enemys,bosses);
            //结算敌方发射物
            enemyProjectilePk(plays,enemyFiles);
            applyEnemyGrenadeBlasts(enemyFiles,plays);

            gameTime++;

            try {
                Thread.sleep(sleep);
            }catch (InterruptedException e) {
                return;
            }
        }
    }

    //玩家移除后判定失败
    private void updateFailedState(List<ElementObj> plays) {
        if(gameState==GameState.RUNNING && plays.isEmpty()) {
            gameState=GameState.FAILED;
            GameAudio.play("stage.gameOver");
        }
    }

    //Boss 移除后判定通关
    private void updateLevelCompletion(List<ElementObj> bosses) {
        if(gameState!=GameState.RUNNING || !levelBossLoaded || !bosses.isEmpty()) {
            return;
        }
        gameState=currentLevel==1 ? GameState.LEVEL_CLEAR : GameState.VICTORY;
        GameAudio.play("stage.missionComplete");
    }

    //跟随玩家位置
    private void updateCamera(List<ElementObj> plays) {
        if(plays.isEmpty()) {
            return;
        }
        ElementObj play=plays.get(0);
        camera.follow(play.getX(),play.getW());
    }

    //清理离开视口的普通子弹
    private void discardPlayerBulletsOutsideViewport(
            List<ElementObj> projectiles) {
        int viewportLeft=camera.getX();
        int viewportRight=viewportLeft+GameLoad.getInt("window.width");
        //检查每个玩家发射物的位置
        for(ElementObj projectile:projectiles) {
            if(projectile instanceof Bullet && projectile.isLive()
                    && (projectile.getX()+projectile.getW()<=viewportLeft
                    || projectile.getX()>=viewportRight)) {
                projectile.setLive(false);
            }
        }
    }

    //更新敌人的攻击可见性
    private void updateAttackVisibility(List<ElementObj> enemies,
                                        List<ElementObj> bosses) {
        int viewportX=camera.getX()+ATTACK_VIEW_MARGIN;
        int viewportY=camera.getY();
        int viewportWidth=GameLoad.getInt("window.width")
                -ATTACK_VIEW_MARGIN*2;
        int viewportHeight=GameLoad.getInt("window.height");
        Rectangle enemyAttackViewport=new Rectangle(viewportX,viewportY,
                Math.max(1,viewportWidth),viewportHeight);
        Rectangle bossViewport=new Rectangle(camera.getX(),camera.getY(),
                GameLoad.getInt("window.width"),viewportHeight);
        //普通敌人使用带内边距的攻击视口
        for(ElementObj enemy:enemies) {
            if(enemy instanceof AbstractEnemy) {
                ((AbstractEnemy)enemy).setAttackVisible(
                        enemyAttackViewport.intersects(enemy.getRectangle()));
            }
        }
        //Boss 使用完整摄像机视口
        for(ElementObj boss:bosses) {
            if(boss instanceof AbstractBoss) {
                AbstractBoss abstractBoss=(AbstractBoss)boss;
                abstractBoss.setAttackVisible(
                        bossViewport.intersects(abstractBoss.getVisibilityBounds()));
            }
        }
    }

    //更新并清理游戏元素
    private void moveAndUpdate(long gameTime) {
        for(GameElement ge:GameElement.values()){
            List<ElementObj> list=em.getElementByKey(ge);
            //倒序遍历，便于安全删除失效元素
            for (int i=list.size()-1;i>=0;i--){
                ElementObj obj = list.get(i);
                //失效元素先执行销毁逻辑再移除
                if (!obj.isLive()) {
                    countDefeatedOnRemoval(ge,obj);
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
        //已失效对象不能受击
        if(!target.isLive()) {
            return false;
        }
        //角色生命值必须大于零
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
            //跳过已经死亡的目标
            if(!canReceiveProjectile(target)) {
                continue;
            }
            for(int j=0;j<projectiles.size();j++) {
                ElementObj projectile=projectiles.get(j);
                if(!projectile.isLive()) {
                    continue;
                }
                //命中后按发射物类型结算
                if(target.pk(projectile)) {
                    //手雷命中后进入爆炸流程
                    if(projectile instanceof Grenade) {
                        ((Grenade)projectile).triggerExplosion();
                    }else {
                        target.hurt(projectile.getAttack());
                        projectile.setLive(false);
                        //普通子弹命中时显示火花
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
            //跳过已经死亡的玩家
            if(!canReceiveProjectile(player)) {
                continue;
            }
            for(int j=0;j<projectiles.size();j++) {
                ElementObj projectile=projectiles.get(j);
                if(!projectile.isLive()) {
                    continue;
                }
                //命中后按发射物类型结算
                if(player.pk(projectile)) {
                    if(projectile instanceof Grenade) {
                        ((Grenade)projectile).triggerExplosion();
                    }else {
                        player.hurt(projectile.getAttack());
                        projectile.setLive(false);
                        if(projectile instanceof EnemyBullet) {
                            addHitEffect(player,projectile);
                        }
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
        //查找本帧刚爆炸的玩家手雷
        for(ElementObj projectile:projectiles) {
            if(!(projectile instanceof Grenade)) {
                continue;
            }
            Grenade grenade=(Grenade)projectile;
            //已经结算过的爆炸不重复处理
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
        //逐个检查目标与爆炸中心的距离
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
            //目标中心在爆炸半径内时受伤
            if(dx*dx+dy*dy<=radiusSquared) {
                target.hurt(grenade.getBlastDamage());
            }
        }
    }

    //结算敌方手雷爆炸并只对存活玩家造成一次范围伤害
    private void applyEnemyGrenadeBlasts(List<ElementObj> enemyFiles,
                                         List<ElementObj> players) {
        //查找本帧刚爆炸的敌方手雷
        for(ElementObj projectile:enemyFiles) {
            if(!(projectile instanceof Grenade)) {
                continue;
            }
            Grenade grenade=(Grenade)projectile;
            if(!grenade.claimBlastDamage()) {
                continue;
            }
            applyGrenadeBlast(grenade,players);
        }
    }

    //玩家接触仍在等待的人质后触发救援
    private void rescueHostages(List<ElementObj> players,
                                List<ElementObj> hostages) {
        //检查玩家与人质的接触
        for(ElementObj player:players) {
            if(!canReceiveProjectile(player)) {
                continue;
            }
            for(ElementObj obj:hostages) {
                if(!obj.isLive()) {
                    continue;
                }
                Hostage hostage=(Hostage)obj;
                //玩家接触等待中的人质时开始救援
                if(hostage.getState()==HostageState.IDLE && player.pk(hostage)) {
                    hostage.rescue(camera.getX()-hostage.getW()-20);
                    if(hostage.getState()==HostageState.RESCUING) {
                        levelHostageRescued++;
                    }
                }
            }
        }
    }

    //当前关卡退出后统一清理世界对象和摄像机
    private void gameOver() {
        em.clearAll();
        camera.reset();
    }

    //获取当前游戏流程状态
    public GameState getGameState() {
        return gameState;
    }

    //获取当前逻辑关卡编号
    public int getCurrentLevel() {
        return currentLevel;
    }

    //获取当前关卡敌人总数，包含普通敌人与 Boss
    public int getLevelEnemyTotal() {
        return levelEnemyTotal;
    }

    //获取当前关卡已完成死亡生命周期的敌人数
    public int getLevelEnemyDefeated() {
        return levelEnemyDefeated;
    }

    //获取当前关卡人质总数
    public int getLevelHostageTotal() {
        return levelHostageTotal;
    }

    //获取当前关卡已解救人质数
    public int getLevelHostageRescued() {
        return levelHostageRescued;
    }

    //获取当前关卡累计运行逻辑帧数
    public int getLevelElapsedFrames() {
        return levelElapsedFrames;
    }

    //按逻辑周期把当前关卡运行帧换算为整秒
    public int getLevelElapsedSeconds() {
        return (int)((long)levelElapsedFrames
                *GameLoad.getInt("game.logicInterval")/1000L);
    }

    //读取当前玩家剩余生命值，失败或玩家已移除时返回零
    public int getPlayerRemainingHp() {
        //失败状态固定返回零生命值
        if(gameState==GameState.FAILED) {
            return 0;
        }
        List<ElementObj> players=em.getElementSnapshot(GameElement.PLAY);
        //玩家不存在时返回零
        if(players.isEmpty() || !(players.get(0) instanceof RoleObj)) {
            return 0;
        }
        return Math.max(0,((RoleObj)players.get(0)).getHp());
    }

    //开始已经加载完成的当前关卡
    public void startGame() {
        //开始当前关卡
        if(gameState==GameState.START) {
            enterRunningState();
        //第一关完成后加载第二关
        }else if(gameState==GameState.LEVEL_CLEAR) {
            gameState=GameState.START;
            GameAudio.stopEffects();
            currentLevel=2;
            startAfterLoad=false;
            loadRequested=true;
            levelRunning=false;
        //全部通关后返回主菜单
        }else if(gameState==GameState.VICTORY) {
            GameAudio.stopEffects();
            gameState=GameState.MAIN_MENU;
            levelRunning=false;
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
            //子菜单返回主菜单
            case LEVEL_SELECT:
            case CONTROLS:
                gameState=GameState.MAIN_MENU;
                break;
            //关卡流程界面返回关卡选择
            case START:
            case FAILED:
            case LEVEL_CLEAR:
                gameState=GameState.LEVEL_SELECT;
                levelRunning=false;
                break;
            //运行中打开暂停界面
            case RUNNING:
                gameState=GameState.PAUSED;
                break;
            //暂停时继续游戏
            case PAUSED:
                gameState=GameState.RUNNING;
                break;
            //胜利界面返回主菜单
            case VICTORY:
                gameState=GameState.MAIN_MENU;
                levelRunning=false;
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
            GameAudio.stopEffects();
            gameState=GameState.MAIN_MENU;
            levelRunning=false;
        }
    }

    //选择关卡；鼠标事件只提交请求，元素清理和对象创建仍由游戏线程完成
    public void selectLevel(int level) {
        //只允许在关卡选择界面提交请求
        if(gameState!=GameState.LEVEL_SELECT) {
            return;
        }
        //只接受现有的两个关卡
        if(level!=1 && level!=2) {
            return;
        }

        currentLevel=level;
        GameAudio.stopEffects();
        startAfterLoad=false;
        loadRequested=true;
        levelRunning=false;
    }

    //在运行和暂停状态之间切换
    public void togglePause() {
        //运行中进入暂停
        if(gameState==GameState.RUNNING) {
            gameState=GameState.PAUSED;
        //暂停时继续运行
        }else if(gameState==GameState.PAUSED) {
            gameState=GameState.RUNNING;
        }
    }

    //失败后请求重新加载当前关卡
    public void restartLevel() {
        //只允许失败状态重新开始
        if(gameState!=GameState.FAILED) {
            return;
        }

        gameState=GameState.START;
        GameAudio.stopEffects();
        startAfterLoad=true;
        loadRequested=true;
        levelRunning=false;
    }

    //结束整个游戏
    public void stopGame() {
        running=false;
        levelRunning=false;
        GameAudio.closeAll();
        interrupt();
    }

    //进入当前关卡运行状态并播放一次开始提示
    private void enterRunningState() {
        gameState=GameState.RUNNING;
        GameAudio.stopEffects();
        //播放当前关卡开始语音
        if(currentLevel==1) {
            GameAudio.play("stage.level1Start");
        }else {
            GameAudio.play("stage.level2Start");
        }
    }

    //仅在运行状态累计一个关卡逻辑帧
    private void countRunningFrame() {
        if(gameState==GameState.RUNNING) {
            levelElapsedFrames++;
        }
    }

    //敌人或 Boss 完成死亡生命周期并实际移除时统计一次击败
    private void countDefeatedOnRemoval(GameElement category,ElementObj obj) {
        boolean enemyCategory=category==GameElement.ENEMY
                || category==GameElement.BOSS;
        if(enemyCategory && obj instanceof RoleObj
                && ((RoleObj)obj).getHp()<=0) {
            levelEnemyDefeated++;
        }
    }

    //判断整个游戏逻辑线程是否仍需继续运行
    public boolean isRunning() {
        return running;
    }
}
