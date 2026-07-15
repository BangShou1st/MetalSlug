package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.RoleObj;
import cn.edu.scnu.element.effect.ExplosionEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameAudio;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import java.awt.*;

/** 可瞄准玩家的 Boss 炮弹。 */
public class BossShell extends ProjectileObj {
    private static final double SPEED=6.0; //每逻辑帧固定飞行速度
    private static final int MAX_LIFE_FRAMES=160; //最长存活逻辑帧
    private double preciseX; //精确横坐标
    private double preciseY; //精确纵坐标
    private double vx; //横向速度分量
    private double vy; //纵向速度分量
    private int lifeFrames; //已经飞行的逻辑帧
    private boolean exploded; //防止重复创建爆炸

    /**
     * @param x      出生 x
     * @param y      出生 y
     * @param target 发射时锁定的玩家
     * @param attack 伤害值（应高于普通敌方子弹）
     */
    public BossShell(int x, int y, ElementObj target, int attack) {
        super(x, y, 40, 16, GameLoad.getImages("weapon.bossShell").get(0), attack);
        preciseX=x;
        preciseY=y;
        double[] direction=getLaunchDirection(target);
        vx=direction[0]*SPEED;
        vy=direction[1]*SPEED;
        GameAudio.playIfIdle("weapon.rocket");
    }

    //按照飞行方向绘制 Boss 炮弹
    @Override
    public void showElement(Graphics g) {
        Graphics2D shellGraphics=(Graphics2D)g.create();
        try {
            double angle=Math.atan2(vy,vx)-Math.PI; //素材默认朝左
            shellGraphics.translate(getX()+getW()/2.0,getY()+getH()/2.0);
            shellGraphics.rotate(angle);
            shellGraphics.drawImage(getIcon().getImage(),-getW()/2,-getH()/2,
                    getW(),getH(),null);
        }finally {
            shellGraphics.dispose();
        }
    }

    //按发射方向移动并检查边界和寿命
    @Override
    protected void move() {
        preciseX+=vx;
        preciseY+=vy;
        setX((int)Math.round(preciseX));
        setY((int)Math.round(preciseY));
        lifeFrames++;
        int verticalMargin=getH()*2;
        //越界或超过寿命后失效
        if(getX()+getW()<0 || getX()>getWorldWidth()
                || getY()+getH()<-verticalMargin
                || getY()>getWorldHeight()+verticalMargin
                || lifeFrames>=MAX_LIFE_FRAMES) {
            setLive(false);
        }
    }

    //只瞄准存活角色
    private boolean isTargetAvailable(ElementObj candidate) {
        return candidate!=null && candidate.isLive()
                && (!(candidate instanceof RoleObj)
                || ((RoleObj)candidate).getHp()>0);
    }

    //发射时计算固定方向
    private double[] getLaunchDirection(ElementObj launchTarget) {
        //目标无效时默认向左飞行
        if(!isTargetAvailable(launchTarget)) {
            return new double[]{-1.0,0.0};
        }
        Rectangle targetBounds=launchTarget.getRectangle();
        double dx=targetBounds.getCenterX()-(preciseX+getW()/2.0);
        double dy=targetBounds.getCenterY()-(preciseY+getH()/2.0);
        double length=Math.hypot(dx,dy);
        //目标中心与炮弹中心重合时默认向左
        if(length<0.0001) {
            return new double[]{-1.0,0.0};
        }
        return new double[]{dx/length,dy/length};
    }

    //读取当前地图世界高度，尚未装配地图时回退到窗口高度
    private int getWorldHeight() {
        java.util.List<ElementObj> maps=ElementManager.getManager()
                .getElementByKey(GameElement.MAPS);
        return maps.isEmpty() ? GameLoad.getInt("window.height") : maps.get(0).getH();
    }

    //返回旋转后的碰撞区域
    @Override
    public Rectangle getRectangle() {
        double angle=Math.atan2(vy,vx);
        int width=Math.max(1,(int)Math.ceil(
                Math.abs(getW()*Math.cos(angle))+Math.abs(getH()*Math.sin(angle))));
        int height=Math.max(1,(int)Math.ceil(
                Math.abs(getW()*Math.sin(angle))+Math.abs(getH()*Math.cos(angle))));
        double centerX=getX()+getW()/2.0;
        double centerY=getY()+getH()/2.0;
        return new Rectangle((int)Math.round(centerX-width/2.0),
                (int)Math.round(centerY-height/2.0),width,height);
    }

    //循环播放 Boss 炮弹三帧动画
    @Override
    protected void updateImage(long gameTime) {
        playAnimation("weapon.bossShell", gameTime, 4, true);
    }

    /** 销毁时生成一次爆炸。 */
    @Override
    public void die() {
        //已经爆炸时直接返回
        if(exploded) {
            return;
        }
        exploded=true;
        int cx = getX() + getW() / 2;
        int cy = getY() + getH() / 2;
        ElementManager.getManager().addElement(
                ExplosionEffect.large(cx, cy), GameElement.EFFECT);
    }
}
