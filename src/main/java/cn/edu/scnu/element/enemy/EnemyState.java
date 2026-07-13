package cn.edu.scnu.element.enemy;

//敌人状态枚举，所有普通敌人（除去BOSS）共享此状态机

public enum EnemyState {
    PATROL, //巡逻（在指定范围内来回移动）
    CHASE, //追击（检测到玩家后快速接近）
    ATTACK, //攻击（进入射程后执行攻击动画）
    HURT, //受伤（敌人短暂僵直）
    DEAD //死亡
}