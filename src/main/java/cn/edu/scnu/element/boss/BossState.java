package cn.edu.scnu.element.boss;

/**
 * Boss状态枚举
 * 
 * <p>状态转换流程：
 * <ul>
 * <li>IDLE ↔ ATTACK_PRIMARY ↔ ATTACK_SPECIAL ↔ HURT → DEAD</li>
 * </ul>
 * 
 * <p>说明：
 * <ul>
 * <li>IDLE：空闲状态，等待攻击冷却结束</li>
 * <li>ATTACK_PRIMARY：主攻击状态，使用常规攻击方式</li>
 * <li>ATTACK_SPECIAL：特殊攻击状态，使用强力攻击方式（预留，当前版本未实现）</li>
 * <li>HURT：受伤状态，受击后的短暂硬直（预留，当前版本未实现独立动画）</li>
 * <li>DEAD：死亡状态，播放残骸动画</li>
 * </ul>
 */
public enum BossState {
    IDLE,
    ATTACK_PRIMARY,
    ATTACK_SPECIAL,
    HURT,
    DEAD
}