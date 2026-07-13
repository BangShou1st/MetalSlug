package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.manager.GameLoad;

/**
 * 敌方普通子弹，逻辑同玩家子弹，通过 ENEMYFILE 分类区分阵营。
 *
 * @author B
 */
public class EnemyBullet extends ProjectileObj {
    private int vx;

    /**
     * @param x      出生 x
     * @param y      出生 y
     * @param dir    朝向（1 右，-1 左）
     * @param attack 伤害值
     */
    public EnemyBullet(int x, int y, int dir, int attack) {
        super(x, y, 23, 7, GameLoad.getImages("weapon.enemyBullet").get(0), attack);
        this.vx = dir * 6;
    }

    @Override
    protected void move() {
        setX(getX() + vx);
        checkWorldBounds();
    }
}
