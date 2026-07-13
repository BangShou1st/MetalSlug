package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.manager.GameLoad;

public class Bullet extends ProjectileObj {
    private int vx;

    public Bullet(int x, int y, int dir, int attack) {
        super(x, y, 23, 7, GameLoad.getImages("weapon.bullet").get(0), attack);
        this.vx = dir * 10;
    }

    @Override
    protected void move() {
        setX(getX() + vx);
        checkWorldBounds();
    }
}
