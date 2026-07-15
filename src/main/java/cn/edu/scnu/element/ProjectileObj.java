package cn.edu.scnu.element;

import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** 发射物基类，负责伤害属性和世界边界判定。 */
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

    //地图未加载时使用窗口宽度
    protected int getWorldWidth() {
        List<ElementObj> maps=ElementManager.getManager()
                .getElementByKey(GameElement.MAPS);
        //没有地图时使用窗口宽度
        if(maps.isEmpty()) {
            return GameLoad.getInt("window.width");
        }
        return maps.get(0).getW();
    }

    //离开地图边界后失效
    protected void checkWorldBounds() {
        //发射物整体离开左右边界后移除
        if (getX() + getW() < 0 || getX() > getWorldWidth()) {
            setLive(false);
        }
    }
}
