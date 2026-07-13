package cn.edu.scnu.element.effect;

import cn.edu.scnu.element.ElementObj;

import java.awt.*;

public class MuzzleEffect extends ElementObj {
    private int dir;

    public MuzzleEffect(int x, int y, int dir) {
        super(x, y, 32, 35, null);
        this.dir = dir;
    }

    @Override
    public void showElement(Graphics g) {
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
