package cn.edu.scnu.element.boss;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.RoleObj;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

/**
 * Boss抽象基类 - 所有Boss的公共父类
 * 
 * <p>职责：
 * <ul>
 * <li>统一Boss状态机管理（IDLE、ATTACK_PRIMARY、ATTACK_SPECIAL、HURT、DEAD）</li>
 * <li>生命值和最大生命值管理</li>
 * <li>攻击冷却和攻击触发机制</li>
 * <li>面向玩家的自动朝向</li>
 * <li>激活机制（玩家到达触发点后才开始攻击）</li>
 * <li>死亡动画播放和残骸显示</li>
 * </ul>
 * 
 * <p>设计注意：
 * <ul>
 * <li>Boss不移动位置，仅原地攻击</li>
 * <li>激活前（isActivated=false）不会进入攻击状态</li>
 * <li>死亡后播放残骸动画（wreck），动画结束后才删除</li>
 * <li>攻击触发使用attackReleased标记，确保每次攻击只触发一次</li>
 * </ul>
 * 
 * @see RoleObj
 */
public abstract class AbstractBoss extends RoleObj {
    /** 当前状态 */
    protected BossState state=BossState.IDLE;
    /** 最大生命值，用于血条显示 */
    protected int maxHealth;
    /** 攻击冷却时间（未使用，保留字段） */
    protected long attackCooldownTime;
    /** 下次攻击时间 */
    protected long nextAttackTime;
    /** 当前攻击是否已触发 */
    protected boolean attackReleased;
    /** 攻击触发帧（由子类实现） */
    protected int attackFrame;
    /** 是否朝右 */
    protected boolean facingRight=true;
    /** 是否已激活，玩家到达触发点后设置为true */
    protected boolean isActivated=false;

    /** 供GameLoad通过反射创建模板对象 */
    protected AbstractBoss() {
    }

    /**
     * 创建Boss实体
     * @param x 初始世界横坐标
     * @param y 初始世界纵坐标
     * @param icon 初始动画帧图标
     * @param hp 生命值（同时作为maxHealth）
     * @param attack 攻击力
     */
    protected AbstractBoss(int x, int y, ImageIcon icon, int hp, int attack) {
        super(x, y, icon.getIconWidth(), icon.getIconHeight(), icon, hp, attack);
        this.maxHealth=hp;
    }

    /** 获取空闲动画键 */
    protected abstract String getIdleAnimation();

    /** 获取攻击动画键 */
    protected abstract String getAttackAnimation();

    /** 获取残骸动画键（死亡后播放） */
    protected abstract String getWreckAnimation();

    /** 获取空闲动画换帧间隔 */
    protected abstract int getIdleInterval();

    /** 获取攻击动画换帧间隔 */
    protected abstract int getAttackInterval();

    /** 获取攻击触发帧 */
    protected abstract int getAttackFrame();

    /** 获取攻击冷却帧数 */
    protected abstract long getAttackCooldownFrames();

    /**
     * 绘制Boss，使用脚底中心锚点，支持左右翻转
     * @param g 画笔
     */
    @Override
    public void showElement(Graphics g) {
        ImageIcon frame=getIcon();
        if (frame == null) {
            return;
        }

        int drawWidth=frame.getIconWidth();
        int drawHeight=frame.getIconHeight();
        int footX=getX() + getW() / 2;
        int footY=getY() + getH();
        int drawX=footX - drawWidth / 2;
        int drawY=footY - drawHeight;
        Image image=frame.getImage();

        if (facingRight) {
            g.drawImage(image, drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
        } else {
            g.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
        }
    }

    /**
     * 移动逻辑：Boss不移动，仅更新朝向
     */
    @Override
    protected void move() {
        if (state == BossState.DEAD) {
            return;
        }

        ElementObj player=findPlayer();
        if (player != null) {
            facingRight=player.getX() > getX();
        }
    }

    /** 根据当前状态切换动画 */
    @Override
    protected void updateImage(long gameTime) {
        switch (state) {
            case ATTACK_PRIMARY:
            case ATTACK_SPECIAL:
                playAnimation(getAttackAnimation(), gameTime, getAttackInterval(), false);
                break;
            case DEAD:
                playAnimation(getWreckAnimation(), gameTime, getAttackInterval(), false);
                break;
            default:
                playAnimation(getIdleAnimation(), gameTime, getIdleInterval(), true);
                break;
        }
    }

    /**
     * 每帧更新逻辑：攻击状态管理、激活检查、死亡检查
     * @param gameTime 游戏时间
     */
    @Override
    protected void add(long gameTime) {
        if (state == BossState.DEAD) {
            /** 死亡动画播放结束后删除 */
            if (isAnimationEnd()) {
                setLive(false);
            }
            return;
        }

        /** 未激活状态下不进行任何攻击 */
        if (!isActivated) {
            return;
        }

        /** 空闲状态且冷却结束时进入攻击状态 */
        if (state == BossState.IDLE && gameTime >= nextAttackTime) {
            state=BossState.ATTACK_PRIMARY;
            attackReleased=false;
        }

        /** 攻击状态下的触发逻辑 */
        if ((state == BossState.ATTACK_PRIMARY || state == BossState.ATTACK_SPECIAL)) {
            /** 在攻击触发帧时触发攻击（仅一次） */
            if (!attackReleased && getImageIndex() == getAttackFrame()) {
                attackReleased=true;
            }

            /** 攻击动画结束后进入冷却 */
            if (isAnimationEnd()) {
                nextAttackTime=gameTime + getAttackCooldownFrames();
                attackReleased=false;
                state=BossState.IDLE;
            }
        }
    }

    /**
     * 受伤处理：扣除生命值，生命值为0时进入DEAD状态
     * @param damage 伤害值
     */
    @Override
    public void hurt(int damage) {
        if (state == BossState.DEAD) {
            return;
        }

        int currentHp=getHp() - damage;
        if (currentHp < 0) {
            currentHp=0;
        }
        setHp(currentHp);

        if (currentHp == 0) {
            state=BossState.DEAD;
        }
    }

    /** 从ElementManager查找第一个存活的玩家 */
    protected ElementObj findPlayer() {
        for (ElementObj player : ElementManager.getManager()
                .getElementByKey(GameElement.PLAY)) {
            if (player.isLive()) {
                return player;
            }
        }
        return null;
    }

    /** 返回贴合Boss身体区域的碰撞框 */
    @Override
    public Rectangle getRectangle() {
        return new Rectangle(
                getX() + (int) (getW() * 0.10),
                getY() + (int) (getH() * 0.05),
                (int) (getW() * 0.80),
                (int) (getH() * 0.90));
    }

    /** 激活Boss，使其开始攻击 */
    public void activate() {
        isActivated=true;
    }

    /** 是否已激活 */
    public boolean isActivated() {
        return isActivated;
    }

    /** 获取最大生命值，用于血条显示 */
    public int getMaxHealth() {
        return maxHealth;
    }

    /** 获取当前状态 */
    public BossState getState() {
        return state;
    }
}