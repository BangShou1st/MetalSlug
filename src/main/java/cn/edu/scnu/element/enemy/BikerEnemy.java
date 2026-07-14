package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

/**
 * 摩托兵 - 高速机动敌方单位
 * 
 * <p>特点：
 * <ul>
 * <li>移动速度最快（4.0），远超其他兵种</li>
 * <li>发现范围最大（500像素），能在很远距离发现玩家</li>
 * <li>攻击范围中等（200像素），进入射程后发射火箭</li>
 * <li>移动动画间隔最短（1帧），体现高速移动感</li>
 * <li>与其他步兵不同，巡逻范围较大，可快速穿越战场</li>
 * <li>离开世界边界后自动删除</li>
 * </ul>
 * 
 * <p>行为模式：高速巡逻 → 发现玩家（500像素内）→ 快速接近 → 进入攻击范围（200像素）→ 发射火箭 → 可能冲过玩家继续移动
 * 
 * <p>设计注意：摩托兵的高速特性使其行为模式与其他步兵明显不同，它不会在攻击后停留，而是继续高速移动
 * 
 * @see AbstractEnemy
 */
public class BikerEnemy extends AbstractEnemy {
    private static final String MOVE_ANIMATION="enemy.biker.move";
    private static final String ATTACK_ANIMATION="enemy.biker.attack";
    private static final String DEATH_ANIMATION="enemy.death";
    /** 移动动画间隔最短，1帧换一帧，体现高速移动 */
    private static final int MOVE_INTERVAL=1;
    private static final int ATTACK_INTERVAL=2;
    private static final int DEATH_INTERVAL=2;
    /** 移动速度最快，4.0像素/帧，远超其他兵种 */
    private static final double MOVE_SPEED=4.0;
    /** 发现范围最大，500像素 */
    private static final int DETECT_RANGE=500;
    /** 攻击范围中等，200像素 */
    private static final int ATTACK_RANGE=200;
    /** 攻击触发帧为第2帧（0-indexed） */
    private static final int ATTACK_FRAME=2;
    /** 攻击冷却中等，45帧 */
    private static final int ATTACK_COOLDOWN_FRAMES=45;

    /** 供GameLoad通过反射创建模板对象 */
    public BikerEnemy() {
    }

    /**
     * 使用配置数据创建摩托兵实体
     * @param x 初始世界横坐标
     * @param y 初始世界纵坐标
     * @param icon 初始动画帧图标
     * @param hp 生命值
     * @param attack 攻击力
     * @param patrolMinX 巡逻范围最小横坐标
     * @param patrolMaxX 巡逻范围最大横坐标
     */
    private BikerEnemy(int x, int y, ImageIcon icon, int hp, int attack,
                       int patrolMinX, int patrolMaxX) {
        super(x, y, icon, hp, attack);
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    /**
     * 按配置字符串创建摩托兵
     * <p>配置格式：x,y,hp,attack,patrolMinX,patrolMaxX
     * @param str 配置字符串
     * @return 摩托兵实例
     */
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        if (data.length != 6) {
            throw new IllegalArgumentException(
                    "摩托兵配置格式应为 x,y,hp,attack,patrolMinX,patrolMaxX");
        }

        int x=Integer.parseInt(data[0].trim());
        int y=Integer.parseInt(data[1].trim());
        int hp=Integer.parseInt(data[2].trim());
        int attack=Integer.parseInt(data[3].trim());
        int patrolMinX=Integer.parseInt(data[4].trim());
        int patrolMaxX=Integer.parseInt(data[5].trim());

        if (patrolMinX > patrolMaxX) {
            throw new IllegalArgumentException(
                    "摩托兵 patrolMinX 不能大于 patrolMaxX");
        }
        if (x < patrolMinX || x > patrolMaxX) {
            throw new IllegalArgumentException("摩托兵 x 必须位于巡逻区间内");
        }

        ImageIcon icon=GameLoad.getImages(MOVE_ANIMATION).get(0);
        return new BikerEnemy(x, y, icon, hp, attack, patrolMinX, patrolMaxX);
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