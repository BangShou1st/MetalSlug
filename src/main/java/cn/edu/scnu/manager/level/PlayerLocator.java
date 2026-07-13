package cn.edu.scnu.manager.level;

/**
 * 玩家定位接口。
 * 目的：让敌人获取玩家位置信息，而不直接 import 具体的 Player 类（低耦合）。
 * 实现方式：从 ElementManager 的 GameElement.PLAY 分类中取得第一个 live 对象。
 * 由成员A提供具体实现，C 只依赖此接口。
 */

import cn.edu.scnu.element.ElementObj;

public interface PlayerLocator {

    /**
     * 获取当前存活的玩家对象引用。
     * @return 玩家 ElementObj，如果没有存活玩家返回 null
     */

    ElementObj findPlayer();
}