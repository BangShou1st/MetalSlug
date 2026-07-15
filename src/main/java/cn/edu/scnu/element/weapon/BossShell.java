package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.RoleObj;
import cn.edu.scnu.element.effect.ExplosionEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import java.awt.*;

/**
 * Boss 炮弹，3 帧循环动画，朝左飞行，朝右时水平翻转绘制。
 *
 * @author B
 */
public class BossShell extends ProjectileObj {
    private static final double SPEED=6.0; //每逻辑帧固定飞行速度
    private static final double HOMING_FACTOR=0.12; //每帧转向目标方向的比例
    private static final int MAX_LIFE_FRAMES=160; //最长存活逻辑帧
    private final ElementObj target; //发射时锁定的玩家对象
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
        this.target=target;
        preciseX=x;
        preciseY=y;
        double[] direction=getDesiredDirection();
        vx=direction[0]*SPEED;
        vy=direction[1]*SPEED;
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

    //平滑修正航向，保持恒速并完成二维越界与寿命检查
    @Override
    protected void move() {
        if(isTargetAvailable()) {
            double[] desired=getDesiredDirection();
            vx=vx*(1-HOMING_FACTOR)+desired[0]*SPEED*HOMING_FACTOR;
            vy=vy*(1-HOMING_FACTOR)+desired[1]*SPEED*HOMING_FACTOR;
            normalizeVelocity();
        }
        preciseX+=vx;
        preciseY+=vy;
        setX((int)Math.round(preciseX));
        setY((int)Math.round(preciseY));
        lifeFrames++;
        int verticalMargin=getH()*2;
        if(getX()+getW()<0 || getX()>getWorldWidth()
                || getY()+getH()<-verticalMargin
                || getY()>getWorldHeight()+verticalMargin
                || lifeFrames>=MAX_LIFE_FRAMES) {
            setLive(false);
        }
    }

    //目标仍存活且角色生命值大于零时才继续制导
    private boolean isTargetAvailable() {
        return target!=null && target.isLive()
                && (!(target instanceof RoleObj) || ((RoleObj)target).getHp()>0);
    }

    //计算弹体中心指向目标碰撞框中心的单位向量
    private double[] getDesiredDirection() {
        if(!isTargetAvailable()) {
            return new double[]{-1.0,0.0};
        }
        Rectangle targetBounds=target.getRectangle();
        double dx=targetBounds.getCenterX()-(preciseX+getW()/2.0);
        double dy=targetBounds.getCenterY()-(preciseY+getH()/2.0);
        double length=Math.hypot(dx,dy);
        if(length<0.0001) {
            return new double[]{-1.0,0.0};
        }
        return new double[]{dx/length,dy/length};
    }

    //插值转向后重新归一化，避免炮弹逐渐减速
    private void normalizeVelocity() {
        double length=Math.hypot(vx,vy);
        if(length>0.0001) {
            vx=vx/length*SPEED;
            vy=vy/length*SPEED;
        }
    }

    //读取当前地图世界高度，尚未装配地图时回退到窗口高度
    private int getWorldHeight() {
        java.util.List<ElementObj> maps=ElementManager.getManager()
                .getElementByKey(GameElement.MAPS);
        return maps.isEmpty() ? GameLoad.getInt("window.height") : maps.get(0).getH();
    }

    //返回旋转后可见弹体的轴对齐包围框，避免陡角飞行时碰撞错位
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

    /**
     * 销毁时在弹体中心生成 ExplosionEffect。
     * 当前命中、越界与寿命耗尽均触发一次爆炸。
     */
    @Override
    public void die() {
        if(exploded) {
            return;
        }
        exploded=true;
        int cx = getX() + getW() / 2;
        int cy = getY() + getH() / 2;
        ElementManager.getManager().addElement(
                new ExplosionEffect(cx, cy), GameElement.EFFECT);
    }
}
