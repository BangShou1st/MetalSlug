package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.effect.ExplosionEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

public class Rocket extends ProjectileObj {
    private int vx;
    private boolean exploded;

    public Rocket(int x, int y, int dir, int attack) {
        super(x, y, 60, 19, GameLoad.getImages("weapon.rocket").get(0), attack);
        this.vx = dir * 8;
    }

    @Override
    protected void move() {
        setX(getX() + vx);
        checkWorldBounds();
    }

    @Override
    protected void updateImage(long gameTime) {
        playAnimation("weapon.rocket", gameTime, 4, true);
    }

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
