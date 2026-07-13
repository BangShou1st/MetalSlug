package cn.edu.scnu.controller;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import java.util.List;
import java.util.Map;

/**
 * @说明 游戏的主线程，用于控制游戏加载，游戏关卡，游戏运行时自动化
 *      游戏判定;游戏地图切换 资源释放和重新读取。....
 * @继承 使用继承的方式实现多线程(一般建议使用接口实现)
 */

public class GameThread extends Thread {
    private ElementManager em;

    public GameThread() {
        em = ElementManager.getManager();
    }

    private volatile boolean running=true; //整个游戏是否运行
    private volatile boolean levelRunning=true; //当前关卡是否运行
    private volatile boolean paused=false; //是否暂停

    @Override
    public void run() { //游戏的run方法   主线程
        GameLoad.loadImg(); //公共图片只加载一次
        while(running) {
            levelRunning=true;
            gameLoad(); //加载当前关卡
            gameRun();  //运行当前关卡
            gameOver(); //清理当前关卡
            try {
                Thread.sleep(10);
            }catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * 游戏的加载
     */
    private void gameLoad() {

    }

    /**
     * @说明 游戏进行时
     */
    private void gameRun() {
        long gameTime=0L;
        int sleep=GameLoad.getInt("game.logicInterval");

        while(running && levelRunning) {
            if(paused) { //暂停时不更新游戏对象
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

            elementPk(enemys,playFiles);   //玩家子弹攻击敌人
            elementPk(bosses,playFiles);   //玩家子弹攻击Boss
            elementPk(plays,enemyFiles);   //敌方子弹攻击玩家

            gameTime++;

            try {
                Thread.sleep(sleep);
            }catch (InterruptedException e) {
                return;
            }
        }
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

    private void elementPk(List<ElementObj> listA,List<ElementObj> listB) {
        for(int i=0;i<listA.size();i++) {
            ElementObj a=listA.get(i);
            if(!a.isLive()) {
                continue;
            }
            for(int j=0;j<listB.size();j++) {
                ElementObj b=listB.get(j);
                if(!b.isLive()) {
                    continue;
                }
                if(a.pk(b)) {
                    a.hurt(b.getAttack());
                    b.setLive(false);
                    break;
                }
            }
        }
    }

    /**
     * 游戏切换关卡
     */
    private void gameOver() {
    }

    //暂停游戏
    public void pauseGame() {
        paused=true;
    }

    //继续游戏
    public void continueGame() {
        paused=false;
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

    public boolean isPaused() {
        return paused;
    }
}
