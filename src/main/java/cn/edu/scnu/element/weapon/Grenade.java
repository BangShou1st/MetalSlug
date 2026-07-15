package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.MapObj;
import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.effect.ExplosionEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameAudio;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

/** 沿抛物线运动并在引信结束或落地时爆炸的手雷。 */
public class Grenade extends ProjectileObj {
    /** 每逻辑帧重力加速度（像素/帧²） */
    private static final double GRAVITY = 0.6;
    private static final double MAX_AIMED_HORIZONTAL_SPEED=22.0; //覆盖九百像素视口的瞄准手雷最大水平速度

    private double preciseX; //手雷精确横坐标
    private double preciseY; //手雷精确纵坐标
    private double vx; //手雷水平速度
    private double vy; //手雷纵向速度
    private double angle; //手雷当前旋转角度
    private double angularVelocity; //手雷每逻辑帧旋转角速度
    /** 防止重复爆炸。 */
    private boolean exploded;
    private boolean blastApplied; //本次爆炸范围伤害是否已经结算
    /** 引信剩余逻辑帧数 */
    private int fuse;

    /**
     * @param x      出生 x
     * @param y      出生 y
     * @param dir    朝向（1 右，-1 左）
     * @param attack 直接命中伤害（范围伤害见 {@link #getBlastDamage()}）
     */
    public Grenade(int x, int y, int dir, int attack) {
        this(x,y,dir*5,-4,attack);
    }

    //使用给定初速度创建一枚手雷
    private Grenade(int x,int y,double vx,double vy,int attack) {
        super(x, y, 15, 18, GameLoad.getImages("weapon.grenade").get(0), attack);
        this.preciseX = x;
        this.preciseY = y;
        this.vx = vx;
        this.vy = vy;
        this.angularVelocity = Math.copySign(18.0,vx==0 ? 1.0 : vx);
        this.fuse = 120; // 约 6 秒（约 20 个逻辑帧/秒）
        GameAudio.playIfIdle("weapon.grenadeThrow");
    }

    //按目标位置计算抛物线初速度
    public static Grenade aimed(int startX,int startY,int targetX,
                                int targetY,int attack) {
        double dx=targetX-startX;
        int flightFrames=Math.max(18,Math.min(42,
                (int)Math.ceil(Math.abs(dx)/6.0)));
        double aimedVx=dx/flightFrames;
        //目标较近时仍保留最低水平速度
        if(Math.abs(aimedVx)<3.0 && Math.abs(dx)>0.001) {
            aimedVx=Math.copySign(3.0,aimedVx);
        }
        aimedVx=Math.max(-MAX_AIMED_HORIZONTAL_SPEED,
                Math.min(MAX_AIMED_HORIZONTAL_SPEED,aimedVx));
        double aimedVy=(targetY-startY
                -GRAVITY*flightFrames*(flightFrames-1)/2.0)/flightFrames;
        aimedVy=Math.max(-10.0,Math.min(3.0,aimedVy));
        return new Grenade(startX,startY,aimedVx,aimedVy,attack);
    }

    //更新位置、旋转和引信
    @Override
    protected void move() {
        //水平匀速，垂直受重力加速
        preciseX += vx;
        preciseY += vy;
        vy += GRAVITY;
        angle += angularVelocity;
        setX((int) Math.round(preciseX));
        setY((int) Math.round(preciseY));

        fuse--;
        int groundTop=getGroundTop();
        //引信结束或落地时爆炸
        if (fuse <= 0 || getY() >= groundTop) {
            //落到地面以下时修正位置
            if(getY()>groundTop) {
                setY(groundTop);
                preciseY=groundTop;
            }
            explode();
        }
        //越界失效时也补触发爆炸
        checkWorldBounds();
        if(!isLive()) {
            explode();
        }
    }

    //计算手雷落地位置
    private int getGroundTop() {
        List<ElementObj> maps=ElementManager.getManager()
                .getElementByKey(GameElement.MAPS);
        //地图未加载时使用窗口底部
        if(maps.isEmpty()) {
            return GameLoad.getInt("window.height")-getH();
        }
        MapObj map=(MapObj)maps.get(0);
        return map.getGroundY()-getH();
    }

    /** 生成爆炸特效并销毁自身。 */
    private void explode() {
        //已经爆炸时不重复生成特效
        if (exploded) return;
        exploded = true;
        GameAudio.playIfIdle("weapon.grenadeExplosion");
        int cx = getX() + getW() / 2;
        int cy = getY() + getH() / 2;
        ElementManager.getManager().addElement(
                ExplosionEffect.small(cx, cy), GameElement.EFFECT);
        setLive(false);
    }

    //碰到敌方目标时进入统一爆炸流程
    public void triggerExplosion() {
        explode();
    }

    //范围伤害只结算一次
    public boolean claimBlastDamage() {
        //未爆炸或已结算时不再处理
        if(!exploded || blastApplied) {
            return false;
        }
        blastApplied=true;
        return true;
    }

    /** 回收时确保爆炸已触发。 */
    @Override
    public void die() {
        explode();
    }

    //爆炸半径（像素），供主线程执行范围伤害
    public int getBlastRadius() {
        return 80;
    }

    //范围爆炸伤害，供主线程执行范围伤害
    public int getBlastDamage() {
        return getAttack() * 2;
    }

    //围绕手雷中心旋转绘制当前图片
    @Override
    public void showElement(Graphics g) {
        Graphics2D grenadeGraphics=(Graphics2D)g.create();
        try {
            grenadeGraphics.translate(
                    getX()+getW()/2.0,getY()+getH()/2.0);
            grenadeGraphics.rotate(Math.toRadians(angle));
            grenadeGraphics.drawImage(getIcon().getImage(),
                    -getW()/2,-getH()/2,getW(),getH(),null);
        }finally {
            grenadeGraphics.dispose();
        }
    }
}
