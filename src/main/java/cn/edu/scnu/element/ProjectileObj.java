package cn.edu.scnu.element;

import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;

/**
 * 发射物基类，集中 attack 字段和世界越界判定，减少 weapon 子类重复代码。
 *
 * @author B
 */
public abstract class ProjectileObj extends ElementObj {
    private int attack;

    /**
     * @param x      出生 x（世界坐标）
     * @param y      出生 y（世界坐标）
     * @param w      碰撞箱宽度
     * @param h      碰撞箱高度
     * @param icon   初始图片
     * @param attack 命中伤害
     */
    public ProjectileObj(int x, int y, int w, int h, ImageIcon icon, int attack) {
        super(x, y, w, h, icon);
        this.attack = attack;
    }

    @Override
    public void showElement(Graphics g) {
        g.drawImage(getIcon().getImage(), getX(), getY(), getW(), getH(), null);
    }

    @Override
    public int getAttack() {
        return attack;
    }

    /**
     * 超出世界边界时自杀。
     * -200 而非 0 是为左侧预留一个缓冲带，避免弹体刚好卡在边界反复生灭。
     * TODO: 等待 A 提供统一 worldWidth 后替换 window.width * 3
     */
    protected void checkWorldBounds() {
        if (getX() < -200 || getX() > GameLoad.getInt("window.width") * 3) {
            setLive(false);
        }
    }
}
