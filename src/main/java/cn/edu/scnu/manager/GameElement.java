package cn.edu.scnu.manager;

/**
 * 游戏元素分类
 * 枚举声明顺序也是默认绘制顺序：越靠后的元素越显示在上层
 */

public enum GameElement {
    MAPS, // 地图、背景
    HOSTAGE, // 人质
    ENEMY, // 普通敌人
    BOSS, // Boss
    PLAY, // 玩家
    PLAYFILE, //玩家发射物  可以放子弹、火箭、手榴弹
    ENEMYFILE, //敌人和 Boss 发射物
    EFFECT, //爆炸、命中、死亡等特效
    UI // 血量、弹药、关卡提示
}