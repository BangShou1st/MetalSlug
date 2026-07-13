package cn.edu.scnu.element;

import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;

/**
 * 发射物轻量基类 — 集中 attack/worldBounds，减少各 weapon 子类重复代码。
 */
public abstract class ProjectileObj extends ElementObj {
    private int attack;

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

    /** 越界判定，供子类 move() 调用 */
    protected void checkWorldBounds() {
        if (getX() < -200 || getX() > GameLoad.getInt("window.width") * 3) {
            setLive(false);
        }
    }
}
