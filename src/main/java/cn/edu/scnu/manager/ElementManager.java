package cn.edu.scnu.manager;

import cn.edu.scnu.element.ElementObj;

import java.util.*;

/**
 * @说明 本类是元素管理器，专门存储所有的元素，同时，提供方法
 *       给予视图和控制获取数据
 * 管理器是视图和控制要访问，管理器就必须只有一个，单例模式
 */

public class ElementManager {

    private final Map<GameElement, List<ElementObj>> gameElements; //保存所有游戏元素

    private static ElementManager EM=null; //懒汉式单例

    private ElementManager() {
        gameElements = new HashMap<>();
        init();
    }

    //初始化游戏元素分类
    private void init() {
        for (GameElement ge : GameElement.values()) { //将每种元素集合都放入到map中
            gameElements.put(ge,Collections.synchronizedList(new ArrayList<ElementObj>()));
        }
    }

    //获取唯一的元素管理器
    public static synchronized ElementManager getManager() {
        if (EM == null) {   //空值判定
            EM=new ElementManager();
        }
        return EM;
    }

    //返回实时列表，仅供游戏逻辑层更新、碰撞和装配使用
    public List<ElementObj> getElementByKey(GameElement ge) {
        return gameElements.get(ge);
    }

    //返回指定元素分类的线程安全只读快照
    public List<ElementObj> getElementSnapshot(GameElement ge) {
        List<ElementObj> elements=gameElements.get(ge);
        synchronized(elements) {
            return new ArrayList<ElementObj>(elements);
        }
    }

    //添加元素
    public void addElement(ElementObj obj,GameElement ge) {
        gameElements.get(ge).add(obj); //添加对象到集合中，按key值进行存储
    }

    //删除指定游戏元素
    public boolean removeElement(ElementObj obj,GameElement ge) {
        return gameElements.get(ge).remove(obj);
    }

    //清空指定类型的游戏元素
    public void clearElement(GameElement ge) {
        List<ElementObj> elements=gameElements.get(ge);
        synchronized(elements) {
            elements.clear();
        }
    }

    /**
     * 清空所有游戏元素
     * 可在关卡切换或重新开始游戏时调用
     */
    public void clearAll() {
        for (List<ElementObj> elements : gameElements.values()) {
            synchronized(elements) {
                elements.clear();
            }
        }
    }

    //获取指定类型的元素数量
    public int size(GameElement gameElement) {
        List<ElementObj> elements=gameElements.get(gameElement);
        synchronized(elements) {
            return elements.size();
        }
    }
}
