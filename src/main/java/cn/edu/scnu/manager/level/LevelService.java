package cn.edu.scnu.manager.level;

/**
 * 关卡服务接口（第一天冻结）。
 * 由成员A在 GameThread 中每帧调用 update()，并查询 isLevelComplete() 判断是否过关。
 * 不要返回 Swing 面板或任何 View 相关对象。
 */

public interface LevelService {


    void loadLevel(int levelId); //加载指定关卡（清理旧对象，读取配置，按照spawn数据生成敌人和人质 levelId为关卡编号，1或2）
    void update(long gameTime); //每帧更新，检查Boss是否被出发、关卡是否完成。
    boolean isLevelComplete(); //判断关卡是否完成 当Boss死亡后，判断为true。
}