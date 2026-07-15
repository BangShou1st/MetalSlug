package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.manager.GameAudio;
import cn.edu.scnu.manager.GameLoad;

import java.awt.Graphics;

/** 水平飞行的敌方子弹。 */
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
        GameAudio.playIfIdle("weapon.machineGun");
    }

    //素材默认朝左
    @Override
    public void showElement(Graphics g) {
        //向右飞行时水平翻转素材
        if(vx>0) {
            g.drawImage(getIcon().getImage(),getX()+getW(),getY(),-getW(),getH(),null);
        }else {
            g.drawImage(getIcon().getImage(),getX(),getY(),getW(),getH(),null);
        }
    }

    //水平移动并检查边界
    @Override
    protected void move() {
        setX(getX() + vx);
        checkWorldBounds();
    }

}
