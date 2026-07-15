package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.effect.HitEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
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

    @Override
    public void die() {
        // die() 时子弹坐标仍在有效范围内说明由碰撞（elementPk）导致死亡，生成命中特效
        if (getX() >= -200 && getX() <= GameLoad.getInt("window.width") * 3) {
            int cx = getX() + getW() / 2;
            int cy = getY() + getH() / 2;
            ElementManager.getManager().addElement(new HitEffect(cx, cy), GameElement.EFFECT);
        }
    }
}
