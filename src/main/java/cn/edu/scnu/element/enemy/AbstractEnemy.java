package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.RoleObj;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Rectangle;

//普通敌人的公共基类，统一世界坐标移动、逻辑帧冷却和延迟死亡
public abstract class AbstractEnemy extends RoleObj {
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

    //获取一次攻击中的唯一武器释放帧
    protected abstract int getAttackFrame();

    //获取一次攻击结束后的冷却逻辑帧数
    protected abstract int getAttackCooldownFrames();

    //校验普通敌人的统一六项配置并转换为整数
    protected int[] parseEnemyConfig(String config,String enemyName) {
        String[] values=config.split(",");
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
            drawFootAnchoredFrame(g,getIcon(),scalePercent,facingRight);
        }
    }

    //返回当前自然帧以逻辑框脚底中心锚定后的绘制区域
    protected Rectangle getCurrentDrawBounds() {
        return getFootAnchoredDrawBounds(getIcon(),scalePercent,facingRight);
    }

    //根据玩家距离执行巡逻、追踪、等待或攻击
    @Override
    protected void move() {
        if(state==EnemyState.DEAD || state==EnemyState.ATTACK) {
            return;
        }
        if(attackCooldownFrames>0) {
            attackCooldownFrames--;
        }

        ElementObj player=findPlayer();
        if(player==null) {
            patrol();
            return;
        }
        int distanceX=player.getX()-getX();
        int absoluteDistanceX=Math.abs(distanceX);
        facingRight=distanceX>0;
        if(absoluteDistanceX<=attackRange) {
            state=attackCooldownFrames==0 ? EnemyState.ATTACK : EnemyState.IDLE;
            return;
        }
        if(absoluteDistanceX<=detectRange) {
            state=EnemyState.MOVE;
            preciseX+=distanceX>0 ? moveSpeed : -moveSpeed;
            clampToPatrolRange();
            return;
        }
        patrol();
    }

    //在配置的世界横坐标区间内往返巡逻
    private void patrol() {
        state=EnemyState.MOVE;
        preciseX+=patrolDirection*moveSpeed*0.5;
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

    //将追踪移动限制在当前敌人的巡逻范围内
    private void clampToPatrolRange() {
        preciseX=Math.max(patrolMinX,Math.min(preciseX,patrolMaxX));
        setX((int)Math.round(preciseX));
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

    //扣除生命值并进入仍保持存活的死亡动画状态
    @Override
    public void hurt(int damage) {
        if(state==EnemyState.DEAD) {
            return;
        }
        setHp(Math.max(0,getHp()-damage));
        if(getHp()==0) {
            state=EnemyState.DEAD;
            lastReleasedAttackFrame=-1;
            attackCooldownFrames=0;
        }
    }

    //处理死亡动画结束、唯一攻击帧和攻击冷却重置
    @Override
    protected void add(long gameTime) {
        if(state==EnemyState.DEAD) {
            if(isAnimationEnd()) {
                setLive(false);
            }
            return;
        }
        if(state!=EnemyState.ATTACK) {
            lastReleasedAttackFrame=-1;
            return;
        }
        int frame=getImageIndex();
        if(frame!=lastReleasedAttackFrame && shouldReleaseAttackAtFrame(frame)) {
            lastReleasedAttackFrame=frame;
            releaseAttack();
        }
        if(isAnimationEnd()) {
            attackCooldownFrames=getAttackCooldownFrames();
            lastReleasedAttackFrame=-1;
            state=EnemyState.IDLE;
        }
    }

    //由具体敌人在唯一攻击帧创建成员 B 的真实武器
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
