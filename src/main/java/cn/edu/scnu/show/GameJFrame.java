package cn.edu.scnu.show;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import cn.edu.scnu.manager.GameLoad;

/**
 * @说明 游戏窗体 主要实现的功能：关闭，显示，最大最小化
 * @功能说明  需要嵌入面板,启动主线程等等
 * @窗体说明 swing awt 窗体大小（记录用户上次使用软件的窗体样式）
 *
 * @分析 1.面板绑定到窗体
 *      2.监听绑定
 *      3.游戏主线程启动
 *      4.显示窗体
 */

public class GameJFrame extends JFrame {
    private JPanel jPanel=null;//正在显示的面板
    private KeyListener keyListener=null;//键盘监听
    private MouseMotionListener  mouseMotionListener=null;//鼠标监听
    private MouseListener mouseListener=null;
    private Thread thread=null;//游戏主线程

    public GameJFrame() {
        init();
    }
    private void init() {
        int width=GameLoad.getInt("window.width");
        int height=GameLoad.getInt("window.height");

        this.setSize(width,height);
        this.setTitle(GameLoad.getString("window.title"));
        this.setResizable(GameLoad.getBoolean("window.resizable"));

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }

    /*窗体布局：可以存档、读档*/
    public void addButton() {
        //this.setLayout(manager); //布局格式，可以添加控件
    }

    /**
     * 启动方法
     */
    public void start() {
        if(jPanel!=null) {
            this.add(jPanel);
        }
        if(keyListener!=null) {
            this.addKeyListener(keyListener);
        }
        if(mouseListener!=null) {
            this.addMouseListener(mouseListener);
        }
        if(mouseMotionListener!=null) {
            this.addMouseMotionListener(mouseMotionListener);
        }
        this.setVisible(true);
        this.requestFocusInWindow();
        if(thread!=null) {
            thread.start();
        }
        if(jPanel instanceof Runnable) {
            new Thread((Runnable)jPanel).start();
        }
    }

    //set注入：通过set方法注入配置文件中读取的数据;将配置文件中的数据赋值为类的属性
    public void setjPanel(JPanel jPanel) {
        this.jPanel = jPanel;
    }

    public void setKeyListener(KeyListener keyListener) {
        this.keyListener = keyListener;
    }

    public void setMouseMotionListener(MouseMotionListener mouseMotionListener) {
        this.mouseMotionListener = mouseMotionListener;
    }

    public void setMouseListener(MouseListener mouseListener) {
        this.mouseListener = mouseListener;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
