package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.RoleObj;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameAudio;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Rectangle;

//普通敌人的公共行为基类
public abstract class AbstractEnemy extends RoleObj {
    private static final int LOST_TARGET_LIMIT=30; //连续超出脱战范围后结束交战的逻辑帧数
    protected EnemyState state=EnemyState.MOVE; //敌人当前行为状态
    protected double preciseX; //敌人的精确世界横坐标
    protected double moveSpeed; //敌人的水平移动速度
    protected boolean facingRight; //敌人是否朝向右侧
    protected int detectRange; //敌人发现玩家的水平距离
    protected int attackRange; //敌人进入攻击状态的水平距离
    protected int patrolMinX; //巡逻范围的最小世界横坐标
    protected int patrolMaxX; //巡逻范围的最大世界横坐标
    protected int patrolDirection=-1; //巡逻方向，-1 向左，1 向右
    protected int attackCooldownFrames; //剩余攻击冷却逻辑帧数
    protected int scalePercent; //素材显示缩放百分比
    private boolean attackVisible; //敌人是否已经进入当前可攻击视口
    private boolean engaged; //敌人是否已经进入持续交战状态
    private int lostTargetFrames; //玩家连续超出脱战范围的逻辑帧数
    private int lastReleasedAttackFrame=-1; //本轮攻击最近一次释放武器的动画帧

    //供具体敌人的无参构造调用
    protected AbstractEnemy() {
    }

    //根据移动首帧的自然尺寸创建敌人逻辑框
    protected AbstractEnemy(int x,int y,ImageIcon icon,int hp,int attack,
                            int scalePercent) {
        super(x,y,scalePixels(getOpaqueBounds(icon).width,scalePercent),
                scalePixels(getOpaqueBounds(icon).height,scalePercent),
                icon,hp,attack);
        this.scalePercent=scalePercent;
        preciseX=x;
    }

    //设置敌人当前是否允许从视口内发动攻击
    public void setAttackVisible(boolean attackVisible) {
        this.attackVisible=attackVisible;
    }

    //获取一次攻击中的唯一武器释放帧
    protected abstract int getAttackFrame();

    //获取一次攻击结束后的冷却逻辑帧数
    protected abstract int getAttackCooldownFrames();

    //获取玩家过近时敌人主动后退的水平距离
    protected int getRetreatRange() {
        return 0;
    }

    //获取进入交战后允许丢失玩家的水平距离
    protected int getLoseRange() {
        return detectRange+450;
    }

    //获取交战状态下的移动速度倍率
    protected double getEngagedSpeedMultiplier() {
        return 1.0;
    }

    //敌人是否在存在存活玩家时始终保持交战
    protected boolean shouldAlwaysEngage() {
        return false;
    }

    //获取公共死亡动画比例
    protected int getDeathScalePercent() {
        return 100;
    }

    //解析敌人配置
    protected int[] parseEnemyConfig(String config,String enemyName) {
        String[] values=config.split(",");
        //敌人统一使用六项配置
        if(values.length!=6) {
            throw new IllegalArgumentException(enemyName
                    +"配置格式应为 x,y,hp,attack,patrolMinX,patrolMaxX");
        }
        int[] parsed=new int[values.length];
        for(int i=0;i<values.length;i++) {
            parsed[i]=Integer.parseInt(values[i].trim());
        }
        int x=parsed[0];
        int patrolMinX=parsed[4];
        int patrolMaxX=parsed[5];
        //出生点必须位于巡逻范围内
        if(patrolMinX>patrolMaxX || x<patrolMinX || x>patrolMaxX) {
            throw new IllegalArgumentException(enemyName
                    +" x 必须位于有效巡逻区间内");
        }
        return parsed;
    }

    //按当前素材自然尺寸和逻辑框脚底中心绘制敌人
    @Override
    public void showElement(Graphics g) {
        if(getIcon()!=null) {
            int drawScale=state==EnemyState.DEAD
                    ? getDeathScalePercent() : scalePercent;
            drawFootAnchoredFrame(g,getIcon(),drawScale,facingRight);
        }
    }

    //返回当前自然帧以逻辑框脚底中心锚定后的绘制区域
    protected Rectangle getCurrentDrawBounds() {
        int drawScale=state==EnemyState.DEAD
                ? getDeathScalePercent() : scalePercent;
        return getFootAnchoredDrawBounds(getIcon(),drawScale,facingRight);
    }

