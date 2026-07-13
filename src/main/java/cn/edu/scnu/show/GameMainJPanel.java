package cn.edu.scnu.show;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.List;

/**
 * @说明 游戏的主要面板
 * @功能说明 主要进行元素的显示，同时进行界面的刷新(多线程)
 *
 * @多线程刷新 1.本类实现线程接口
 *           2.本类中定义一个内部类来实现
 */

public class GameMainJPanel extends JPanel implements Runnable {
    //联动管理器
    private ElementManager em;

    public GameMainJPanel() {
        init();
    }
    public void init() {
        em=ElementManager.getManager();//得到元素管理器对象
    }

    /**
     * paint方法是进行绘画元素
     * 绘画时是有固定的顺序，先绘画的图片会在底层，后绘画的图片会覆盖先绘画的
     * 约定:本方法只执行一次，想实时刷新需要使用多线程
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Map<GameElement,List<ElementObj>> all=em.getGameElements();
        for(GameElement ge:GameElement.values()){ //GameElement.values();返回值是一个数组，数组的顺序就是定义枚举的顺序
            List<ElementObj> list=all.get(ge);
            for(ElementObj obj:list){
                obj.showElement(g);//调用每个类的自己的show方法完成自己的显示
            }
        }
    }

    @Override
    public void run() {
        int sleep= GameLoad.getInt("game.repaintInterval");
        while(true) {
            repaint();
            try {
                Thread.sleep(sleep);
            }catch (InterruptedException e) {
                return;
            }
        }
    }
}
