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

    /**
     * 通过一个集合来记录所有按下的键，如果重复触发，就直接结束
     * 同时，第1次按下，记录到集合中，第2次判定集合中否有。
     * 松开就直接删除集合中的记录。
     * set集合
     */
    private Set<Integer> set=new HashSet<Integer>();

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
        if(set.contains(key)){ //判定集合中是否已经存在这个对象
            //如果包含直接结束方法
            return;
        }
        set.add(key);
        //拿到玩家集合
        List<ElementObj> play=em.getElementByKey(GameElement.PLAY);
        for(ElementObj obj: play){
            obj.keyClick(true,e.getKeyCode());
        }
    }
    /**
     * 松开
     */
    @Override
    public void keyReleased(KeyEvent e) {
        //System.out.println("keyReleased"+e.getKeyCode());
        if(!set.contains(e.getKeyCode())){
            return;
        }
        set.remove(e.getKeyCode());//移除数据
        List<ElementObj> play=em.getElementByKey(GameElement.PLAY);
        for(ElementObj obj: play){
            obj.keyClick(false,e.getKeyCode());
        }
    }
}
