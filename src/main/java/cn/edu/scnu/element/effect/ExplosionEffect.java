package cn.edu.scnu.element.effect;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.GameLoad;

import java.awt.*;

/** 居中播放且不参与碰撞的爆炸特效。 */
public class ExplosionEffect extends ElementObj {
    private static final String SMALL_ANIMATION="effect.explosion"; //小型爆炸动画键
    private static final String LARGE_ANIMATION="effect.explosion.large"; //大型爆炸动画键
    private static final int SMALL_INTERVAL=1; //小型爆炸每个逻辑帧切换
    private static final int LARGE_INTERVAL=1; //大型爆炸每个逻辑帧切换
    private final int centerX; //爆炸不随帧尺寸变化的世界中心横坐标
    private final int centerY; //爆炸不随帧尺寸变化的世界中心纵坐标
    private final String animationKey; //当前爆炸使用的动画资源键
    private final int animationInterval; //当前爆炸类型的换帧逻辑间隔

    //默认创建小型爆炸
    public ExplosionEffect(int cx, int cy) {
        this(cx,cy,SMALL_ANIMATION);
    }

    //以爆炸中心创建指定动画
    private ExplosionEffect(int cx,int cy,String animationKey) {
        super(cx,cy,1,1,GameLoad.getImages(animationKey).get(0));
        this.centerX=cx;
        this.centerY=cy;
        this.animationKey=animationKey;
        this.animationInterval=SMALL_ANIMATION.equals(animationKey)
                ? SMALL_INTERVAL : LARGE_INTERVAL;
        centerCurrentFrame();
    }

    //创建小型爆炸
    public static ExplosionEffect small(int cx,int cy) {
        return new ExplosionEffect(cx,cy,SMALL_ANIMATION);
    }

    //创建大型爆炸
    public static ExplosionEffect large(int cx,int cy) {
        return new ExplosionEffect(cx,cy,LARGE_ANIMATION);
    }

    //根据当前帧保持爆炸中心不变
    private void centerCurrentFrame() {
        int width=getIcon().getIconWidth();
        int height=getIcon().getIconHeight();
        setW(width);
        setH(height);
        setX(centerX-width/2);
        setY(centerY-height/2);
    }

    @Override
    public void showElement(Graphics g) {
        g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
    }

    //播放一次爆炸动画
    @Override
    protected void updateImage(long gameTime) {
        playAnimation(animationKey,gameTime,animationInterval,false);
        centerCurrentFrame();
        //动画结束后移除特效
        if (isAnimationEnd()) setLive(false);
    }
}
