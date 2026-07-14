package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

/**
 * 火箭筒兵 - 中远程敌方单位
 * 
 * <p>特点：
 * <ul>
 * <li>使用火箭弹攻击，攻击距离最远（350像素）</li>
 * <li>移动速度最慢（1.0），换弹冷却时间较长（60帧）</li>
 * <li>发现范围较大（450像素），在攻击范围外会缓慢接近玩家</li>
 * <li>火箭弹命中后产生爆炸范围伤害（待成员B接入WeaponService）</li>
 * </ul>
 * 
 * <p>行为模式：巡逻 → 发现玩家（450像素内）→ 接近至攻击范围（350像素）→ 发射火箭 → 冷却等待
 * 
 * @see AbstractEnemy
 */
public class BazookaEnemy extends AbstractEnemy {
    private static final String MOVE_ANIMATION="enemy.bazooka.move";
    private static final String ATTACK_ANIMATION="enemy.bazooka.attack";
    private static final String DEATH_ANIMATION="enemy.death";
    private static final int MOVE_INTERVAL=3;
    private static final int ATTACK_INTERVAL=3;
    private static final int DEATH_INTERVAL=2;
    /** 移动速度最慢，仅1.0像素/帧 */
    private static final double MOVE_SPEED=1.0;
    /** 发现范围较大，450像素 */
    private static final int DETECT_RANGE=450;
    /** 攻击范围最远，350像素 */
    private static final int ATTACK_RANGE=350;
    /** 攻击触发帧为第2帧（0-indexed） */
    private static final int ATTACK_FRAME=2;
    /** 攻击冷却最长，60帧 */
    private static final int ATTACK_COOLDOWN_FRAMES=60;

    /** 供GameLoad通过反射创建模板对象 */
    public BazookaEnemy() {
    }

    /**
     * 使用配置数据创建火箭筒兵实体
     * @param x 初始世界横坐标
     * @param y 初始世界纵坐标
     * @param icon 初始动画帧图标
     * @param hp 生命值
     * @param attack 攻击力
     * @param patrolMinX 巡逻范围最小横坐标
     * @param patrolMaxX 巡逻范围最大横坐标
     */
    private BazookaEnemy(int x, int y, ImageIcon icon, int hp, int attack,
                         int patrolMinX, int patrolMaxX) {
        super(x, y, icon, hp, attack);
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    /**
     * 按配置字符串创建火箭筒兵
     * <p>配置格式：x,y,hp,attack,patrolMinX,patrolMaxX
     * @param str 配置字符串
     * @return 火箭筒兵实例
     */
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        if (data.length != 6) {
            throw new IllegalArgumentException(
                    "火箭筒兵配置格式应为 x,y,hp,attack,patrolMinX,patrolMaxX");
        }

        int x=Integer.parseInt(data[0].trim());
        int y=Integer.parseInt(data[1].trim());
        int hp=Integer.parseInt(data[2].trim());
        int attack=Integer.parseInt(data[3].trim());
        int patrolMinX=Integer.parseInt(data[4].trim());
        int patrolMaxX=Integer.parseInt(data[5].trim());

        if (patrolMinX > patrolMaxX) {
            throw new IllegalArgumentException(
                    "火箭筒兵 patrolMinX 不能大于 patrolMaxX");
        }
        if (x < patrolMinX || x > patrolMaxX) {
            throw new IllegalArgumentException("火箭筒兵 x 必须位于巡逻区间内");
        }

        ImageIcon icon=GameLoad.getImages(MOVE_ANIMATION).get(0);
        return new BazookaEnemy(x, y, icon, hp, attack, patrolMinX, patrolMaxX);
    }

    /** 根据当前状态切换动画 */
    @Override
    protected void updateImage(long gameTime) {
        switch (state) {
            case DEAD:
                playAnimation(DEATH_ANIMATION, gameTime, DEATH_INTERVAL, false);
                break;
            case ATTACK:
                playAnimation(ATTACK_ANIMATION, gameTime, ATTACK_INTERVAL, false);
                break;
            default:
                playAnimation(MOVE_ANIMATION, gameTime, MOVE_INTERVAL, true);
                break;
        }
    }

    /** 获取攻击触发帧 */
    @Override
    protected int getAttackFrame() {
        return ATTACK_FRAME;
    }

    /** 获取攻击冷却帧数 */
    @Override
    protected int getAttackCooldownFrames() {
        return ATTACK_COOLDOWN_FRAMES;
    }
}