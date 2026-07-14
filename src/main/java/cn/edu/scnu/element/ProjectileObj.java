package cn.edu.scnu.element;

import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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

    //获取当前地图的真实世界宽度，地图尚未加载时回退到窗口宽度
    protected int getWorldWidth() {
        List<ElementObj> maps=ElementManager.getManager()
                .getElementByKey(GameElement.MAPS);
        if(maps.isEmpty()) {
            return GameLoad.getInt("window.width");
        }
        return maps.get(0).getW();
    }

    //发射物整体离开当前地图的真实世界边界时失效
    protected void checkWorldBounds() {
        if (getX() + getW() < 0 || getX() > getWorldWidth()) {
            setLive(false);
        }
    }
}
