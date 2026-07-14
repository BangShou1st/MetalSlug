package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

/**
 * 投弹兵 - 投掷类敌方单位
 * 
 * <p>特点：
 * <ul>
 * <li>使用手雷攻击，抛物线轨迹</li>
 * <li>发现范围中等（350像素），攻击范围较近（280像素）</li>
 * <li>手雷落地后产生范围爆炸伤害（待成员B接入WeaponService）</li>
 * <li>攻击触发帧较晚（第3帧），需要完整投掷动作</li>
 * <li>移动速度较慢（1.2），保持中距离作战</li>
 * </ul>
 * 
 * <p>行为模式：巡逻 → 发现玩家（350像素内）→ 接近至攻击范围（280像素）→ 投掷手雷 → 冷却等待
 * 
 * @see AbstractEnemy
 */
public class GrenadierEnemy extends AbstractEnemy {
    private static final String MOVE_ANIMATION="enemy.grenadier.move";
    private static final String ATTACK_ANIMATION="enemy.grenadier.attack";
    private static final String DEATH_ANIMATION="enemy.death";
    private static final int MOVE_INTERVAL=3;
    private static final int ATTACK_INTERVAL=3;
    private static final int DEATH_INTERVAL=2;
    /** 移动速度较慢，1.2像素/帧 */
    private static final double MOVE_SPEED=1.2;
    /** 发现范围中等，350像素 */
    private static final int DETECT_RANGE=350;
    /** 攻击范围较近，280像素 */
    private static final int ATTACK_RANGE=280;
    /** 攻击触发帧为第3帧（0-indexed），投掷动作完成时 */
    private static final int ATTACK_FRAME=3;
    /** 攻击冷却中等，55帧 */
    private static final int ATTACK_COOLDOWN_FRAMES=55;

    /** 供GameLoad通过反射创建模板对象 */
    public GrenadierEnemy() {
    }

    /**
     * 使用配置数据创建投弹兵实体
     * @param x 初始世界横坐标
     * @param y 初始世界纵坐标
     * @param icon 初始动画帧图标
     * @param hp 生命值
     * @param attack 攻击力
     * @param patrolMinX 巡逻范围最小横坐标
     * @param patrolMaxX 巡逻范围最大横坐标
     */
    private GrenadierEnemy(int x, int y, ImageIcon icon, int hp, int attack,
                           int patrolMinX, int patrolMaxX) {
        super(x, y, icon, hp, attack);
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    /**
     * 按配置字符串创建投弹兵
     * <p>配置格式：x,y,hp,attack,patrolMinX,patrolMaxX
     * @param str 配置字符串
     * @return 投弹兵实例
     */
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        if (data.length != 6) {
            throw new IllegalArgumentException(
                    "投弹兵配置格式应为 x,y,hp,attack,patrolMinX,patrolMaxX");
        }

        int x=Integer.parseInt(data[0].trim());
        int y=Integer.parseInt(data[1].trim());
        int hp=Integer.parseInt(data[2].trim());
        int attack=Integer.parseInt(data[3].trim());
        int patrolMinX=Integer.parseInt(data[4].trim());
        int patrolMaxX=Integer.parseInt(data[5].trim());

        if (patrolMinX > patrolMaxX) {
            throw new IllegalArgumentException(
                    "投弹兵 patrolMinX 不能大于 patrolMaxX");
        }
        if (x < patrolMinX || x > patrolMaxX) {
            throw new IllegalArgumentException("投弹兵 x 必须位于巡逻区间内");
        }

        ImageIcon icon=GameLoad.getImages(MOVE_ANIMATION).get(0);
        return new GrenadierEnemy(x, y, icon, hp, attack, patrolMinX, patrolMaxX);
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