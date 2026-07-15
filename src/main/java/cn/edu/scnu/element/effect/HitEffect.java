package cn.edu.scnu.element.effect;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.GameLoad;

import java.awt.*;

/** 播放一次且不参与碰撞的命中特效。 */
public class HitEffect extends ElementObj {
    /** 在命中点居中显示 */
    public HitEffect(int x, int y) {
        super(x - 12, y - 12, 24, 24,
                GameLoad.getImages("effect.hit").get(0));
    }

    @Override
    public void showElement(Graphics g) {
        g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
    }

    @Override
    protected void updateImage(long gameTime) {
        playAnimation("effect.hit", gameTime, 2, false);
        //动画结束后移除特效
        if (isAnimationEnd()) setLive(false);
    }
}
