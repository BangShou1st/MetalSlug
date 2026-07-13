package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ElementObj;

import java.awt.*;

public class MeleeHitBox extends ElementObj {
    private int attack;
    private int life;

    public MeleeHitBox(int x, int y, int attack) {
        super(x, y, 40, 30, null);
        this.attack = attack;
        this.life = 2;
    }

    @Override
    public void showElement(Graphics g) {
    }

    @Override
    protected void move() {
        life--;
        if (life <= 0) {
            setLive(false);
        }
    }

    @Override
    public int getAttack() {
        return attack;
    }
}
