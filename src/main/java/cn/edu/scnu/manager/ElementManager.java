package cn.edu.scnu.manager;

import cn.edu.scnu.element.ElementObj;

import java.util.*;

/** 按类型集中管理游戏元素的单例。 */

public class ElementManager {

    private final Map<GameElement, List<ElementObj>> gameElements; //保存所有游戏元素

    private static ElementManager EM=null; //单例实例

    private ElementManager() {
        gameElements = new HashMap<>();
        init();
    }

    //初始化元素分类
    private void init() {
        for (GameElement ge : GameElement.values()) {
            gameElements.put(ge,Collections.synchronizedList(new ArrayList<ElementObj>()));
        }
    }

    //获取唯一的元素管理器
    public static synchronized ElementManager getManager() {
        //首次访问时创建单例
        if (EM == null) {
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
        //同步复制，避免绘制时遍历正在修改的列表
        synchronized(elements) {
            return new ArrayList<ElementObj>(elements);
        }
    }

    public void addElement(ElementObj obj,GameElement ge) {
        gameElements.get(ge).add(obj);
    }

    /** 清空所有游戏元素。 */
    public void clearAll() {
        //逐类清空当前关卡对象
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
