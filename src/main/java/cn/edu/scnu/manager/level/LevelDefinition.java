package cn.edu.scnu.manager.level;

/**
 * 关卡定义数据类。
 * 包含一关的全部静态配置：地图、世界尺寸、地面高度、玩家出生点、Boss 触发位置、生成点列表。
 * 从 level.properties 文件中读取构建。
 */

import java.util.List;

public class LevelDefinition {
    private int id; //关卡编号
    private String mapKey; //地图图片键
    private int worldWidth; //世界宽度（像素，已乘以缩放比例）
    private int worldHeight; //世界高度（像素，已乘以缩放比例）
    private int groundY; //地面Y坐标（世界坐标）
    private int playerStartX; //玩家出生点X坐标
    private int playerStartY; //玩家出生点Y坐标
    private int bossTriggerX; //Boss 触发位置X坐标
    private int finishX; //通过关卡结束位置X坐标
    private List<SpawnDefinition> spawns;  //本关卡所有（敌人）生成点

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMapKey() {
        return mapKey;
    }

    public void setMapKey(String mapKey) {
        this.mapKey = mapKey;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public void setWorldWidth(int worldWidth) {
        this.worldWidth = worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public void setWorldHeight(int worldHeight) {
        this.worldHeight = worldHeight;
    }

    public int getGroundY() {
        return groundY;
    }

    public void setGroundY(int groundY) {
        this.groundY = groundY;
    }

    public int getPlayerStartX() {
        return playerStartX;
    }

    public void setPlayerStartX(int playerStartX) {
        this.playerStartX = playerStartX;
    }

    public int getPlayerStartY() {
        return playerStartY;
    }

    public void setPlayerStartY(int playerStartY) {
        this.playerStartY = playerStartY;
    }

    public int getBossTriggerX() {
        return bossTriggerX;
    }

    public void setBossTriggerX(int bossTriggerX) {
        this.bossTriggerX = bossTriggerX;
    }

    public int getFinishX() {
        return finishX;
    }

    public void setFinishX(int finishX) {
        this.finishX = finishX;
    }

    public List<SpawnDefinition> getSpawns() {
        return spawns;
    }

    public void setSpawns(List<SpawnDefinition> spawns) {
        this.spawns = spawns;
    }
}