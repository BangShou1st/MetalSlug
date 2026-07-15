package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ElementObj;

import java.awt.*;

/**
 * 近战判定框，短暂存在后自动消失，供 C 的刀兵等近战敌人使用。
 * <p>
 * 无绘制内容（{{@code showElement}} 为空），碰撞框由 {@link #getRectangle()} 提供。
 *
 * @author B
 */
public class MeleeHitBox extends ElementObj {
    private int attack;
    /** 剩余存活帧数，2 帧足够覆盖一次碰撞检测周期 */
    private int life;

    public MeleeHitBox(int x, int y, int attack) {
        this(x,y,40,30,attack);
    }

    //按攻击者缩放后的身体尺寸创建近战判定框
    public MeleeHitBox(int x,int y,int width,int height,int attack) {
        super(x,y,width,height,null);
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
