package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.MapObj;
import cn.edu.scnu.element.effect.ExplosionEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

/**
 * 手雷，抛物线运动，引信结束或落地时触发爆炸。
 * <p>
 * 使用 double 精度累积避免逐帧取整导致的运动偏移。
 *
 * @author B
 */
public class Grenade extends ProjectileObj {
    /** 每逻辑帧重力加速度（像素/帧²） */
    private static final double GRAVITY = 0.6;

    // double 精度坐标消除累积取整误差
    private double preciseX;
    private double preciseY;
    private double vx;
    private double vy;
    /** 防止 {@link #die()} 与 {@link #move()} 重复触发爆炸 */
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
        super(x, y, 15, 18, GameLoad.getImages("weapon.grenade").get(0), attack);
        this.preciseX = x;
        this.preciseY = y;
        this.vx = dir * 5;
        this.vy = -4;   // 初始向上速度
        this.fuse = 120; // 约 6 秒（约 20 个逻辑帧/秒）
    }

    @Override
    protected void move() {
        // 抛物线积分：水平匀速，垂直受重力加速
        preciseX += vx;
        preciseY += vy;
        vy += GRAVITY;
        setX((int) Math.round(preciseX));
        setY((int) Math.round(preciseY));

        fuse--;
        int groundTop=getGroundTop();
        if (fuse <= 0 || getY() >= groundTop) {
            if(getY()>groundTop) {
                setY(groundTop);
                preciseY=groundTop;
            }
            explode();
        }
        // 先检查引信/地面，再检查越界，避免 explode() 设 setLive(false) 后 checkWorldBounds 再做一次无用判断
        checkWorldBounds();
        if(!isLive()) {
            explode();
        }
    }

    //读取当前地图地面并换算为手雷左上角落地位置
    private int getGroundTop() {
        java.util.List<ElementObj> maps=ElementManager.getManager()
                .getElementByKey(GameElement.MAPS);
        if(maps.isEmpty()) {
            return GameLoad.getInt("window.height")-getH();
        }
        MapObj map=(MapObj)maps.get(0);
        return map.getGroundY()-getH();
    }

    /**
     * 执行爆炸：生成 ExplosionEffect 并销毁自身。
     * 此方法被两条路径调用：
     * 1. move() — 引信结束或到达地面
     * 2. die() — moveAndUpdate() 回收死亡对象
     * exploded 守卫保证两条路径不会重复生成。
     */
    private void explode() {
        if (exploded) return;
        exploded = true;
        int cx = getX() + getW() / 2;
        int cy = getY() + getH() / 2;
        ElementManager.getManager().addElement(
                new ExplosionEffect(cx, cy), GameElement.EFFECT);
        setLive(false);
    }

    //碰到敌方目标时进入统一爆炸流程
    public void triggerExplosion() {
        explode();
    }

    //确认爆炸后仅允许主线程领取一次范围伤害结算
    public boolean claimBlastDamage() {
        if(!exploded || blastApplied) {
            return false;
        }
        blastApplied=true;
        return true;
    }

    /**
     * moveAndUpdate() 回收时触发二次爆炸保护。
     * 如果 explode() 已经在 move() 中执行过，守卫静默跳过。
     * 如果 move() 中未到达爆炸条件（例如越界销毁），在此处补发爆炸。
     */
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
}
