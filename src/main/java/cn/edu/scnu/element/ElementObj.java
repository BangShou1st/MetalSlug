package cn.edu.scnu.element;

import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @说明 所有元素的基类
 */

public abstract class ElementObj {
    private int x;
    private int y;
    private int w;
    private int h;
    private ImageIcon icon;
    private boolean live = true; //生存状态 true 代表存在，false代表死亡
                                    //可以采用枚举值来定义这个(生存，死亡，隐身，无敌)
    //注明:当重新定义一个用于判定状态的变量，需要思考:1.初始化  2.值的改变  3.值的判定

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

    /**
     * 只要是 VO 类就要为属性生成 get 和 set 方法
     */
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
}
