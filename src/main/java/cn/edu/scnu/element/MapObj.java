package cn.edu.scnu.element;

import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;

/**
 * 地图对象，负责按照世界坐标绘制关卡背景。
 */
public class MapObj extends ElementObj {
    private int groundY; //当前地图统一地面的世界纵坐标

    //供 GameLoad 通过反射创建地图模板
    public MapObj() {
    }

    //创建具有世界位置、绘制尺寸和统一地面的地图对象
    private MapObj(int x,int y,int w,int h,ImageIcon icon,int groundY) {
        super(x,y,w,h,icon);
        this.groundY=groundY;
    }

    //根据图片键、世界位置和绘制尺寸创建地图对象
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        String imageKey=data[0].trim();
        int x=Integer.parseInt(data[1].trim());
        int y=Integer.parseInt(data[2].trim());
        int width=Integer.parseInt(data[3].trim());
        int height=Integer.parseInt(data[4].trim());
        int groundY=Integer.parseInt(data[5].trim());
        ImageIcon icon=GameLoad.getImage(imageKey);
        return new MapObj(x,y,width,height,icon,groundY);
    }

    //获取玩家在当前地图中的统一落地点
    public int getGroundY() {
        return groundY;
    }

    //按照地图的世界位置和世界尺寸绘制背景
    @Override
    public void showElement(Graphics g) {
        g.drawImage(getIcon().getImage(),getX(),getY(),getW(),getH(),null);
    }
}
