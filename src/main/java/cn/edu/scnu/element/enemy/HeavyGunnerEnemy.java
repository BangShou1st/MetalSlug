package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

/**
 * 重机枪兵 - 高血量火力压制单位
 * 
 * <p>特点：
 * <ul>
 * <li>生命值最高，是普通敌人中最耐打的单位</li>
 * <li>移动速度最慢（0.8），机动性差</li>
 * <li>攻击时连续发射多发子弹，火力压制效果强</li>
 * <li>攻击间隔短（2帧），攻击动画播放速度快</li>
 * <li>攻击冷却最长（80帧），一轮射击后需要较长时间恢复</li>
 * <li>适合布置在固定防御点，提供持续火力输出</li>
 * </ul>
 * 
 * <p>行为模式：巡逻 → 发现玩家（380像素内）→ 接近至攻击范围（300像素）→ 连续射击 → 长冷却等待
 * 
 * @see AbstractEnemy
 */
public class HeavyGunnerEnemy extends AbstractEnemy {
    private static final String MOVE_ANIMATION="enemy.heavyGunner.move";
    private static final String ATTACK_ANIMATION="enemy.heavyGunner.attack";
    private static final String DEATH_ANIMATION="enemy.death";
    /** 移动动画间隔较长，4帧换一帧，体现笨重感 */
    private static final int MOVE_INTERVAL=4;
    /** 攻击动画间隔短，2帧换一帧，体现高射速 */
    private static final int ATTACK_INTERVAL=2;
    private static final int DEATH_INTERVAL=2;
    /** 移动速度最慢，0.8像素/帧，体现重装特点 */
    private static final double MOVE_SPEED=0.8;
    /** 发现范围较大，380像素 */
    private static final int DETECT_RANGE=380;
    /** 攻击范围中等，300像素 */
    private static final int ATTACK_RANGE=300;
    /** 攻击触发帧为第1帧（0-indexed），射击动作早期触发 */
    private static final int ATTACK_FRAME=1;
    /** 攻击冷却最长，80帧，一轮射击后需要长时间恢复 */
    private static final int ATTACK_COOLDOWN_FRAMES=80;

    /** 供GameLoad通过反射创建模板对象 */
    public HeavyGunnerEnemy() {
    }

    /**
     * 使用配置数据创建重机枪兵实体
     * @param x 初始世界横坐标
     * @param y 初始世界纵坐标
     * @param icon 初始动画帧图标
     * @param hp 生命值
     * @param attack 攻击力
     * @param patrolMinX 巡逻范围最小横坐标
     * @param patrolMaxX 巡逻范围最大横坐标
     */
    private HeavyGunnerEnemy(int x, int y, ImageIcon icon, int hp, int attack,
                             int patrolMinX, int patrolMaxX) {
        super(x, y, icon, hp, attack);
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    /**
     * 按配置字符串创建重机枪兵
     * <p>配置格式：x,y,hp,attack,patrolMinX,patrolMaxX
     * @param str 配置字符串
     * @return 重机枪兵实例
     */
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        if (data.length != 6) {
            throw new IllegalArgumentException(
                    "重机枪兵配置格式应为 x,y,hp,attack,patrolMinX,patrolMaxX");
        }

        int x=Integer.parseInt(data[0].trim());
        int y=Integer.parseInt(data[1].trim());
        int hp=Integer.parseInt(data[2].trim());
        int attack=Integer.parseInt(data[3].trim());
        int patrolMinX=Integer.parseInt(data[4].trim());
        int patrolMaxX=Integer.parseInt(data[5].trim());

        if (patrolMinX > patrolMaxX) {
            throw new IllegalArgumentException(
                    "重机枪兵 patrolMinX 不能大于 patrolMaxX");
        }
        if (x < patrolMinX || x > patrolMaxX) {
            throw new IllegalArgumentException("重机枪兵 x 必须位于巡逻区间内");
        }

        ImageIcon icon=GameLoad.getImages(MOVE_ANIMATION).get(0);
        return new HeavyGunnerEnemy(x, y, icon, hp, attack, patrolMinX, patrolMaxX);
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