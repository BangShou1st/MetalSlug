package cn.edu.scnu.game;

import cn.edu.scnu.controller.GameListener;
import cn.edu.scnu.controller.GameThread;
import cn.edu.scnu.manager.Camera;
import cn.edu.scnu.manager.GameAudio;
import cn.edu.scnu.manager.GameLoad;
import cn.edu.scnu.show.GameJFrame;
import cn.edu.scnu.show.GameMainJPanel;

import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameStart {
    /** 程序入口。 */
    public static void main(String[] args) {
        //预加载图片并播放背景音乐
        GameLoad.loadImg();
        GameAudio.loop("music.background");
        //在 Swing 事件线程中创建窗口
        SwingUtilities.invokeLater(GameStart::startGame);
    }

    private static void startGame() {
        GameJFrame gj=new GameJFrame();
        GameMainJPanel jp=new GameMainJPanel();
        GameListener listener=new GameListener();
        GameThread gameThread=new GameThread();
        //创建线程和绘制面板共用的唯一摄像机
        Camera camera=new Camera();
        gameThread.setCamera(camera);
        jp.setCamera(camera);
        //注入线程、监听器和面板
        listener.setGameThread(gameThread);
        jp.setGameThread(gameThread);
        gj.setjPanel(jp);
        gj.setKeyListener(listener);
        gj.setMouseListener(jp);
        gj.setMouseMotionListener(jp);
        gj.setThread(gameThread);
        //关闭窗口时停止游戏资源
        gj.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gameThread.stopGame();
            }
        });

        gj.start();
    }
}
