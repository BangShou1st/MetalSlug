package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.effect.HitEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import java.awt.Graphics;

/**
 * 敌方普通子弹，逻辑同玩家子弹，通过 ENEMYFILE 分类区分阵营。
 *
 * @author B
 */
public class EnemyBullet extends ProjectileObj {
    private int vx; //敌方子弹每帧的水平位移

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

    //按照飞行方向绘制敌方子弹，素材默认朝左
    @Override
    public void showElement(Graphics g) {
        if(vx>0) {
            g.drawImage(getIcon().getImage(),getX()+getW(),getY(),-getW(),getH(),null);
        }else {
            g.drawImage(getIcon().getImage(),getX(),getY(),getW(),getH(),null);
        }
    }

    //按当前水平速度移动并检查世界边界
    @Override
    protected void move() {
        setX(getX() + vx);
        checkWorldBounds();
    }

    @Override
    public void die() {
        if (getX() >= -200 && getX() <= GameLoad.getInt("window.width") * 3) {
            int cx = getX() + getW() / 2;
            int cy = getY() + getH() / 2;
            ElementManager.getManager().addElement(new HitEffect(cx, cy), GameElement.EFFECT);
        }
    }
}
