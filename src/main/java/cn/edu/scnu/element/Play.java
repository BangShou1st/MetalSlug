package cn.edu.scnu.element;

import cn.edu.scnu.element.effect.MuzzleEffect;
import cn.edu.scnu.element.weapon.Bullet;
import cn.edu.scnu.element.weapon.Grenade;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * 玩家对象，负责基础输入、移动、动画切换和绘制。
 */
public class Play extends RoleObj {
    private static final String STAND_IMAGE_KEY="player.stand"; //站立动画键
    private static final String RUN_IMAGE_KEY="player.run"; //跑步动画键
    private static final String JUMP_IMAGE_KEY="player.jump"; //跳跃动画键
    private static final String CROUCH_IMAGE_KEY="player.crouch"; //下蹲图片键
    private static final String SHOOT_STAND_IMAGE_KEY="player.shoot.stand"; //站立射击动画键
    private static final String SHOOT_CROUCH_IMAGE_KEY="player.shoot.crouch"; //下蹲射击动画键
    private static final String THROW_IMAGE_KEY="player.throw"; //投掷手雷动画键
    private static final String HURT_IMAGE_KEY="player.hurt"; //受伤动画键
    private static final String DEATH_IMAGE_KEY="player.death"; //死亡动画键
    private static final int DIRECTION_RIGHT=1; //朝右的方向值
    private static final int DIRECTION_LEFT=-1; //朝左的方向值
    private static final int STAND_INTERVAL=1; //站立图片刷新间隔
    private static final int RUN_INTERVAL=1; //跑步动画每个逻辑帧切换一次
    private static final int JUMP_INTERVAL=1; //跳跃动画每个逻辑帧切换一次
    private static final int CROUCH_INTERVAL=1; //下蹲图片刷新间隔
    private static final int SHOOT_INTERVAL=1; //射击动画每个逻辑帧切换一次
    private static final int THROW_INTERVAL=1; //投掷动画每个逻辑帧切换一次
    private static final int HURT_INTERVAL=1; //受伤动画每个逻辑帧切换一次
    private static final int DEATH_INTERVAL=1; //死亡动画每个逻辑帧切换一次
    private static final int INVINCIBLE_FRAMES=20; //受伤后的无敌逻辑帧数
    private static final int SHOOT_RELEASE_FRAME=2; //射击动作释放子弹的动画帧
    private static final int THROW_RELEASE_FRAME=3; //投掷动作释放手雷的动画帧
    private static final int BULLET_WIDTH=23; //普通子弹逻辑宽度
    private static final int BULLET_HEIGHT=7; //普通子弹逻辑高度
    private static final int MUZZLE_WIDTH=32; //枪口特效逻辑宽度
    private static final int MUZZLE_HEIGHT=35; //枪口特效逻辑高度

    //玩家当前动作状态
    private enum PlayerState {
        STAND, //站立
        RUN, //跑动
        JUMP, //跳跃
        CROUCH, //下蹲
        SHOOT_STAND, //站立射击
        SHOOT_CROUCH, //下蹲射击
        THROW, //投掷手雷
        HURT, //受伤
        DEAD //死亡
    }

    private boolean leftPressed; //是否按住左方向键
    private boolean rightPressed; //是否按住右方向键
    private boolean jumpRequested; //是否请求开始一次跳跃
    private boolean crouchPressed; //是否按住下蹲键
    private boolean shootRequested; //是否请求开始一次射击
    private boolean throwRequested; //是否请求开始一次投掷
    private boolean moving; //玩家本帧是否实际水平移动
    private boolean onGround; //玩家是否站在地面上
    private int direction=DIRECTION_RIGHT; //玩家当前朝向
    private int speed; //玩家水平移动速度
    private int jumpSpeed; //玩家起跳时的纵向速度
    private int gravity; //重力
    private int groundY; //玩家脚底对应的地面纵坐标
    private int worldWidth; //玩家可以移动的地图世界宽度
    private double preciseY; //跳跃过程中使用的精确纵坐标
    private double verticalSpeed; //玩家当前纵向速度
    private PlayerState state=PlayerState.STAND; //玩家当前动作状态
    private int invincibleFrames; //玩家剩余无敌逻辑帧数
    private boolean actionAnimationStarted; //一次性动作动画是否已经实际开始播放
    private boolean actionReleased; //当前动作是否已经生成发射物

