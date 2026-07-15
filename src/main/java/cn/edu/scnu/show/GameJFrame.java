package cn.edu.scnu.show;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import cn.edu.scnu.manager.GameLoad;

/** 承载游戏面板并管理游戏线程的窗口。 */

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
        this.setTitle(GameLoad.getString("window.title"));
        this.setResizable(GameLoad.getBoolean("window.resizable"));

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    /** 显示窗口并启动游戏线程。 */
    public void start() {
        //装配游戏面板
        if(jPanel!=null) {
            this.setContentPane(jPanel);
        }
        //绑定键盘和鼠标监听
        if(keyListener!=null) {
            this.addKeyListener(keyListener);
        }
        if(mouseListener!=null && jPanel!=null) {
            jPanel.addMouseListener(mouseListener);
        }
        if(mouseMotionListener!=null && jPanel!=null) {
            jPanel.addMouseMotionListener(mouseMotionListener);
        }
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.requestFocusInWindow();
        //启动游戏逻辑线程
        if(thread!=null) {
            thread.start();
        }
        //单独启动画面刷新线程
        if(jPanel instanceof Runnable) {
            Thread repaintThread=new Thread((Runnable)jPanel,"MetalSlug-Repaint");
            repaintThread.start();
        }
    }

    //设置窗口参数
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
