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

    @Override
    public void run() { //游戏的run方法   主线程
        while(true) { //true可以变为控制关卡结束等的变量,这里特别注意
            //游戏开始前  读进度条，加载游戏资源（场景资源）
            gameLoad();
            //游戏进行时  游戏过程中
            gameRun();
            //游戏场景结束    游戏资源回收（场景资源）
            gameOver();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 游戏的加载
     */
    private void gameLoad() {
        GameLoad.loadImg(); //加载图片
        //GameLoad.MapLoad(5);  //可以变为变量，每一关重新加载 加载地图
        //加载主角
        //GameLoad.loadPlay();
        //加载敌人NPC
        //全部加载完成，游戏启动
    }

    /**
     * @说明 游戏进行时
     * @任务说明 游戏过程中需要做的事情:1.自动化玩家的移动，碰撞，死亡
     *                            2.新元素的增加(NPC死亡后出现道具)
     *                            3.暂停等等
     */
    private void gameRun() {
        long gameTime=0L;
        while(true) { //true可以变为控制关卡结束等的变量,这里特别注意
            Map<GameElement, List<ElementObj>> all=em.getGameElements();
            List<ElementObj> enemys=em.getElementByKey(GameElement.ENEMY);
            List<ElementObj> files=em.getElementByKey(GameElement.PLAYFILE);

            moveAndUpdate(all,gameTime); //游戏元素自动化方法

            ElementPk(enemys,files);

            gameTime++;//唯一的时间控制
            try {
                Thread.sleep(GameLoad.getInt("game.logicInterval"));
            } catch (InterruptedException e) {}
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

    private void ElementPk(List<ElementObj> listA, List<ElementObj> listB) {
        for(int i=0;i<listA.size();i++){
            ElementObj a=listA.get(i);
            for(int j=0;j<listB.size();j++){
                ElementObj b=listB.get(j);
                if(a.pk(b)){
                    //问题:如果是boos，那么也一枪一个吗????
                    //将setLive(false)变为一个受攻击方法，还可以传入另外一个对象的攻击力
                    //当受攻击方法里执行时，如果血量减为0再进行设置生存为false
                    a.setLive(false);
                    b.setLive(false);
                    break;
                }
            }
        }
    }

    public void ElementPk() {



    }

    /**
     * 游戏切换关卡
     */
    private void gameOver() {
    }
}
