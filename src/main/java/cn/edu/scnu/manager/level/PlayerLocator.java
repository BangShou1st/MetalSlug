package cn.edu.scnu.manager.level;

import cn.edu.scnu.element.ElementObj;

/**
 * 玩家定位器接口
 * 
 * <p>设计目的：
 * <ul>
 * <li>为敌人AI提供查找玩家的能力，同时避免直接依赖具体的Player类</li>
 * <li>实现低耦合原则，敌人只读取目标位置和碰撞矩形，不访问Player的具体方法</li>
 * </ul>
 * 
 * <p>实现说明：
 * <ul>
 * <li>实现类可从GameElement.PLAY中获取第一个存活的玩家对象</li>
 * <li>敌人只读取：目标x/y坐标、Rectangle、live状态</li>
 * <li>若需访问生命值，应通过只读接口，不强转为Player</li>
 * </ul>
 */
public interface PlayerLocator {
    /**
     * 查找当前存活的玩家
     * @return 玩家对象，若无存活玩家返回null
     */
    ElementObj findPlayer();
}