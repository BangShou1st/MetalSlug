package cn.edu.scnu.element.effect;

import cn.edu.scnu.element.ElementObj;

import java.awt.*;

/**
 * 枪口特效，4 帧非循环动画，播完自杀，不参与碰撞。
 * <p>
 * 根据射击朝向水平翻转。
 *
 * @author B
 */
public class MuzzleEffect extends ElementObj {
    private int dir;

    /** @param dir 射击朝向（1 右，-1 左） */
    public MuzzleEffect(int x, int y, int dir) {
        super(x, y, 32, 35, null);
        this.dir = dir;
    }

    @Override
    public void showElement(Graphics g) {
        // 朝左时水平翻转图片，从枪口位置反向绘制
        if (dir < 0) {
            g.drawImage(getIcon().getImage(), getX() + getW(), getY(), -getW(), getH(), null);
        } else {
            g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
        }
    }

    @Override
    protected void updateImage(long gameTime) {
        playAnimation("effect.muzzle", gameTime, 2, false);
        if (isAnimationEnd()) setLive(false);
    }
}
