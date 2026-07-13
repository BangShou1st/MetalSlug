package cn.edu.scnu.controller;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @说明 监听类，用于监听用户的操作 KeyListener
 */

public class GameListener implements KeyListener {
    private ElementManager em=ElementManager.getManager();
    private GameThread gameThread; //处理开始、暂停和重新开始的游戏线程

    /**
     * 通过一个集合来记录所有按下的键，如果重复触发，就直接结束
     * 同时，第1次按下，记录到集合中，第2次判定集合中否有。
     * 松开就直接删除集合中的记录。
     * set集合
     */
    private Set<Integer> set=new HashSet<Integer>();

    //注入需要接收全局按键的游戏线程
    public void setGameThread(GameThread gameThread) {
        this.gameThread=gameThread;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
    /**
     * 按下：左37 上38 右39 下40
     * 实现主角的移动
     */
    @Override
    public void keyPressed(KeyEvent e) {
        //System.out.println("keyPressed"+e.getKeyCode());
        int key=e.getKeyCode();
        if(!set.add(key)){ //同一个按键只处理第一次按下
            return;
        }
        if(key==KeyEvent.VK_ENTER) {
            gameThread.startGame();
            return;
        }
        if(key==KeyEvent.VK_P) {
            gameThread.togglePause();
            return;
        }
        if(key==KeyEvent.VK_R) {
            gameThread.restartLevel();
            return;
        }
        if(key==KeyEvent.VK_ESCAPE) {
            gameThread.handleEscape();
            return;
        }
        //拿到玩家集合
        List<ElementObj> play=em.getElementByKey(GameElement.PLAY);
        for(ElementObj obj: play){
            obj.keyClick(true,key);
        }
    }
    /**
     * 松开
     */
    @Override
    public void keyReleased(KeyEvent e) {
        //System.out.println("keyReleased"+e.getKeyCode());
        int key=e.getKeyCode();
        if(!set.remove(key)){
            return;
        }
        if(isGlobalKey(key)) {
            return;
        }
        List<ElementObj> play=em.getElementByKey(GameElement.PLAY);
        for(ElementObj obj: play){
            obj.keyClick(false,key);
        }
    }

    //判断按键是否只用于控制全局游戏流程
    private boolean isGlobalKey(int key) {
        return key==KeyEvent.VK_ENTER
                || key==KeyEvent.VK_P
                || key==KeyEvent.VK_R
                || key==KeyEvent.VK_ESCAPE;
    }
}
