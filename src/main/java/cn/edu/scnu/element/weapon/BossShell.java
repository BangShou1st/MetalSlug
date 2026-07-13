package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.effect.ExplosionEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import java.awt.*;

/**
 * Boss 炮弹，3 帧循环动画，朝左飞行，朝右时水平翻转绘制。
 *
 * @author B
 */
public class BossShell extends ProjectileObj {
    private int vx;

    /**
     * @param x      出生 x
     * @param y      出生 y
     * @param dir    朝向（1 右，-1 左）
     * @param attack 伤害值（应高于普通敌方子弹）
     */
    public BossShell(int x, int y, int dir, int attack) {
        super(x, y, 40, 16, GameLoad.getImages("weapon.bossShell").get(0), attack);
        this.vx = dir * 4;
    }

    @Override
    public void showElement(Graphics g) {
        // 使用负宽度的 drawImage 实现水平翻转，避免额外翻转变量
        if (vx < 0) {
            g.drawImage(getIcon().getImage(), getX() + getW(), getY(), -getW(), getH(), null);
        } else {
            g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
        }
    }

    @Override
    protected void move() {
        setX(getX() + vx);
        checkWorldBounds();
    }

    @Override
    protected void updateImage(long gameTime) {
        playAnimation("weapon.bossShell", gameTime, 4, true);
    }

    /**
     * 销毁时在弹体中心生成 ExplosionEffect。
     * 当前命中、越界均触发爆炸，若后续需要越界不爆炸，需在此处判断死亡原因。
     * TODO: 确认越界时是否需要抑制爆炸
     */
    @Override
    public void die() {
        int cx = getX() + getW() / 2;
        int cy = getY() + getH() / 2;
        ElementManager.getManager().addElement(
                new ExplosionEffect(cx, cy), GameElement.EFFECT);
    }
}
