package cn.edu.scnu.element;

import javax.swing.*;

/**
 * 战斗角色父类    玩家、敌人和Boss继承本类
 */
public abstract class RoleObj extends ElementObj {

    private int hp=1;       //生命值
    private int attack=0;   //攻击力

    public RoleObj() {
    }

    public RoleObj(int x,int y,int w,int h,ImageIcon icon, int hp,int attack) {
        super(x,y,w,h,icon);
        this.hp=hp;
        this.attack=attack;
    }

    //受到伤害
    @Override
    public void hurt(int damage) {
        hp-=damage;
        if(hp<=0) {
            hp=0;
            setLive(false);
        }
    }

    @Override
    public int getAttack() {
        return attack;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp=hp;
    }

    public void setAttack(int attack) {
        this.attack=attack;
    }
}