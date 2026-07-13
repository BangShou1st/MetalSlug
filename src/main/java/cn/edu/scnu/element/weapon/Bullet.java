package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.manager.GameLoad;

/**
 * 玩家普通子弹，水平直线飞行，加入 PLAYFILE 分类。
 *
 * @author B
 */
public class Bullet extends ProjectileObj {
    private int vx;

    /**
     * @param x      出生 x
     * @param y      出生 y
     * @param dir    朝向（1 右，-1 左）
     * @param attack 伤害值，由玩家攻击力传入
     */
    public Bullet(int x, int y, int dir, int attack) {
        super(x, y, 23, 7, GameLoad.getImages("weapon.bullet").get(0), attack);
        this.vx = dir * 10;
    }

    @Override
    protected void move() {
        setX(getX() + vx);
        checkWorldBounds();
    }
}