    //根据玩家距离更新行为
    @Override
    protected void move() {
        //死亡或攻击时不处理移动
        if(state==EnemyState.DEAD || state==EnemyState.ATTACK) {
            return;
        }
        if(attackCooldownFrames>0) {
            attackCooldownFrames--;
        }

        ElementObj player=findPlayer();
        //没有玩家时返回巡逻范围
        if(player==null) {
            engaged=false;
            lostTargetFrames=0;
            if(!returnToPatrolRange()) {
                patrol();
            }
            return;
        }
        int distanceX=player.getX()-getX();
        int absoluteDistanceX=Math.abs(distanceX);
        facingRight=distanceX>0;

        //持续追击型敌人始终保持交战
        if(shouldAlwaysEngage()) {
            engaged=true;
            lostTargetFrames=0;
        }else {
            //玩家进入侦测范围时开始交战
            if(!engaged && absoluteDistanceX<=detectRange) {
                engaged=true;
                lostTargetFrames=0;
            }
            //玩家离开过久后结束交战
            if(engaged && absoluteDistanceX>getLoseRange()) {
                lostTargetFrames++;
                if(lostTargetFrames>LOST_TARGET_LIMIT) {
                    engaged=false;
                    lostTargetFrames=0;
                }
            }else if(engaged) {
                lostTargetFrames=0;
            }
        }

        //未交战时继续巡逻
        if(!engaged) {
            if(!returnToPatrolRange()) {
                patrol();
            }
            return;
        }

        //玩家过近时主动后退
        if(attackVisible && getRetreatRange()>0
                && absoluteDistanceX<getRetreatRange()) {
            state=EnemyState.MOVE;
            moveHorizontally(distanceX>0 ? -1 : 1,
                    moveSpeed*getEngagedSpeedMultiplier());
            clampToWorldBounds();
            return;
        }
        //玩家进入攻击范围时攻击或等待冷却
        if(attackVisible && absoluteDistanceX<=attackRange) {
            state=attackCooldownFrames==0 ? EnemyState.ATTACK : EnemyState.IDLE;
            return;
        }
        //交战中向玩家靠近
        state=EnemyState.MOVE;
        moveHorizontally(distanceX>0 ? 1 : -1,
                moveSpeed*getEngagedSpeedMultiplier());
        clampToWorldBounds();
    }

    //在配置的世界横坐标区间内往返巡逻
    private void patrol() {
        state=EnemyState.MOVE;
        preciseX+=patrolDirection*moveSpeed*0.5;
        //到达巡逻边界后掉头
        if(preciseX<=patrolMinX) {
            preciseX=patrolMinX;
            patrolDirection=1;
            facingRight=true;
        }else if(preciseX>=patrolMaxX) {
            preciseX=patrolMaxX;
            patrolDirection=-1;
            facingRight=false;
        }
        setX((int)Math.round(preciseX));
    }

    //按方向和速度更新敌人的精确世界横坐标
    private void moveHorizontally(int direction,double speed) {
        preciseX+=direction*speed;
        setX((int)Math.round(preciseX));
    }

    //将交战移动限制在当前地图世界范围内
    private void clampToWorldBounds() {
        int worldWidth=GameLoad.getInt("window.width");
        java.util.List<ElementObj> maps=ElementManager.getManager()
                .getElementByKey(GameElement.MAPS);
        if(!maps.isEmpty()) {
            worldWidth=maps.get(0).getW();
        }
        preciseX=Math.max(0,Math.min(preciseX,
                Math.max(0,worldWidth-getW())));
        setX((int)Math.round(preciseX));
    }

    //脱战后返回巡逻区
    private boolean returnToPatrolRange() {
        if(preciseX<patrolMinX) {
            state=EnemyState.MOVE;
            facingRight=true;
            moveHorizontally(1,Math.min(moveSpeed,patrolMinX-preciseX));
            return true;
        }
        if(preciseX>patrolMaxX) {
            state=EnemyState.MOVE;
            facingRight=false;
            moveHorizontally(-1,Math.min(moveSpeed,preciseX-patrolMaxX));
            return true;
        }
        return false;
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

    //生命归零后播放死亡动画
    @Override
    public void hurt(int damage) {
        //死亡动画期间不再受伤
        if(state==EnemyState.DEAD) {
            return;
        }
        setHp(Math.max(0,getHp()-damage));
        //生命归零时进入死亡动画
        if(getHp()==0) {
            GameAudio.playIfIdle("enemy.death");
            state=EnemyState.DEAD;
            lastReleasedAttackFrame=-1;
            attackCooldownFrames=0;
        }
    }

    //处理死亡、攻击释放和冷却
    @Override
    protected void add(long gameTime) {
        //死亡动画结束后移除敌人
        if(state==EnemyState.DEAD) {
            if(isAnimationEnd()) {
                setLive(false);
            }
            return;
        }
        //非攻击状态重置释放帧记录
        if(state!=EnemyState.ATTACK) {
            lastReleasedAttackFrame=-1;
            return;
        }
        int frame=getImageIndex();
        //每个攻击关键帧只释放一次武器
        if(frame!=lastReleasedAttackFrame && shouldReleaseAttackAtFrame(frame)) {
            lastReleasedAttackFrame=frame;
            releaseAttack();
        }
        //攻击动画结束后进入冷却
        if(isAnimationEnd()) {
            attackCooldownFrames=getAttackCooldownFrames();
            lastReleasedAttackFrame=-1;
            state=EnemyState.IDLE;
        }
    }

    //由具体敌人创建武器
    protected void releaseAttack() {
    }

    //普通敌人默认只在一个关键帧释放武器，连发敌人可覆盖
    protected boolean shouldReleaseAttackAtFrame(int frame) {
        return frame==getAttackFrame();
    }

    //根据移动首帧逻辑尺寸返回稳定的身体碰撞框
    @Override
    public Rectangle getRectangle() {
        return new Rectangle(getX()+(int)(getW()*0.20),
                getY()+(int)(getH()*0.10),(int)(getW()*0.60),
                (int)(getH()*0.85));
    }
}