    //供 GameLoad 通过反射创建玩家模板
    public Play() {
    }

    //创建具有实际位置、战斗属性和运动参数的玩家
    private Play(int x,int y,int w,int h,ImageIcon icon,int hp,int attack,int speed,
                 int jumpSpeed,int gravity,int groundY) {
        super(x,y,w,h,icon,hp,attack);
        this.speed=speed;
        this.jumpSpeed=jumpSpeed;
        this.gravity=gravity;
        this.groundY=groundY;
        worldWidth=GameLoad.getInt("window.width");
        preciseY=y;
        verticalSpeed=0;
        onGround=true;
    }

    //创建新的玩家对象
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        int x=Integer.parseInt(data[0].trim());
        int y=Integer.parseInt(data[1].trim());
        int hp=Integer.parseInt(data[2].trim());
        int attack=Integer.parseInt(data[3].trim());

        ImageIcon icon=GameLoad.getImages(STAND_IMAGE_KEY).get(0);
        int speed=GameLoad.getInt("player.speed");
        int jumpSpeed=GameLoad.getInt("player.jumpSpeed");
        int gravity=GameLoad.getInt("player.gravity");
        int groundY=GameLoad.getInt("player.groundY");
        return new Play(x,y,icon.getIconWidth(),icon.getIconHeight(),icon,hp,attack,
                speed,jumpSpeed,gravity,groundY);
    }

    //设置玩家在当前地图中的水平移动范围
    public void setWorldWidth(int worldWidth) {
        this.worldWidth=worldWidth;
    }

    //设置当前地图地面，并将玩家稳定放置到地面上
    public void placeOnGround(int groundY) {
        this.groundY=groundY;
        //玩家 y 是图片左上角；同步精确坐标后，脚底恰好位于地图地面
        preciseY=groundY-getH();
        setY((int)Math.round(preciseY));
        verticalSpeed=0;
        onGround=true;
    }

    //记录移动、跳跃、下蹲、射击和投掷的输入状态
    @Override
    public void keyClick(boolean pressed,int key) {
        if(key==KeyEvent.VK_A || key==KeyEvent.VK_LEFT) {
            leftPressed=pressed;
        }else if(key==KeyEvent.VK_D || key==KeyEvent.VK_RIGHT) {
            rightPressed=pressed;
        }else if(pressed && (key==KeyEvent.VK_W || key==KeyEvent.VK_UP)) {
            jumpRequested=true;
        }else if(key==KeyEvent.VK_S || key==KeyEvent.VK_DOWN) {
            crouchPressed=pressed;
        }else if(pressed && key==KeyEvent.VK_J) {
            shootRequested=true;
        }else if(pressed && key==KeyEvent.VK_K) {
            throwRequested=true;
        }
    }

    //玩家受伤时自行扣血，使死亡动画播完前仍留在框架更新列表中
    @Override
    public void hurt(int damage) {
        if(state==PlayerState.DEAD || invincibleFrames>0) {
            return;
        }

        int remainingHp=getHp()-damage;
        if(remainingHp<0) {
            remainingHp=0;
        }
        setHp(remainingHp);
        jumpRequested=false;
        shootRequested=false;
        throwRequested=false;
        moving=false;

        //不能调用 super.hurt()，否则生命归零时会立即 setLive(false)，死亡动画来不及播放
        if(remainingHp==0) {
            enterAction(PlayerState.DEAD);
        }else {
            invincibleFrames=INVINCIBLE_FRAMES;
            enterAction(PlayerState.HURT);
        }
    }

    //协调一次性动作、跳跃请求以及水平和纵向移动
    @Override
    protected void move() {
        updateInvincibility();
        boolean actionFinished=finishCurrentAction();
        if(!isLive()) {
            return;
        }
        if(!actionFinished && !isActionLocked()) {
            startRequestedAction();
        }

        if(!isActionLocked() && jumpRequested && onGround) {
            //屏幕 y 轴向下为正，因此负速度表示向上起跳
            verticalSpeed=-jumpSpeed;
            onGround=false;
        }
        jumpRequested=false;

        moveHorizontally();
        moveVertically();
        updateBasicState();

        //一次性请求每帧都清除，避免条件不满足时在未来自动触发
        shootRequested=false;
        throwRequested=false;
    }

    //每个逻辑帧递减一次无敌时间
    private void updateInvincibility() {
        if(invincibleFrames>0) {
            invincibleFrames--;
        }
    }

    //进入一次性动作时等待对应动画真正开始，避免沿用上一段动画的结束标记
    private void enterAction(PlayerState nextState) {
        state=nextState;
        actionAnimationStarted=false;
        actionReleased=false;
    }

    //判断射击或投掷动画是否正在锁定普通动作
    private boolean isActionLocked() {
        return state==PlayerState.SHOOT_STAND
                || state==PlayerState.SHOOT_CROUCH
                || state==PlayerState.THROW
                || state==PlayerState.HURT
                || state==PlayerState.DEAD;
    }

    //在一次性动画结束后退出动作，并留出一帧切回基础动画
    private boolean finishCurrentAction() {
        if(isActionLocked() && actionAnimationStarted && isAnimationEnd()) {
            if(state==PlayerState.DEAD) {
                setLive(false);
                return true;
            }
            state=PlayerState.STAND;
            actionAnimationStarted=false;
            actionReleased=false;
            return true;
        }
        return false;
    }

    //按投掷优先于射击的顺序消费本帧动作请求
    private void startRequestedAction() {
        if(isActionLocked() || !onGround) {
            return;
        }
        if(throwRequested) {
            enterAction(PlayerState.THROW);
        }else if(shootRequested && crouchPressed) {
            enterAction(PlayerState.SHOOT_CROUCH);
        }else if(shootRequested) {
            enterAction(PlayerState.SHOOT_STAND);
        }
    }

    //水平输入、下蹲限制、朝向和地图世界边界
    private void moveHorizontally() {
        int oldX=getX();
        int targetX=oldX;
        boolean crouching=onGround && crouchPressed;
        if(!isActionLocked() && !crouching && leftPressed!=rightPressed) {
            if(leftPressed) {
                targetX-=speed;
            }else if(rightPressed) {
                targetX+=speed;
            }
        }

        //玩家保存世界坐标，因此右边界取地图世界宽度而不是窗口宽度
        int maxX=worldWidth-getW();
        if(maxX<0) {
            maxX=0;
        }
        if(targetX<0) {
            targetX=0;
        }else if(targetX>maxX) {
            targetX=maxX;
        }

        setX(targetX);
        moving=targetX!=oldX;
        if(targetX<oldX) {
            direction=DIRECTION_LEFT;
        }else if(targetX>oldX) {
            direction=DIRECTION_RIGHT;
        }
    }

    //跳跃    处理重力、空中位移和落地
    private void moveVertically() {
        if(!onGround) {
            //先按当前速度更新位置，重力再使下一帧速度逐渐向下增加
            preciseY+=verticalSpeed;
            verticalSpeed+=gravity;

            //玩家 y 表示图片左上角，脚底落点需要用地面坐标减去玩家高度
            int groundTop=groundY-getH();
            if(preciseY>=groundTop) {
                preciseY=groundTop;
                verticalSpeed=0;
                onGround=true;
            }
            //物理计算保留小数精度，最终绘制坐标转换为整数
            setY((int)Math.round(preciseY));
        }
    }

    //在没有一次性动作时根据运动和输入更新基础状态
    private void updateBasicState() {
        if(isActionLocked()) {
            return;
        }
        if(!onGround) {
            state=PlayerState.JUMP;
        }else if(crouchPressed) {
            state=PlayerState.CROUCH;
        }else if(moving) {
            state=PlayerState.RUN;
        }else {
            state=PlayerState.STAND;
        }
    }

    //根据玩家当前状态播放对应动画
    @Override
    protected void updateImage(long gameTime) {
        switch(state) {
            case DEAD:
                playAnimation(DEATH_IMAGE_KEY,gameTime,DEATH_INTERVAL,false);
                actionAnimationStarted=true;
                break;
            case HURT:
                playAnimation(HURT_IMAGE_KEY,gameTime,HURT_INTERVAL,false);
                actionAnimationStarted=true;
                break;
            case THROW:
                playAnimation(THROW_IMAGE_KEY,gameTime,THROW_INTERVAL,false);
                actionAnimationStarted=true;
                break;
            case SHOOT_STAND:
                playAnimation(SHOOT_STAND_IMAGE_KEY,gameTime,SHOOT_INTERVAL,false);
                actionAnimationStarted=true;
                break;
            case SHOOT_CROUCH:
                playAnimation(SHOOT_CROUCH_IMAGE_KEY,gameTime,SHOOT_INTERVAL,false);
                actionAnimationStarted=true;
                break;
            case JUMP:
                playAnimation(JUMP_IMAGE_KEY,gameTime,JUMP_INTERVAL,false);
                break;
            case CROUCH:
                playAnimation(CROUCH_IMAGE_KEY,gameTime,CROUCH_INTERVAL,true);
                break;
            case RUN:
                playAnimation(RUN_IMAGE_KEY,gameTime,RUN_INTERVAL,true);
                break;
            default:
                playAnimation(STAND_IMAGE_KEY,gameTime,STAND_INTERVAL,true);
                break;
        }
    }

    //在射击或投掷动画的唯一释放帧创建玩家发射物
    @Override
    protected void add(long gameTime) {
        if(actionReleased) {
            return;
        }
        if((state==PlayerState.SHOOT_STAND
                || state==PlayerState.SHOOT_CROUCH)
                && getImageIndex()==SHOOT_RELEASE_FRAME) {
            actionReleased=true;
            releaseBullet();
        }else if(state==PlayerState.THROW
                && getImageIndex()==THROW_RELEASE_FRAME) {
            actionReleased=true;
            releaseGrenade();
        }
    }

    //根据站姿、蹲姿和朝向计算枪口并创建一颗子弹与一次枪口特效
    private void releaseBullet() {
        int muzzleSourceX=44;
        int muzzleSourceY=28;
        if(state==PlayerState.SHOOT_CROUCH) {
            muzzleSourceY=31;
        }

        int muzzleX=getX()+muzzleSourceX;
        if(direction==DIRECTION_LEFT) {
            muzzleX=getX()+getW()-1-muzzleSourceX;
        }
        int muzzleY=getY()+muzzleSourceY;
        int bulletX=muzzleX;
        int effectX=muzzleX;
        if(direction==DIRECTION_LEFT) {
            bulletX-=BULLET_WIDTH;
            effectX-=MUZZLE_WIDTH;
        }
        int bulletY=muzzleY-BULLET_HEIGHT/2;
        int effectY=muzzleY-MUZZLE_HEIGHT/2;

        ElementManager manager=ElementManager.getManager();
        manager.addElement(new Bullet(bulletX,bulletY,direction,getAttack()),
                GameElement.PLAYFILE);
        manager.addElement(new MuzzleEffect(effectX,effectY,direction),
                GameElement.EFFECT);
    }

    //根据玩家当前朝向从投掷动作的手部位置创建一枚手雷
    private void releaseGrenade() {
        int releaseX=getX()+getW()/2;
        int releaseY=getY()+(int)Math.round(getH()*0.12);
        ElementManager.getManager().addElement(
                new Grenade(releaseX,releaseY,direction,getAttack()),
                GameElement.PLAYFILE);
    }

    //按照玩家朝向绘制当前动画帧
    @Override
    public void showElement(Graphics g) {
        if(direction==DIRECTION_LEFT) {
            //负宽度绘制可以水平翻转图片，同时保持玩家逻辑位置不变
            g.drawImage(getIcon().getImage(),getX()+getW(),getY(),-getW(),getH(),null);
        }else {
            g.drawImage(getIcon().getImage(),getX(),getY(),getW(),getH(),null);
        }
    }
}
