package cn.edu.scnu.element.hostage;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.GameAudio;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Rectangle;

//等待玩家碰撞救援，起身后向摄像机左侧逃离的人质
public class Hostage extends ElementObj {
    private static final String IDLE_ANIMATION="hostage.idle"; //人质等待动画键
    private static final String RUN_ANIMATION="hostage.run"; //人质逃跑动画键
    private static final int IDLE_INTERVAL=3; //等待动画换帧间隔
    private static final int RUN_INTERVAL=2; //逃跑动画换帧间隔
    private static final double RUN_SPEED=3.0; //向左逃跑速度
    private HostageState state=HostageState.IDLE; //人质当前行为状态
    private double preciseX; //人质移动使用的精确世界横坐标
    private int escapeTargetX; //当前救援时确定的左侧逃生终点
    private int scalePercent; //人质素材显示缩放百分比

    //供 GameLoad 通过反射创建模板
    public Hostage() {
    }

    //使用等待首帧自然尺寸创建人质
    private Hostage(int x,int y,ImageIcon icon) {
        super(x,y,scalePixels(getOpaqueBounds(icon).width,
                        GameLoad.getInt("sprite.hostage.scalePercent")),
                scalePixels(getOpaqueBounds(icon).height,
                        GameLoad.getInt("sprite.hostage.scalePercent")),icon);
        scalePercent=GameLoad.getInt("sprite.hostage.scalePercent");
        preciseX=x;
    }

    //按 x,y 格式创建人质
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        if(data.length!=2) throw new IllegalArgumentException("人质配置格式应为 x,y");
        return new Hostage(Integer.parseInt(data[0].trim()),Integer.parseInt(data[1].trim()),
                GameLoad.getImages(IDLE_ANIMATION).get(0));
    }

    //玩家接触等待中的人质后播放起身动画，再奔向指定左侧坐标
    public void rescue(int escapeTargetX) {
        //只有等待状态可以触发救援
        if(state==HostageState.IDLE) {
            this.escapeTargetX=escapeTargetX;
            state=HostageState.RESCUING;
            GameAudio.play("voice.hostageThanks");
        }
    }

    //逃跑时向本次救援确定的左侧终点移动
    @Override
    protected void move() {
        //只有逃跑状态需要移动
        if(state!=HostageState.RUNNING) {
            return;
        }
        preciseX-=RUN_SPEED;
        //到达目标位置后完成救援
        if(preciseX<=escapeTargetX) {
            preciseX=escapeTargetX;
            setX(escapeTargetX);
            state=HostageState.SAVED;
            setLive(false);
            return;
        }
        setX((int)Math.round(preciseX));
    }

    //等待时固定首帧，救援时完整起身，随后循环奔跑
    @Override
    protected void updateImage(long gameTime) {
        //等待救援时显示首帧
        if(state==HostageState.IDLE) {
            setIcon(GameLoad.getImages(IDLE_ANIMATION).get(0));
        //获救后先播放起身动画
        }else if(state==HostageState.RESCUING) {
            playAnimation(IDLE_ANIMATION,gameTime,IDLE_INTERVAL,false);
            //起身结束后开始逃跑
            if(isAnimationEnd()) {
                state=HostageState.RUNNING;
            }
        //逃跑时循环播放跑步动画
        }else if(state==HostageState.RUNNING) {
            playAnimation(RUN_ANIMATION,gameTime,RUN_INTERVAL,true);
        }
    }

    //按当前自然帧与等待逻辑框的脚底中心绘制人质
    @Override
    public void showElement(Graphics g) {
        ImageIcon frame=getIcon();
        if(frame==null) {
            return;
        }
        drawFootAnchoredFrame(g,frame,scalePercent,false);
    }

    //返回贴合人质身体的稳定碰撞框
    @Override
    public Rectangle getRectangle() {
        return new Rectangle(getX()+(int)(getW()*0.15),
                getY()+(int)(getH()*0.10),(int)(getW()*0.70),
                (int)(getH()*0.85));
    }

    //获取人质当前行为状态
    public HostageState getState() {
        return state;
    }
}
