package cn.edu.scnu.element.effect;

import cn.edu.scnu.element.ElementObj;

import java.awt.*;

/**
 * 爆炸特效，8 帧非循环动画，播完自杀，不参与碰撞。
 * <p>
 * 生成于爆炸中心位置，碰撞伤害由外部范围判定（Grenade、Rocket）或 GameThread 处理。
 *
 * @author B
 */
public class ExplosionEffect extends ElementObj {
    /** 在爆炸中心居中显示 */
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
