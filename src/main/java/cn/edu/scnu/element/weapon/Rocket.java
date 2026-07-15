package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.effect.ExplosionEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import java.awt.Graphics;

/**
 * 火箭弹，3 帧循环动画，命中/死亡时在中心生成 ExplosionEffect。
 *
 * @author B
 */
public class Rocket extends ProjectileObj {
    private int vx; //火箭弹每帧的水平位移
    /**
     * 防止 die() 重复生成爆炸。
     * Rocket 的 die() 只被 moveAndUpdate() 调用一次（碰撞或越界二选一），
     * 守卫的存在是为了与 Grenade 的同一模式保持风格一致，避免未来重构引入 bug。
     */
    private boolean exploded;

    /**
     * @param x      出生 x
     * @param y      出生 y
     * @param dir    朝向（1 右，-1 左）
     * @param attack 伤害值
     */
    public Rocket(int x, int y, int dir, int attack) {
        super(x, y, 60, 19, GameLoad.getImages("weapon.rocket").get(0), attack);
        this.vx = dir * 8;
    }

    //按照飞行方向绘制火箭弹，素材默认朝右
    @Override
    public void showElement(Graphics g) {
        if(vx<0) {
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

    //循环播放火箭弹三帧动画
    @Override
    protected void updateImage(long gameTime) {
        playAnimation("weapon.rocket", gameTime, 4, true);
    }

    /**
     * 销毁时在弹体中心生成 ExplosionEffect。
     * exploded 守卫确保爆炸只触发一次，参见 {@link #exploded} 字段说明。
     */
    @Override
    public void die() {
        if (!exploded) {
            exploded = true;
            int cx = getX() + getW() / 2;
            int cy = getY() + getH() / 2;
            ElementManager.getManager().addElement(
                    new ExplosionEffect(cx, cy), GameElement.EFFECT);
        }
    }
}
