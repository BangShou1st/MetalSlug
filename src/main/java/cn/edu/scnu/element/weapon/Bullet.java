package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.manager.GameAudio;
import cn.edu.scnu.manager.GameLoad;

/** 水平飞行的玩家子弹。 */
public class Bullet extends ProjectileObj {
    private int vx; //每逻辑帧水平速度

    /**
     * @param x      出生 x
     * @param y      出生 y
     * @param dir    朝向（1 右，-1 左）
     * @param attack 伤害值，由玩家攻击力传入
     */
    public Bullet(int x, int y, int dir, int attack) {
        super(x, y, 23, 7, GameLoad.getImages("weapon.bullet").get(0), attack);
        this.vx = dir * 10;
        GameAudio.play("weapon.playerFire");
    }

    @Override
    protected void move() {
        //水平移动并检查地图边界
        setX(getX() + vx);
        checkWorldBounds();
    }

}
