package cn.edu.scnu.element.hostage;

/**
 * 人质状态枚举
 * 
 * <p>状态转换流程：
 * <ul>
 * <li>IDLE → RESCUED → RUNNING → LEFT</li>
 * </ul>
 * 
 * <p>说明：
 * <ul>
 * <li>IDLE：等待被营救状态，原地站立</li>
 * <li>RESCUED：被玩家营救状态，切换为逃跑动画前的过渡状态</li>
 * <li>RUNNING：逃跑中，向右移动离开战场</li>
 * <li>LEFT：已离开世界范围，准备被删除</li>
 * </ul>
 */
public enum HostageState {
    IDLE,
    RESCUED,
    RUNNING,
    LEFT
}