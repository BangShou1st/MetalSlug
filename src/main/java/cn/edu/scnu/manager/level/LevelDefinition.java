package cn.edu.scnu.manager.level;

import java.util.ArrayList;
import java.util.List;

/**
 * 关卡定义数据类
 * 
 * <p>职责：存储单个关卡的完整配置信息
 * 
 * <p>Properties配置格式：
 * <ul>
 * <li>level.id - 关卡ID</li>
 * <li>level.mapKey - 地图资源键</li>
 * <li>level.worldWidth - 世界宽度</li>
 * <li>level.worldHeight - 世界高度</li>
 * <li>level.groundY - 地面高度</li>
 * <li>level.playerStartX - 玩家起始横坐标</li>
 * <li>level.playerStartY - 玩家起始纵坐标</li>
 * <li>level.bossTriggerX - Boss触发横坐标（玩家到达此位置激活Boss）</li>
 * <li>level.finishX - 关卡终点横坐标</li>
 * </ul>
 * 
 * @see SpawnDefinition
 */
public class LevelDefinition {
    /** 关卡ID */
    private int id;
    /** 地图资源键，对应ImageData.properties中的配置 */
    private String mapKey;
    /** 世界宽度（像素） */
    private int worldWidth;
    /** 世界高度（像素） */
    private int worldHeight;
    /** 地面高度（世界坐标系，玩家站立位置） */
    private int groundY;
    /** 玩家起始横坐标 */
    private int playerStartX;
    /** 玩家起始纵坐标 */
    private int playerStartY;
    /** Boss触发横坐标，玩家到达此位置后激活Boss */
    private int bossTriggerX;
    /** 关卡终点横坐标 */
    private int finishX;
    /** 生成点列表 */
    private List<SpawnDefinition> spawns=new ArrayList<>();

    public LevelDefinition() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id=id;
    }

    public String getMapKey() {
        return mapKey;
    }

    public void setMapKey(String mapKey) {
        this.mapKey=mapKey;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public void setWorldWidth(int worldWidth) {
        this.worldWidth=worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public void setWorldHeight(int worldHeight) {
        this.worldHeight=worldHeight;
    }

    public int getGroundY() {
        return groundY;
    }

    public void setGroundY(int groundY) {
        this.groundY=groundY;
    }

    public int getPlayerStartX() {
        return playerStartX;
    }

    public void setPlayerStartX(int playerStartX) {
        this.playerStartX=playerStartX;
    }

    public int getPlayerStartY() {
        return playerStartY;
    }

    public void setPlayerStartY(int playerStartY) {
        this.playerStartY=playerStartY;
    }

    public int getBossTriggerX() {
        return bossTriggerX;
    }

    public void setBossTriggerX(int bossTriggerX) {
        this.bossTriggerX=bossTriggerX;
    }

    public int getFinishX() {
        return finishX;
    }

    public void setFinishX(int finishX) {
        this.finishX=finishX;
    }

    public List<SpawnDefinition> getSpawns() {
        return spawns;
    }

    /**
     * 添加生成点
     * @param spawn 生成点定义
     */
    public void addSpawn(SpawnDefinition spawn) {
        spawns.add(spawn);
    }
}