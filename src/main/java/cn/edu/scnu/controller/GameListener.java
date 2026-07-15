package cn.edu.scnu.controller;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 处理游戏键盘输入。 */

public class GameListener implements KeyListener {
    private ElementManager em=ElementManager.getManager();
    private GameThread gameThread; //游戏流程线程

    //记录已按下的键，避免系统重复触发按键事件
    private Set<Integer> set=new HashSet<Integer>();

    public void setGameThread(GameThread gameThread) {
        this.gameThread=gameThread;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key=e.getKeyCode();
        if(!set.add(key)){ //同一个按键只处理第一次按下
            return;
        }
        //Enter 启动游戏
        if(key==KeyEvent.VK_ENTER) {
            gameThread.startGame();
            return;
        }
        //P 暂停或继续游戏
        if(key==KeyEvent.VK_P) {
            gameThread.togglePause();
            return;
        }
        //失败后按 R 重新开始
        if(key==KeyEvent.VK_R) {
            if(gameThread.getGameState()==GameThread.GameState.FAILED) {
                gameThread.restartLevel();
                return;
            }
            //非运行状态不处理玩家武器切换
            if(gameThread.getGameState()!=GameThread.GameState.RUNNING) {
                return;
            }
        }
        //Esc 返回或退出当前界面
        if(key==KeyEvent.VK_ESCAPE) {
            gameThread.handleEscape();
            return;
        }
        //非运行状态不处理玩家输入
        if(gameThread.getGameState()!=GameThread.GameState.RUNNING) {
            return;
        }
        //把按键转发给所有玩家对象
        List<ElementObj> play=em.getElementSnapshot(GameElement.PLAY);
        for(ElementObj obj: play){
            obj.keyClick(true,key);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key=e.getKeyCode();
        //忽略没有记录的松键事件
        if(!set.remove(key)){
            return;
        }
        //全局按键不交给玩家处理
        if(isGlobalKey(key)) {
            return;
        }
        //把松键状态转发给所有玩家对象
        List<ElementObj> play=em.getElementSnapshot(GameElement.PLAY);
        for(ElementObj obj: play){
            obj.keyClick(false,key);
        }
    }

    //全局按键不转发给玩家对象
    private boolean isGlobalKey(int key) {
        return key==KeyEvent.VK_ENTER
                || key==KeyEvent.VK_P
                || key==KeyEvent.VK_ESCAPE;
    }
}
