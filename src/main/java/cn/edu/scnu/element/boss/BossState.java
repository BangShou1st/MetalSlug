package cn.edu.scnu.element.boss;

//Boss 当前行为状态
public enum BossState {
    IDLE, //尚未攻击或等待冷却
    ATTACK, //播放攻击动画
    DEAD //显示被摧毁后的残骸
}
