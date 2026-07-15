package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.GameAudio;

import java.awt.*;

/** 短暂存在且不绘制的近战判定框。 */
public class MeleeHitBox extends ElementObj {
    private int attack;
    /** 剩余存活帧数。 */
    private int life;

    public MeleeHitBox(int x, int y, int attack) {
        this(x,y,40,30,attack);
    }

    //按攻击者缩放后的身体尺寸创建近战判定框
    public MeleeHitBox(int x,int y,int width,int height,int attack) {
        super(x,y,width,height,null);
        this.attack = attack;
        this.life = 2;
        GameAudio.playIfIdle("weapon.melee");
    }

    @Override
    public void showElement(Graphics g) {
    }

    @Override
    protected void move() {
        life--;
        //存活帧数用尽后移除判定框
        if (life <= 0) {
            setLive(false);
        }
    }

    @Override
    public int getAttack() {
        return attack;
    }
}
