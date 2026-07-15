package cn.edu.scnu.show;

import cn.edu.scnu.controller.GameThread;
import cn.edu.scnu.manager.Camera;
import cn.edu.scnu.manager.GameLoad;
import cn.edu.scnu.show.GameRenderer.MenuButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/** 负责画面刷新和鼠标输入的游戏面板。 */
public class GameMainJPanel extends JPanel
        implements Runnable,MouseListener,MouseMotionListener {
    private final GameRenderer renderer=new GameRenderer(); //集中处理所有游戏画面的绘制
    private GameThread gameThread; //处理界面操作和控制重绘生命周期的游戏线程
    private MenuButton hoveredButton=MenuButton.NONE; //当前鼠标悬停的按钮

    //创建负责刷新和输入的游戏面板
    public GameMainJPanel() {
        int width=GameLoad.getInt("window.width");
        int height=GameLoad.getInt("window.height");
        setPreferredSize(new Dimension(width,height));
        setFocusable(false);
    }

    //注入与游戏线程共用的摄像机
    public void setCamera(Camera camera) {
        renderer.setCamera(camera);
    }

    //注入绘制和界面操作使用的游戏线程
    public void setGameThread(GameThread gameThread) {
        this.gameThread=gameThread;
        renderer.setGameThread(gameThread);
    }

    //将当前面板尺寸和悬停状态交给绘制器
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.draw((Graphics2D)g,getWidth(),getHeight(),hoveredButton);
    }

    //处理界面按钮的鼠标左键点击
    @Override
    public void mouseClicked(MouseEvent e) {
        //只处理鼠标左键
        if(!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        MenuButton button=renderer.findButton(
                e.getX(),e.getY(),getWidth(),getHeight());
        //按命中的按钮切换游戏流程
        switch(button) {
            case START_GAME:
                gameThread.openLevelSelect();
                break;
            case CONTROLS:
                gameThread.openControls();
                break;
            case EXIT_GAME:
                gameThread.stopGame();
                break;
            case LEVEL_ONE:
                gameThread.selectLevel(1);
                break;
            case LEVEL_TWO:
                gameThread.selectLevel(2);
                break;
            case BACK:
                gameThread.backToMainMenu();
                break;
            case SETTINGS:
                gameThread.openSettings();
                break;
            case RETURN_GAME:
                gameThread.returnToGame();
                break;
            case HOME:
                gameThread.returnToMainMenu();
                break;
            default:
                return;
        }

        resetMenuHover();
    }

    //更新按钮悬停效果和鼠标光标
    @Override
    public void mouseMoved(MouseEvent e) {
        MenuButton next=renderer.findButton(
                e.getX(),e.getY(),getWidth(),getHeight());
        //悬停按钮变化时重新绘制
        if(next!=hoveredButton) {
            hoveredButton=next;
            repaint();
        }
        //可点击区域显示手型光标
        if(next!=MenuButton.NONE) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    //清除按钮悬停状态并恢复默认光标
    private void resetMenuHover() {
        hoveredButton=MenuButton.NONE;
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    //鼠标离开面板时清除按钮悬停效果
    @Override
    public void mouseExited(MouseEvent e) {
        resetMenuHover();
    }

    //当前界面不处理鼠标按下事件
    @Override
    public void mousePressed(MouseEvent e) {
    }

    //当前界面不处理鼠标松开事件
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    //当前界面不处理鼠标进入事件
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    //当前界面不处理鼠标拖拽事件
    @Override
    public void mouseDragged(MouseEvent e) {
    }

    //按照配置间隔刷新画面，逻辑线程结束后同步停止重绘
    @Override
    public void run() {
        int sleep=GameLoad.getInt("game.repaintInterval");
        //游戏运行期间按固定间隔重绘
        while(gameThread.isRunning()) {
            repaint();
            try {
                Thread.sleep(sleep);
            }catch (InterruptedException e) {
                return;
            }
        }
        closeGameWindow();
    }

    //逻辑线程停止后在 Swing 事件线程中关闭当前游戏窗口
    private void closeGameWindow() {
        //回到 Swing 事件线程关闭窗口
        SwingUtilities.invokeLater(() -> {
            Window window=SwingUtilities.getWindowAncestor(this);
            if(window!=null) {
                window.dispose();
            }
        });
    }
}
