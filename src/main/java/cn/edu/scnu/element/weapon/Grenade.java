package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.effect.ExplosionEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

public class Grenade extends ProjectileObj {
    private static final double GRAVITY = 0.6;

    private double preciseX;
    private double preciseY;
    private double vx;
    private double vy;
    private boolean exploded;
    private int fuse;

    public Grenade(int x, int y, int dir, int attack) {
        super(x, y, 15, 18, GameLoad.getImages("weapon.grenade").get(0), attack);
        this.preciseX = x;
        this.preciseY = y;
        this.vx = dir * 5;
        this.vy = -4;
        this.fuse = 120;
    }

    @Override
    protected void move() {
        preciseX += vx;
        preciseY += vy;
        vy += GRAVITY;
        setX((int) Math.round(preciseX));
        setY((int) Math.round(preciseY));

        fuse--;
        int groundY = GameLoad.getInt("player.groundY");
        if (fuse <= 0 || getY() >= groundY) {
            explode();
        }
        checkWorldBounds();
    }

    private void explode() {
        if (exploded) return;
        exploded = true;
        int cx = getX() + getW() / 2;
        int cy = getY() + getH() / 2;
        ElementManager.getManager().addElement(
                new ExplosionEffect(cx, cy), GameElement.EFFECT);
        setLive(false);
    }

    @Override
    public void die() {
        explode();
    }

    /** 爆炸半径（像素），供 A 接入范围伤害 */
    public int getBlastRadius() {
        return 80;
    }

    /** 爆炸伤害，供 A 接入范围伤害 */
    public int getBlastDamage() {
        return getAttack() * 2;
    }
}
