package cn.edu.scnu.element;

import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @说明 所有元素的基类
 */

public abstract class ElementObj {
    private static final Map<ImageIcon,Rectangle> OPAQUE_BOUNDS_CACHE=
            new ConcurrentHashMap<ImageIcon,Rectangle>(); //每张帧图只扫描一次透明边界
    private int x;
    private int y;
    private int w;
    private int h;
    private ImageIcon icon;
    private boolean live = true; //生存状态 true 代表存在，false代表死亡

    //动画播放数据，每个元素都有自己独立的播放进度
    private int imageIndex=0;
    private long imageTime=0;
    private String imageKey="";
    private boolean animationEnd=false;

    public ElementObj() {}

    /**
     * @说明 带参数的构造方法;可以由子类传输数据到父类
     */
    public ElementObj(int x, int y, int w, int h, ImageIcon icon) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.icon = icon;
    }

    /**
     * @param g 画笔 用于进行绘画
     * @说明 抽象方法，显示元素
     */
    public abstract void showElement(Graphics g);

    /**
     * @param bl  点击的类型true代表按下，false代表松开
     * @param key 代表触发的键盘的code值
     * @说明 使用父类定义接收键盘事件的方法
     * 只有需要实现键盘监听的子类，重写这个方法(约定)
     * @说明 方式2：使用接口的方式;使用接口方式需要在监听类进行类型转换
     */
    //这个方法不是强制必须重写的
    public void keyClick(boolean bl, int key) {
    }

    /**
     * @说明 移动方法;需要移动的子类，请重写实现这个方法
     */
    protected void move() {
    }

    /**
     * @设计模式 模板模式;在模板模式中定义  对象执行方法的先后顺序，由子类选择性重写方法
     * 1.移动     2.换装    3.子弹发射
     */
    public final void model(long gameTime) {
        move();
        updateImage(gameTime);
        add(gameTime);
    }

    //更新当前图片，需要动画的子类重写
    protected void updateImage(long gameTime) {
    }

    /**
     * 播放图片序列
     * @param key 图片序列名称
     * @param gameTime 游戏时间
     * @param interval 换帧间隔 游戏逻辑帧数 每 n次逻辑更新切换一帧
     * @param loop 是否循环
     */
    protected void playAnimation(String key,long gameTime,int interval,boolean loop) {

        List<ImageIcon> images= GameLoad.getImages(key);

        if(images==null || images.isEmpty()) {
            throw new RuntimeException("动画不存在："+key);
        }

        if(interval<1) {
            interval=1;
        }

        //切换了动画，重新从第一帧播放
        if(!key.equals(imageKey)) {
            imageKey=key;
            imageIndex=0;
            imageTime=gameTime;
            animationEnd=false;
        }
        if(gameTime-imageTime>=interval) {
            imageTime=gameTime;
            imageIndex++;
            if(imageIndex>=images.size()) {
                if(loop) {
                    imageIndex=0;
                }else {
                    imageIndex=images.size()-1;
                    animationEnd=true;
                }
            }
        }
        setIcon(images.get(imageIndex));
    }

    //非循环动画是否播放结束
    protected boolean isAnimationEnd() {
        return animationEnd;
    }

    //当前动画播放到第几帧
    protected int getImageIndex() {
        return imageIndex;
    }

    protected void add(long gameTime) {
    }

    //返回图片中 alpha 大于零的最小矩形，并缓存扫描结果
    protected static Rectangle getOpaqueBounds(ImageIcon frame) {
        return new Rectangle(OPAQUE_BOUNDS_CACHE.computeIfAbsent(
                frame,ElementObj::scanOpaqueBounds));
    }

    //首次访问帧图时扫描并生成不可变用途的透明边界值
    private static Rectangle scanOpaqueBounds(ImageIcon frame) {
        int width=frame.getIconWidth();
        int height=frame.getIconHeight();
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics=image.createGraphics();
        try {
            graphics.drawImage(frame.getImage(),0,0,null);
        }finally {
            graphics.dispose();
        }
        int minX=width;
        int minY=height;
        int maxX=-1;
        int maxY=-1;
        for(int y=0;y<height;y++) {
            for(int x=0;x<width;x++) {
                if((image.getRGB(x,y)>>>24)!=0) {
                    minX=Math.min(minX,x);
                    minY=Math.min(minY,y);
                    maxX=Math.max(maxX,x);
                    maxY=Math.max(maxY,y);
                }
            }
        }
        return maxX<0 ? new Rectangle(0,0,width,height)
                : new Rectangle(minX,minY,maxX-minX+1,maxY-minY+1);
    }

    //按百分比缩放像素，确保非空尺寸至少为一像素
    protected static int scalePixels(int pixels,int scalePercent) {
        return Math.max(1,(int)Math.round(pixels*scalePercent/100.0));
    }

    //根据逻辑框脚底中心计算当前帧整张 PNG 的绘制区域
    protected Rectangle getFootAnchoredDrawBounds(ImageIcon frame,
                                                   int scalePercent,
                                                   boolean mirrored) {
        Rectangle opaque=getOpaqueBounds(frame);
        double scale=scalePercent/100.0;
        double opaqueCenter=opaque.x+opaque.width/2.0;
        if(mirrored) {
            opaqueCenter=frame.getIconWidth()-opaqueCenter;
        }
        int drawWidth=scalePixels(frame.getIconWidth(),scalePercent);
        int drawHeight=scalePixels(frame.getIconHeight(),scalePercent);
        int drawX=(int)Math.round(getX()+getW()/2.0-opaqueCenter*scale);
        int drawY=(int)Math.round(getY()+getH()
                -(opaque.y+opaque.height)*scale);
        return new Rectangle(drawX,drawY,drawWidth,drawHeight);
    }

    //以透明边界的脚底中心为锚点绘制当前帧
    protected void drawFootAnchoredFrame(Graphics g,ImageIcon frame,
                                         int scalePercent,boolean mirrored) {
        Rectangle bounds=getFootAnchoredDrawBounds(frame,scalePercent,mirrored);
        if(mirrored) {
            g.drawImage(frame.getImage(),bounds.x+bounds.width,bounds.y,
                    -bounds.width,bounds.height,null);
        }else {
            g.drawImage(frame.getImage(),bounds.x,bounds.y,
                    bounds.width,bounds.height,null);
        }
    }

    //死亡方法 给子类继承的
    public void die(){ //死亡也是一个对象

    }

    public ElementObj createElement(String str) {
        return null;
    }

    /**
     * @说明 本方法返回元素的碰撞矩形对象(实时返回)
     */
    public Rectangle getRectangle() {
        //可以将这个数据进行处理(碰撞面积)
        return new Rectangle(x,y,w,h);
    }

    /**
     * @说明 碰撞方法
     * 一个是this对象一个是传入值obj
     * boolean返回true 说明有碰撞，返回false说明没有碰撞
     */
    public boolean pk(ElementObj obj) {
        return this.getRectangle().intersects(obj.getRectangle());
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    //受到伤害，需要生命值的子类重写
    public void hurt(int damage) {
    }

    //获取攻击力，没有攻击力的元素默认为0
    public int getAttack() {
        return 0;
    }
}
