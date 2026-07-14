package cn.edu.scnu.element.boss;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

/**
 * TowerBoss - 第二关Boss（塔式坦克）
 * 
 * <p>特点：
 * <ul>
 * <li>第二关最终Boss，塔式坦克造型</li>
 * <li>生命值略高于TankBoss（配置值，默认350）</li>
 * <li>攻击力25，发射BossShell炮弹或多方向攻击</li>
 * <li>攻击冷却70帧，攻击频率比TankBoss略高</li>
 * <li>攻击触发帧为第2帧（0-indexed）</li>
 * </ul>
 * 
 * <p>行为模式：等待激活 → 激活后周期性攻击 → 被击败后播放残骸动画 → 触发游戏胜利
 * 
 * <p>设计注意：
 * <ul>
 * <li>TowerBoss位于第二关末尾（约3500像素位置）</li>
 * <li>玩家到达bossTriggerX（约3200像素）时被激活</li>
 * <li>死亡后播放boss2.wreck动画，然后删除</li>
 * <li>TowerBoss死亡意味着游戏胜利</li>
 * </ul>
 * 
 * @see AbstractBoss
 */
public class TowerBoss extends AbstractBoss {
    private static final String IDLE_ANIMATION="boss2.idle";
    private static final String ATTACK_ANIMATION="boss2.attack";
    private static final String WRECK_ANIMATION="boss2.wreck";
    private static final int IDLE_INTERVAL=3;
    private static final int ATTACK_INTERVAL=3;
    /** 攻击触发帧为第2帧（0-indexed） */
    private static final int ATTACK_FRAME=2;
    /** 攻击冷却70帧，比TankBoss略短 */
    private static final long ATTACK_COOLDOWN_FRAMES=70;

    /** 供GameLoad通过反射创建模板对象 */
    public TowerBoss() {
    }

    /**
     * 创建TowerBoss实体
     * @param x 初始世界横坐标
     * @param y 初始世界纵坐标
     * @param icon 初始动画帧图标
     * @param hp 生命值
     * @param attack 攻击力
     */
    private TowerBoss(int x, int y, ImageIcon icon, int hp, int attack) {
        super(x, y, icon, hp, attack);
    }

    /**
     * 按配置字符串创建TowerBoss
     * <p>配置格式：x,y,hp
     * @param str 配置字符串
     * @return TowerBoss实例
     */
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        if (data.length != 3) {
            throw new IllegalArgumentException("TowerBoss配置格式应为 x,y,hp");
        }

        int x=Integer.parseInt(data[0].trim());
        int y=Integer.parseInt(data[1].trim());
        int hp=Integer.parseInt(data[2].trim());

        ImageIcon icon=GameLoad.getImages(IDLE_ANIMATION).get(0);
        return new TowerBoss(x, y, icon, hp, 25);
    }

    @Override
    protected String getIdleAnimation() {
        return IDLE_ANIMATION;
    }

    @Override
    protected String getAttackAnimation() {
        return ATTACK_ANIMATION;
    }

    @Override
    protected String getWreckAnimation() {
        return WRECK_ANIMATION;
    }

    @Override
    protected int getIdleInterval() {
        return IDLE_INTERVAL;
    }

    @Override
    protected int getAttackInterval() {
        return ATTACK_INTERVAL;
    }

    @Override
    protected int getAttackFrame() {
        return ATTACK_FRAME;
    }

    @Override
    protected long getAttackCooldownFrames() {
        return ATTACK_COOLDOWN_FRAMES;
    }
}