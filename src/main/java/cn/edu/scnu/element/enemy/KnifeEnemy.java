package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

/**
 * 刀兵 - 近战敌方单位
 * 
 * <p>特点：
 * <ul>
 * <li>移动速度最快（2.5），接近战攻击</li>
 * <li>发现范围最小（200像素），只有靠近玩家才会被激活</li>
 * <li>攻击范围极短（40像素），必须贴身才能攻击</li>
 * <li>攻击冷却较短（30帧），攻击频率高</li>
 * <li>使用近战判定框（MeleeHitBox）造成接触伤害（待成员B接入WeaponService）</li>
 * </ul>
 * 
 * <p>行为模式：巡逻 → 发现玩家（200像素内）→ 高速追击 → 进入攻击范围（40像素）→ 近战攻击 → 继续追击
 * 
 * @see AbstractEnemy
 */
public class KnifeEnemy extends AbstractEnemy {
    private static final String MOVE_ANIMATION="enemy.knife.move";
    private static final String ATTACK_ANIMATION="enemy.knife.attack";
    private static final String DEATH_ANIMATION="enemy.death";
    private static final int MOVE_INTERVAL=2;
    private static final int ATTACK_INTERVAL=2;
    private static final int DEATH_INTERVAL=2;
    /** 移动速度最快，2.5像素/帧 */
    private static final double MOVE_SPEED=2.5;
    /** 发现范围最小，仅200像素 */
    private static final int DETECT_RANGE=200;
    /** 攻击范围极短，40像素，必须贴身攻击 */
    private static final int ATTACK_RANGE=40;
    /** 攻击触发帧为第2帧（0-indexed） */
    private static final int ATTACK_FRAME=2;
    /** 攻击冷却较短，30帧 */
    private static final int ATTACK_COOLDOWN_FRAMES=30;

    /** 供GameLoad通过反射创建模板对象 */
    public KnifeEnemy() {
    }

    /**
     * 使用配置数据创建刀兵实体
     * @param x 初始世界横坐标
     * @param y 初始世界纵坐标
     * @param icon 初始动画帧图标
     * @param hp 生命值
     * @param attack 攻击力
     * @param patrolMinX 巡逻范围最小横坐标
     * @param patrolMaxX 巡逻范围最大横坐标
     */
    private KnifeEnemy(int x, int y, ImageIcon icon, int hp, int attack,
                       int patrolMinX, int patrolMaxX) {
        super(x, y, icon, hp, attack);
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    /**
     * 按配置字符串创建刀兵
     * <p>配置格式：x,y,hp,attack,patrolMinX,patrolMaxX
     * @param str 配置字符串
     * @return 刀兵实例
     */
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        if (data.length != 6) {
            throw new IllegalArgumentException(
                    "刀兵配置格式应为 x,y,hp,attack,patrolMinX,patrolMaxX");
        }

        int x=Integer.parseInt(data[0].trim());
        int y=Integer.parseInt(data[1].trim());
        int hp=Integer.parseInt(data[2].trim());
        int attack=Integer.parseInt(data[3].trim());
        int patrolMinX=Integer.parseInt(data[4].trim());
        int patrolMaxX=Integer.parseInt(data[5].trim());

        if (patrolMinX > patrolMaxX) {
            throw new IllegalArgumentException(
                    "刀兵 patrolMinX 不能大于 patrolMaxX");
        }
        if (x < patrolMinX || x > patrolMaxX) {
            throw new IllegalArgumentException("刀兵 x 必须位于巡逻区间内");
        }

        ImageIcon icon=GameLoad.getImages(MOVE_ANIMATION).get(0);
        return new KnifeEnemy(x, y, icon, hp, attack, patrolMinX, patrolMaxX);
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