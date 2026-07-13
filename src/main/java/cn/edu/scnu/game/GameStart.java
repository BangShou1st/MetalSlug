package cn.edu.scnu.game;

import cn.edu.scnu.controller.GameListener;
import cn.edu.scnu.controller.GameThread;
import cn.edu.scnu.manager.Camera;
import cn.edu.scnu.manager.GameLoad;
import cn.edu.scnu.show.GameJFrame;
import cn.edu.scnu.show.GameMainJPanel;

public class GameStart {
    /**
     * 程序的唯一入口
     */
    public static void main(String[] args) {
        GameLoad.loadImg();

        GameJFrame gj=new GameJFrame();
        //实例化面板，注入到jframe中
        GameMainJPanel jp=new GameMainJPanel();
        //实例化监听
        GameListener listener=new GameListener();
        //实例化主进程
        GameThread th=new GameThread();
        //创建线程和绘制面板共用的唯一摄像机
        Camera camera=new Camera();
        th.setCamera(camera);
        jp.setCamera(camera);
        listener.setGameThread(th);
        jp.setGameThread(th);
        //注入
        gj.setjPanel(jp);
        gj.setKeyListener(listener);
        gj.setMouseListener(jp);
        gj.setMouseMotionListener(jp);
        gj.setThread(th);

        gj.start();
    }
}
