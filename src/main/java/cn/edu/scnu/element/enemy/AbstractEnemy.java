package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.RoleObj;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

//普通敌人的公共基类，统一简单移动、攻击和死亡行为
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
    protected boolean attackReleased; //当前攻击动画是否已触发攻击时机

    //供具体敌人的无参构造调用
    protected AbstractEnemy() {
    }

    //根据初始动画帧创建具有自然素材尺寸的敌人
    protected AbstractEnemy(int x, int y, ImageIcon icon, int hp, int attack) {
        super(x, y, icon.getIconWidth(), icon.getIconHeight(), icon, hp, attack);
        preciseX=x;
    }

    //获取一次攻击中的唯一触发帧
    protected abstract int getAttackFrame();

    //获取一次攻击结束后的冷却逻辑帧数
    protected abstract int getAttackCooldownFrames();

    //按当前动画帧的自然尺寸和逻辑框脚底中心绘制敌人
    @Override
    public void showElement(Graphics g) {
        ImageIcon frame = getIcon();
        if (frame == null) {
            return;
        }

        int drawWidth=frame.getIconWidth();
        int drawHeight=frame.getIconHeight();
        int footX = getX() + getW() / 2;
        int footY = getY() + getH();
        int drawX = footX - drawWidth / 2;
        int drawY = footY - drawHeight;
        Image image = frame.getImage();

        if (facingRight) {
            g.drawImage(image, drawX + drawWidth, drawY,
                    -drawWidth, drawHeight, null);
        } else {
            g.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
        }
    }

    //根据玩家距离执行巡逻、追踪、等待或攻击
    @Override
    protected void move() {
        if (state == EnemyState.DEAD || state == EnemyState.ATTACK) {
            return;
        }

        if (attackCooldownFrames > 0) {
            attackCooldownFrames--;
        }

        ElementObj player = findPlayer();
        if (player == null) {
            patrol();
            return;
        }

        int distanceX = player.getX() - getX();
        int absoluteDistanceX = Math.abs(distanceX);
        facingRight = distanceX > 0;

        if (absoluteDistanceX <= attackRange) {
            if (attackCooldownFrames == 0) {
                state=EnemyState.ATTACK;
            } else {
                state=EnemyState.IDLE;
            }
            return;
        }

        if (absoluteDistanceX <= detectRange) {
            state=EnemyState.MOVE;
            if (distanceX > 0) {
                preciseX+=moveSpeed;
            } else if (distanceX < 0) {
                preciseX-=moveSpeed;
            }

            if (preciseX < patrolMinX) {
                preciseX=patrolMinX;
            } else if (preciseX > patrolMaxX) {
                preciseX=patrolMaxX;
            }
            setX((int) Math.round(preciseX));
            return;
        }

        patrol();
    }

    //在配置的世界横坐标区间内往返巡逻
    private void patrol() {
        state=EnemyState.MOVE;
        preciseX+=patrolDirection * moveSpeed * 0.5;
        if (preciseX <= patrolMinX) {
            preciseX=patrolMinX;
            patrolDirection=1;
            facingRight=true;
        } else if (preciseX >= patrolMaxX) {
            preciseX=patrolMaxX;
            patrolDirection=-1;
            facingRight=false;
        }
        setX((int) Math.round(preciseX));
    }

    //从唯一元素管理器中查找第一个存活玩家
    protected ElementObj findPlayer() {
        for (ElementObj player : ElementManager.getManager()
                .getElementByKey(GameElement.PLAY)) {
            if (player.isLive()) {
                return player;
            }
        }
        return null;
    }

    //扣除生命值，并在死亡动画结束后才使敌人失效
    @Override
    public void hurt(int damage) {
        if (state == EnemyState.DEAD) {
            return;
        }

        int currentHp=getHp()-damage;
        if (currentHp < 0) {
            currentHp=0;
        }
        setHp(currentHp);

        if (currentHp == 0) {
            state=EnemyState.DEAD;
            attackReleased=false;
            attackCooldownFrames=0;
        }
        //不调用 super.hurt()，因为父类会立即失效，使死亡动画无法显示
    }

    //处理死亡结束、唯一攻击时机和攻击动画结束
    @Override
    protected void add(long gameTime) {
        if (state == EnemyState.DEAD) {
            if (isAnimationEnd()) {
                setLive(false);
            }
            return;
        }

        if (state != EnemyState.ATTACK) {
            attackReleased=false;
            return;
        }

        if (!attackReleased && getImageIndex() == getAttackFrame()) {
            attackReleased=true;
            releaseAttack();
        }

        if (isAnimationEnd()) {
            attackCooldownFrames=getAttackCooldownFrames();
            attackReleased=false;
            state=EnemyState.IDLE;
        }
    }

    //在攻击动画的唯一释放帧执行具体攻击行为
    protected void releaseAttack() {
    }

    //根据基础移动尺寸返回较贴合身体区域的碰撞框
    @Override
    public Rectangle getRectangle() {
        return new Rectangle(
                getX() + (int) (getW() * 0.20),
                getY() + (int) (getH() * 0.10),
                (int) (getW() * 0.60),
                (int) (getH() * 0.85));
    }
}
