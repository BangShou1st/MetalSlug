package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.effect.ExplosionEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import java.awt.*;

public class BossShell extends ProjectileObj {
    private int vx;

    public BossShell(int x, int y, int dir, int attack) {
        super(x, y, 40, 16, GameLoad.getImages("weapon.bossShell").get(0), attack);
        this.vx = dir * 4;
    }

    @Override
    public void showElement(Graphics g) {
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

    @Override
    public void die() {
        int cx = getX() + getW() / 2;
        int cy = getY() + getH() / 2;
        ElementManager.getManager().addElement(
                new ExplosionEffect(cx, cy), GameElement.EFFECT);
    }
}
