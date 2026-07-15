package cn.edu.scnu.element;

import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 所有游戏元素的基类。 */

public abstract class ElementObj {
    private static final Map<ImageIcon,Rectangle> OPAQUE_BOUNDS_CACHE=
            new ConcurrentHashMap<ImageIcon,Rectangle>(); //每张帧图只扫描一次透明边界
    private int x;
    private int y;
    private int w;
    private int h;
    private ImageIcon icon;
    private boolean live = true; //是否仍在游戏中，存活

    //当前动画的播放进度
    private int imageIndex=0;
    private long imageTime=0;
    private String imageKey="";
    private boolean animationEnd=false;

    public ElementObj() {}

    public ElementObj(int x, int y, int w, int h, ImageIcon icon) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.icon = icon;
    }

    /** 绘制元素。 */
    public abstract void showElement(Graphics g);

    /** 接收按键状态，需要输入的元素可重写。 */
    public void keyClick(boolean bl, int key) {
    }

    /** 更新位置，需要移动的元素可重写。 */
    protected void move() {
    }

    /** 按移动、动画、生成对象的顺序更新元素。 */
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

        //动画资源为空时停止播放
        if(images==null || images.isEmpty()) {
            throw new RuntimeException("动画不存在："+key);
        }

        //换帧间隔至少为一帧
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
        //达到间隔后切换下一帧
        if(gameTime-imageTime>=interval) {
            imageTime=gameTime;
            imageIndex++;
            //到达末帧后循环或停留
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
        //扫描所有非透明像素
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
        //镜像时同步翻转透明区域中心
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
        //朝左时水平镜像绘制
        if(mirrored) {
            g.drawImage(frame.getImage(),bounds.x+bounds.width,bounds.y,
                    -bounds.width,bounds.height,null);
        }else {
            g.drawImage(frame.getImage(),bounds.x,bounds.y,
                    bounds.width,bounds.height,null);
        }
    }

    //死亡时的扩展入口
    public void die(){

    }

    public ElementObj createElement(String str) {
        return null;
    }

    /** 返回当前碰撞区域。 */
    public Rectangle getRectangle() {
        return new Rectangle(x,y,w,h);
    }

    /** 判断是否与指定元素碰撞。 */
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
