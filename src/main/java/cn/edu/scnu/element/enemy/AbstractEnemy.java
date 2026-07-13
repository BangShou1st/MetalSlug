package cn.edu.scnu.element.enemy;

/**
 * 普通敌人的抽象父类
 * 职责：
 * 1.管理敌人共有状态（巡逻、追击、攻击、受伤、死亡）
 * 2.管理敌人的生命值、朝向、移动速度
 * 3.通过PlayerLocator来获取玩家位置 （不导入具体Player类）
 * 4.通过WeaponService发起攻击 （不直接new武器对象）
 * 5.统一脚底中心描点绘制
 * 6.统一攻击冷却和防止重复发射，
 *
 */

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.Camera;
import cn.edu.scnu.manager.level.PlayerLocator;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractEnemy extends ElementObj {

    protected EnemyState state = EnemyState.PATROL;  //敌人状态

    protected int health; //当前生命值（用于判断敌人状态）
    protected int maxHealth; //最大生命值(敌人初始生命值）

    //敌人具体坐标
    protected double preciseX;
    protected double preciseY;

    //敌人移动速度
    protected double moveSpeed;

    //敌人朝向 默认向左
    protected boolean facingRight = false;

    protected int detectRange;  //敌人发现玩家的距离
    protected int attackRange;  //敌人开始攻击玩家的距离

    //敌人的巡逻范围
    protected int patrolMinX;
    protected int patrolMaxX;
    protected int patrolDirection = -1;  //巡逻方向，-1向左，1向右

    //攻击控制
    protected long nextAttackTime; //敌人下次可攻击的时间
    protected boolean attackReleased; //当前攻击动画是否已经发生过发射物

    //外部依赖（通过构造注入）
    //protected WeaponService weaponService; //成员B提供内容
    protected PlayerLocator playerLocator;
    protected Camera camera;

    protected double scale = 3.0; //地图缩放比例。

    //敌人的构造方法 世界坐标x、y； 敌人逻辑碰撞宽度w，碰撞高度h， 以及敌人的首帧图片firstFrame
    protected AbstractEnemy(int x, int y, int w, int h, ImageIcon firstFrame) {
        super(x, y, w, h, firstFrame);
        this.preciseX = x;
        this.preciseY = y;
    }

    //返回敌人的移动动画键
    protected abstract String getMoveAnimationKey();

    //返回该敌人的攻击动画键
    protected abstract String getAttackAnimationKey();

    //返回攻击动画中生成发射器的关键帧索引
    protected abstract int getAttackFrame();

    //返回攻击冷却时间（毫秒）
    protected abstract long getAttackCooldown();

    //构建攻击请求
    protected abstract void performAttack(long gameTime);

    @Override
    public void showElement(Graphics g) {
        //获取当前动画帧图片
        ImageIcon currentFrame = getIcon();
        if (currentFrame == null) return;

        Image img = currentFrame.getImage();
        int srcW = currentFrame.getIconWidth();
        int srcH = currentFrame.getIconHeight();

        //计算绘制尺寸
        int drawW = (int) Math.round(srcW * scale);
        int drawH = (int) Math.round(srcH * scale);

        //脚底中心锚点计算
        int footX = getX() + getW() / 2;
        int footY = getY() + getH();
        int drawX = footX - drawW / 2;
        int drawY = footY - drawH;

        //减去Camera偏移 （从世界坐标→ 屏幕坐标）
        if (camera != null) {
            drawX -= camera.getX();
            drawY -= camera.getY();
        }

        //敌人朝向反转（默认素材向左，朝右需要水平翻转。）
        if (facingRight) {
            //需要确认Graphics的翻转绘制是否可用
            g.drawImage(img, drawX + drawW, drawY, -drawW, drawH, null);
        } else {
            g.drawImage(img, drawX, drawY, drawW, drawH, null);
        }
    }

    @Override
    protected void move() {
        if (state == EnemyState.DEAD || state == EnemyState.HURT) {
            return;  //当敌人死亡或者受伤，则停止移动。
        }
        if (state == EnemyState.ATTACK) {
            return; //当敌人攻击时，则停止移动。
        }

        //获取玩家位置，决定敌人的行为（巡逻还是追击）
        if (playerLocator != null) {
            ElementObj player = playerLocator.findPlayer();
            if (player != null && player.isLive()) {
                int distX = player.getX() - getX();
                int absDistX = Math.abs(distX);

                facingRight = distX > 0; //更新朝向

                //玩家进入敌人攻击范围
                if (absDistX <= attackRange) {
                    state = EnemyState.ATTACK;
                    return;
                } else if (absDistX <= detectRange) {
                    //在检测范围内但是没到攻击距离，切换至追击状态
                    state = EnemyState.CHASE;
                    double dir = distX > 0 ? moveSpeed : -moveSpeed;
                    preciseX += dir;
                } else {
                    //超出检测范围，切换至巡逻状态
                    state = EnemyState.PATROL;
                    preciseX += patrolDirection * moveSpeed * 0.5;
                    //抵达巡逻边界，切换巡逻方向
                    if (preciseX <= patrolMinX) {
                        patrolDirection = 1;
                        facingRight = true;
                    } else if (preciseX >= patrolMaxX) {
                        patrolDirection = -1;
                        facingRight = false;
                    }
                }
            } else {
                //玩家不存在，默认为巡逻状态
                state = EnemyState.PATROL;
                preciseX += patrolDirection * moveSpeed * 0.5;
                if (preciseX <= patrolMinX) patrolDirection = 1;
                if (preciseX >= patrolMaxX) patrolDirection = -1;
            }
        } else {
            //还没有PlayerLocator，只做简单巡逻
            state = EnemyState.PATROL;
            preciseX += patrolDirection * moveSpeed * 0.5;
            if (preciseX <= patrolMinX) {
                patrolDirection = 1;
                facingRight = true;
            } else if (preciseX >= patrolMaxX) {
                patrolDirection = -1;
                facingRight = false;
            }
        }
        //同步精准坐标到父类整数坐标。
        setX((int) Math.round(preciseX));
        setY((int) Math.round(preciseY));
    }

    @Override
    protected void updateImage(long gameTime) {
        if (state == EnemyState.DEAD) {
            return;  //死亡状态不更新动画（由独立EFFECT播放死亡动画）
        }
        if (state == EnemyState.ATTACK) {
            playAnimation(getAttackAnimationKey(), gameTime, getAttackInterval(), false);
        } else {
            // 巡逻或追击状态 都使用移动动画。
            playAnimation(getMoveAnimationKey(), gameTime, getMoveInterval(), true);
        }
    }

    @Override
    protected void add(long gameTime) {
        if (state != EnemyState.ATTACK) {
            attackReleased = false;  //退出进攻状态时，重置攻击释放状态
            return;
        }

        //关键帧只生成一次发射物
        if (getImageIndex() == getAttackFrame() && !attackReleased) {
            performAttack(gameTime);
            attackReleased = true;
        }

        //非循环攻击动画播放完毕，切换到巡逻或追击追击状态。
        if (isAnimationEnd()) {
            attackReleased = false;
            state = EnemyState.PATROL;
        }
    }

    @Override
    // 创建公共死亡特效（enemy.death）作为独立 EFFECT 对象
    // 具体实现：向 ElementManager 添加一个死亡动画 EFFECT
    // 需要等 B 的特效模块合并后完善，第一天可以先留注释占位
    public void die() {
        state = EnemyState.DEAD;
    }

    // ====== Damageable 接口实现（第一天可以先写，等 B 的接口合并后实现） ======

    // public Camp getCamp() { return Camp.ENEMY; }
    // public Rectangle getDamageBounds() { ... }
    // public void takeDamage(int damage) { ... }
    // public boolean isDead() { return state == EnemyState.DEAD; }

    protected int getMoveInterval() {
        return 6;
    } //移动动画的换帧间隔（逻辑更新次数）
    //当前 logicInterval=50ms，interval=6 约等于300ms/帧

    protected int getAttackInterval() {
        return 5;
    } //攻击动画的换帧间隔。 子类可以覆盖调整。

    @Override
    //重写碰撞框，使用稳定的人体矩形，不随attack画布变宽。
    public Rectangle getRectangle() {
        int w = getW();
        int h = getH();
        int x = getX();
        int y = getY();
        return new Rectangle(
            x + (int)(w * 0.20),
            y + (int)(h * 0.10),
            (int)(w * 0.60),
            (int)(h * 0.85)
        );
    }

    //设置敌人巡逻范围
    public void setPatrolRange(int minX, int maxX) {
        this.patrolMinX = minX;
        this.patrolMaxX = maxX;
    }

    //设置外部依赖。
    public void setDependencies(PlayerLocator playerLocator, Camera camera) {
        this.playerLocator = playerLocator;
        this.camera = camera;
    }
}