package cn.edu.scnu.element.weapon;

import cn.edu.scnu.element.ProjectileObj;
import cn.edu.scnu.element.effect.ExplosionEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameAudio;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** 直线飞行并在销毁时爆炸的火箭弹。 */
public class Rocket extends ProjectileObj {
    private static final double SPEED=8.0; //火箭弹每逻辑帧固定飞行速度
    private double preciseX; //火箭弹精确横坐标
    private double preciseY; //火箭弹精确纵坐标
    private double vx; //火箭弹水平速度分量
    private double vy; //火箭弹垂直速度分量
    /** 防止重复爆炸。 */
    private boolean exploded;

    /**
     * @param x      出生 x
     * @param y      出生 y
     * @param dir    朝向（1 右，-1 左）
     * @param attack 伤害值
     */
    public Rocket(int x, int y, int dir, int attack) {
        this(x,y,dir*SPEED,0.0,attack);
    }

    //按指定速度创建火箭弹
    private Rocket(int x,int y,double vx,double vy,int attack) {
        super(x,y,rocketImage().getIconWidth(),rocketImage().getIconHeight(),
                rocketImage(),attack);
        preciseX=x;
        preciseY=y;
        this.vx=vx;
        this.vy=vy;
        GameAudio.playIfIdle("weapon.rocket");
    }

    //创建瞄准目标的火箭弹
    public static Rocket aimed(int startX,int startY,int targetX,
                               int targetY,int attack) {
        ImageIcon image=rocketImage();
        int left=(int)Math.round(startX-image.getIconWidth()/2.0);
        int top=(int)Math.round(startY-image.getIconHeight()/2.0);
        double dx=targetX-startX;
        double dy=targetY-startY;
        double length=Math.hypot(dx,dy);
        //目标与起点重合时默认向右飞行
        if(length<0.0001) {
            return new Rocket(left,top,SPEED,0.0,attack);
        }
        return new Rocket(left,top,dx/length*SPEED,
                dy/length*SPEED,attack);
    }

    //使用固定火箭图片
    private static ImageIcon rocketImage() {
        return GameLoad.getImage("weapon.rocket");
    }

    //素材默认朝右
    @Override
    public void showElement(Graphics g) {
        Graphics2D rocketGraphics=(Graphics2D)g.create();
        try {
            double angle=Math.atan2(vy,vx);
            rocketGraphics.translate(getX()+getW()/2.0,getY()+getH()/2.0);
            rocketGraphics.rotate(angle);
            rocketGraphics.drawImage(getIcon().getImage(),-getW()/2,-getH()/2,
                    getW(),getH(),null);
        }finally {
            rocketGraphics.dispose();
        }
    }

    //直线移动并检查地图边界
    @Override
    protected void move() {
        preciseX+=vx;
        preciseY+=vy;
        setX((int)Math.round(preciseX));
        setY((int)Math.round(preciseY));
        //离开地图边界后失效
        if(getX()+getW()<0 || getX()>getWorldWidth()
                || getY()+getH()<0 || getY()>getWorldHeight()) {
            setLive(false);
        }
    }

    //读取当前地图真实世界高度，地图缺失时回退窗口高度
    private int getWorldHeight() {
        java.util.List<cn.edu.scnu.element.ElementObj> maps=
                ElementManager.getManager().getElementByKey(GameElement.MAPS);
        return maps.isEmpty() ? GameLoad.getInt("window.height")
                : maps.get(0).getH();
    }

    //返回旋转后的碰撞区域
    @Override
    public Rectangle getRectangle() {
        double angle=Math.atan2(vy,vx);
        int width=Math.max(1,(int)Math.ceil(Math.abs(getW()*Math.cos(angle))
                +Math.abs(getH()*Math.sin(angle))));
        int height=Math.max(1,(int)Math.ceil(Math.abs(getW()*Math.sin(angle))
                +Math.abs(getH()*Math.cos(angle))));
        double centerX=getX()+getW()/2.0;
        double centerY=getY()+getH()/2.0;
        return new Rectangle((int)Math.round(centerX-width/2.0),
                (int)Math.round(centerY-height/2.0),width,height);
    }

    //火箭使用单帧图片
    @Override
    protected void updateImage(long gameTime) {
    }

    /** 销毁时生成一次爆炸。 */
    @Override
    public void die() {
        //每枚火箭只生成一次爆炸
        if (!exploded) {
            exploded = true;
            int cx = getX() + getW() / 2;
            int cy = getY() + getH() / 2;
            ElementManager.getManager().addElement(
                    ExplosionEffect.large(cx, cy), GameElement.EFFECT);
        }
    }
}
