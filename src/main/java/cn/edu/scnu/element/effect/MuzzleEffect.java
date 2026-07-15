package cn.edu.scnu.element.effect;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.GameLoad;

import java.awt.*;

/** 根据射击方向播放一次的枪口特效。 */
public class MuzzleEffect extends ElementObj {
    private int dir; //枪口特效当前朝向

    /** @param dir 射击朝向（1 右，-1 左） */
    public MuzzleEffect(int x, int y, int dir) {
        super(x, y, 32, 35, GameLoad.getImages("effect.muzzle").get(0));
        this.dir = dir;
    }

    //按射击方向绘制
    @Override
    public void showElement(Graphics g) {
        //朝左时水平翻转
        if (dir < 0) {
            g.drawImage(getIcon().getImage(), getX() + getW(), getY(), -getW(), getH(), null);
        } else {
            g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
        }
    }

    //动画结束后失效
    @Override
    protected void updateImage(long gameTime) {
        playAnimation("effect.muzzle", gameTime, 2, false);
        //动画结束后移除特效
        if (isAnimationEnd()) setLive(false);
    }
}
