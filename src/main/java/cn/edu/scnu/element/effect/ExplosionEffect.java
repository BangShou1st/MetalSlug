package cn.edu.scnu.element.effect;

import cn.edu.scnu.element.ElementObj;

import java.awt.*;

public class ExplosionEffect extends ElementObj {
    public ExplosionEffect(int cx, int cy) {
        super(cx - 47, cy - 46, 94, 92, null);
    }

    @Override
    public void showElement(Graphics g) {
        g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
    }

    @Override
    protected void updateImage(long gameTime) {
        playAnimation("effect.explosion", gameTime, 4, false);
        if (isAnimationEnd()) setLive(false);
    }
}
