package cn.edu.scnu.element.effect;

import cn.edu.scnu.element.ElementObj;

import java.awt.*;

public class HitEffect extends ElementObj {
    public HitEffect(int x, int y) {
        super(x - 12, y - 12, 24, 24, null);
    }

    @Override
    public void showElement(Graphics g) {
        g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
    }

    @Override
    protected void updateImage(long gameTime) {
        playAnimation("effect.hit", gameTime, 2, false);
        if (isAnimationEnd()) setLive(false);
    }
}
