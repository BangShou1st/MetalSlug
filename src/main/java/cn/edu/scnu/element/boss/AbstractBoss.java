package cn.edu.scnu.element.boss;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.RoleObj;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Rectangle;

//Boss 公共基类，负责距离激活、逻辑帧冷却、唯一攻击释放和延迟移除
public abstract class AbstractBoss extends RoleObj {
    private static final int WRECK_DISPLAY_FRAMES=40; //残骸持续显示的逻辑帧数
    protected boolean active; //Boss 是否已经进入战斗状态
    protected BossState state=BossState.IDLE; //Boss 当前行为状态
    protected boolean facingRight; //Boss 是否朝向右侧
    protected int attackCooldownFrames; //剩余攻击冷却逻辑帧数
    protected boolean attackReleased; //当前攻击动画是否已经释放炮弹
    protected int activationRange; //玩家触发 Boss 战斗的水平范围
    protected int attackRange; //Boss 开始攻击的水平范围
    protected int wreckFrames; //残骸已经显示的逻辑帧数
    protected int scalePercent; //Boss 素材显示缩放百分比

    //供具体 Boss 的无参构造调用
    protected AbstractBoss() {
    }

    //根据待机首帧自然尺寸创建 Boss 逻辑框
    protected AbstractBoss(int x,int y,ImageIcon icon,int hp,int attack,
                           int scalePercent) {
        super(x,y,scalePixels(getOpaqueBounds(icon).width,scalePercent),
                scalePixels(getOpaqueBounds(icon).height,scalePercent),
                icon,hp,attack);
        this.scalePercent=scalePercent;
    }

    //获取待机动画键
    protected abstract String getIdleAnimation();

    //获取攻击动画键
    protected abstract String getAttackAnimation();

    //获取残骸图片键
    protected abstract String getWreckAnimation();

    //获取待机动画换帧间隔
    protected abstract int getIdleInterval();

    //获取攻击动画换帧间隔
    protected abstract int getAttackInterval();

    //获取唯一炮弹释放帧
    protected abstract int getAttackFrame();

    //获取攻击结束后的冷却逻辑帧数
    protected abstract int getAttackCooldownFrames();

    //按当前素材自然尺寸和逻辑框脚底中心绘制 Boss
    @Override
    public void showElement(Graphics g) {
        if(getIcon()!=null) {
            drawFootAnchoredFrame(g,getIcon(),scalePercent,facingRight);
        }
    }

    //返回当前自然帧以逻辑框脚底中心锚定后的绘制区域
    protected Rectangle getCurrentDrawBounds() {
        return getFootAnchoredDrawBounds(getIcon(),scalePercent,facingRight);
    }

    //查找玩家并在进入激活范围后更新朝向与逻辑帧冷却
    @Override
    protected void move() {
        if(state==BossState.DEAD) {
            return;
        }
        ElementObj player=findPlayer();
        if(player==null) {
            return;
        }
        int distanceX=player.getX()-getX();
        int absoluteDistanceX=Math.abs(distanceX);
        if(!active && absoluteDistanceX<=activationRange) {
            active=true;
        }
        if(!active) {
            return;
        }
        facingRight=distanceX>0;
        if(attackCooldownFrames>0) {
            attackCooldownFrames--;
        }
        if(state==BossState.IDLE && attackCooldownFrames==0
                && absoluteDistanceX<=attackRange) {
            state=BossState.ATTACK;
            attackReleased=false;
        }
    }

    //按当前状态播放待机、攻击动画或显示单帧残骸
    @Override
    protected void updateImage(long gameTime) {
        if(state==BossState.DEAD) {
            setIcon(GameLoad.getImages(getWreckAnimation()).get(0));
        }else if(state==BossState.ATTACK) {
            playAnimation(getAttackAnimation(),gameTime,getAttackInterval(),false);
        }else {
            playAnimation(getIdleAnimation(),gameTime,getIdleInterval(),true);
        }
    }

    //处理 40 帧残骸、唯一炮弹释放帧和攻击结束后的冷却
    @Override
    protected void add(long gameTime) {
        if(state==BossState.DEAD) {
            wreckFrames++;
            if(wreckFrames>=WRECK_DISPLAY_FRAMES) {
                setLive(false);
            }
            return;
        }
        if(!active || state!=BossState.ATTACK) {
            return;
        }
        if(!attackReleased && getImageIndex()==getAttackFrame()) {
            attackReleased=true;
            releaseAttack();
        }
        if(isAnimationEnd()) {
            attackCooldownFrames=getAttackCooldownFrames();
            attackReleased=false;
            state=BossState.IDLE;
        }
    }

    //由具体 Boss 在唯一攻击帧创建成员 B 的 BossShell
    protected void releaseAttack() {
    }

    //扣除生命值并进入仍保持存活的残骸显示状态
    @Override
    public void hurt(int damage) {
        if(state==BossState.DEAD) {
            return;
        }
        setHp(Math.max(0,getHp()-damage));
        if(getHp()==0) {
            state=BossState.DEAD;
            attackReleased=false;
            attackCooldownFrames=0;
            wreckFrames=0;
        }
    }

    //从唯一元素管理器中查找第一个仍可战斗的玩家
    protected ElementObj findPlayer() {
        for(ElementObj player:ElementManager.getManager()
                .getElementByKey(GameElement.PLAY)) {
            if(player.isLive() && (!(player instanceof RoleObj)
                    || ((RoleObj)player).getHp()>0)) {
                return player;
            }
        }
        return null;
    }

    //返回贴合 Boss 主体的稳定碰撞框
    @Override
    public Rectangle getRectangle() {
        return new Rectangle(getX()+(int)(getW()*0.10),
                getY()+(int)(getH()*0.05),(int)(getW()*0.80),
                (int)(getH()*0.90));
    }

    //判断 Boss 是否已经进入战斗
    public boolean isActive() {
        return active;
    }
}
