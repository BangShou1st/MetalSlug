package cn.edu.scnu.element.boss;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.RoleObj;
import cn.edu.scnu.element.effect.ExplosionEffect;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameAudio;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Rectangle;

//Boss 公共行为基类
public abstract class AbstractBoss extends RoleObj {
    private static final int WRECK_DISPLAY_FRAMES=40; //残骸持续显示的逻辑帧数
    protected boolean active; //Boss 是否已经进入战斗状态
    protected BossState state=BossState.IDLE; //Boss 当前行为状态
    protected boolean facingRight; //Boss 是否朝向右侧
    protected int attackCooldownFrames; //剩余攻击冷却逻辑帧数
    protected boolean attackReleased; //当前攻击动画是否已经释放炮弹
    protected int wreckFrames; //残骸已经显示的逻辑帧数
    protected int scalePercent; //Boss 素材显示缩放百分比
    private boolean attackVisible; //Boss 是否已经进入当前可攻击视口

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

    //设置 Boss 当前是否允许在视口内激活或发动新攻击
    public void setAttackVisible(boolean attackVisible) {
        this.attackVisible=attackVisible;
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

    //返回用于判断 Boss 是否进入当前摄像机视口的实际绘制区域
    public Rectangle getVisibilityBounds() {
        return getCurrentDrawBounds();
    }

    //根据玩家位置更新朝向和攻击状态
    @Override
    protected void move() {
        //死亡后不再更新行为
        if(state==BossState.DEAD) {
            return;
        }
        ElementObj player=findPlayer();
        //没有存活玩家时停止行动
        if(player==null) {
            return;
        }
        int distanceX=player.getX()-getX();
        //首次进入视口时激活 Boss
        if(!active && attackVisible) {
            active=true;
            GameAudio.play("boss.activation");
        }
        //未激活时保持待机
        if(!active) {
            return;
        }
        facingRight=distanceX>0;
        //逐帧减少攻击冷却
        if(attackCooldownFrames>0) {
            attackCooldownFrames--;
        }
        //可见且冷却结束时开始攻击
        if(attackVisible && state==BossState.IDLE && attackCooldownFrames==0) {
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

    //处理残骸、攻击释放和冷却
    @Override
    protected void add(long gameTime) {
        //残骸显示结束后移除 Boss
        if(state==BossState.DEAD) {
            wreckFrames++;
            if(wreckFrames>=WRECK_DISPLAY_FRAMES) {
                setLive(false);
            }
            return;
        }
        //非攻击状态不释放炮弹
        if(!active || state!=BossState.ATTACK) {
            return;
        }
        //攻击动画到达指定帧时释放炮弹
        if(!attackReleased && getImageIndex()==getAttackFrame()) {
            attackReleased=true;
            releaseAttack();
        }
        //攻击动画结束后进入冷却
        if(isAnimationEnd()) {
            attackCooldownFrames=getAttackCooldownFrames();
            attackReleased=false;
            state=BossState.IDLE;
        }
    }

    //由具体 Boss 创建炮弹
    protected void releaseAttack() {
    }

    //生命归零后进入残骸状态
    @Override
    public void hurt(int damage) {
        //残骸状态不再受伤
        if(state==BossState.DEAD) {
            return;
        }
        setHp(Math.max(0,getHp()-damage));
        //生命归零时生成爆炸并显示残骸
        if(getHp()==0) {
            state=BossState.DEAD;
            attackReleased=false;
            attackCooldownFrames=0;
            wreckFrames=0;
            Rectangle bounds=getRectangle();
            ElementManager.getManager().addElement(
                    ExplosionEffect.large((int)Math.round(bounds.getCenterX()),
                            (int)Math.round(bounds.getCenterY())),
                    GameElement.EFFECT);
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
