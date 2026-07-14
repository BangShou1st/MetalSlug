package cn.edu.scnu.element.hostage;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

/**
 * 人质类 - 中立单位，等待玩家营救
 * 
 * <p>职责：
 * <ul>
 * <li>等待玩家接近（60像素范围内）</li>
 * <li>被营救后切换逃跑状态并向右移动</li>
 * <li>离开世界边界后自动删除</li>
 * </ul>
 * 
 * <p>设计注意：
 * <ul>
 * <li>人质属于中立阵营（NEUTRAL），不能被任何阵营伤害</li>
 * <li>营救距离为60像素（以中心点计算）</li>
 * <li>逃跑速度为3.0像素/帧，速度较快</li>
 * </ul>
 */
public class Hostage extends ElementObj {
    private static final String IDLE_ANIMATION="hostage.idle";
    private static final String RUN_ANIMATION="hostage.run";
    private static final int IDLE_INTERVAL=3;
    private static final int RUN_INTERVAL=2;
    /** 逃跑速度，3.0像素/帧 */
    private static final double RUN_SPEED=3.0;

    /** 当前状态 */
    private HostageState state=HostageState.IDLE;
    /** 精确横坐标，用于平滑移动 */
    private double preciseX;
    /** 是否朝右 */
    private boolean facingRight=true;
    /** 营救距离，玩家进入此范围即触发营救，60像素 */
    private int rescueDistance=60;

    /** 供GameLoad通过反射创建模板对象 */
    public Hostage() {
    }

    /**
     * 创建人质实体
     * @param x 初始世界横坐标
     * @param y 初始世界纵坐标
     * @param icon 初始动画帧图标
     */
    private Hostage(int x, int y, ImageIcon icon) {
        super(x, y, icon.getIconWidth(), icon.getIconHeight(), icon);
        preciseX=x;
    }

    /**
     * 按配置字符串创建人质
     * <p>配置格式：x,y
     * @param str 配置字符串
     * @return 人质实例
     */
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        if (data.length != 2) {
            throw new IllegalArgumentException("人质配置格式应为 x,y");
        }

        int x=Integer.parseInt(data[0].trim());
        int y=Integer.parseInt(data[1].trim());

        ImageIcon icon=GameLoad.getImages(IDLE_ANIMATION).get(0);
        return new Hostage(x, y, icon);
    }

    /**
     * 绘制人质，使用脚底中心锚点，支持左右翻转
     * @param g 画笔
     */
    @Override
    public void showElement(Graphics g) {
        ImageIcon frame=getIcon();
        if (frame == null) {
            return;
        }

        int drawWidth=frame.getIconWidth();
        int drawHeight=frame.getIconHeight();
        int footX=getX() + getW() / 2;
        int footY=getY() + getH();
        int drawX=footX - drawWidth / 2;
        int drawY=footY - drawHeight;
        Image image=frame.getImage();

        if (facingRight) {
            g.drawImage(image, drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
        } else {
            g.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
        }
    }

    /**
     * 移动逻辑：只有RUNNING状态下向右移动
     */
    @Override
    protected void move() {
        if (state == HostageState.RUNNING) {
            preciseX+=RUN_SPEED;
            setX((int) Math.round(preciseX));

            /** 离开世界边界（6000像素）后标记为LEFT并删除 */
            if (getX() > 6000) {
                state=HostageState.LEFT;
                setLive(false);
            }
        }
    }

    /** 根据当前状态切换动画 */
    @Override
    protected void updateImage(long gameTime) {
        switch (state) {
            case RUNNING:
                playAnimation(RUN_ANIMATION, gameTime, RUN_INTERVAL, true);
                break;
            default:
                playAnimation(IDLE_ANIMATION, gameTime, IDLE_INTERVAL, true);
                break;
        }
    }

    /**
     * 每帧检查营救状态
     * @param gameTime 游戏时间
     */
    @Override
    protected void add(long gameTime) {
        if (state == HostageState.IDLE) {
            checkRescue();
        }
    }

    /**
     * 检查是否被玩家营救
     * <p>计算玩家与人质中心的水平距离，小于等于rescueDistance即触发营救
     */
    private void checkRescue() {
        ElementObj player=findPlayer();
        if (player == null) {
            return;
        }

        int playerCenterX=player.getX() + player.getW() / 2;
        int hostageCenterX=getX() + getW() / 2;
        int distance=Math.abs(playerCenterX - hostageCenterX);

        if (distance <= rescueDistance) {
            state=HostageState.RESCUED;
            facingRight=true;
            state=HostageState.RUNNING;
        }
    }

    /** 从ElementManager查找第一个存活的玩家 */
    private ElementObj findPlayer() {
        for (ElementObj player : ElementManager.getManager()
                .getElementByKey(GameElement.PLAY)) {
            if (player.isLive()) {
                return player;
            }
        }
        return null;
    }

    /** 返回贴合身体区域的碰撞框 */
    @Override
    public Rectangle getRectangle() {
        return new Rectangle(
                getX() + (int) (getW() * 0.15),
                getY() + (int) (getH() * 0.10),
                (int) (getW() * 0.70),
                (int) (getH() * 0.85));
    }

    /** 获取当前状态 */
    public HostageState getState() {
        return state;
    }
}