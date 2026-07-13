package cn.edu.scnu.element.enemy;

//敌人当前行为状态
public enum EnemyState {
    IDLE, //等待攻击冷却结束
    MOVE, //巡逻或追踪玩家
    ATTACK, //播放攻击动画
    DEAD //播放死亡动画
}
