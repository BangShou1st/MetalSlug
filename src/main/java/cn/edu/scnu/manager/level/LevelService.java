package cn.edu.scnu.manager.level;

/**
 * 关卡服务接口
 * 
 * <p>职责：定义关卡加载、更新和完成判定的标准接口
 * 
 * <p>设计目的：
 * <ul>
 * <li>提供统一的关卡管理入口，供GameThread调用</li>
 * <li>解耦关卡逻辑与游戏主线程</li>
 * <li>便于未来扩展多关卡和动态关卡生成</li>
 * </ul>
 * 
 * <p>使用流程：
 * <ul>
 * <li>GameThread调用loadLevel(int)加载关卡</li>
 * <li>每帧调用update(long)更新关卡状态</li>
 * <li>检查isLevelComplete()判断是否通关</li>
 * </ul>
 */
public interface LevelService {
    /**
     * 加载指定关卡
     * <p>会清理当前世界元素并重新加载新关卡配置
     * @param levelId 关卡ID（1或2）
     */
    void loadLevel(int levelId);

    /**
     * 每帧更新关卡状态
     * <p>负责Boss触发检测和通关判定
     * @param gameTime 游戏时间
     */
    void update(long gameTime);

    /**
     * 检查当前关卡是否完成
     * <p>当Boss被击败且所有Boss死亡时返回true
     * @return 关卡是否完成
     */
    boolean isLevelComplete();

    /**
     * 获取当前关卡ID
     * @return 当前关卡ID
     */
    int getCurrentLevelId();
}