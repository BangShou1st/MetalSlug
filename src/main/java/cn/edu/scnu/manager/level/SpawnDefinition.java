package cn.edu.scnu.manager.level;

/**
 * 生成点定义数据类
 * 
 * <p>职责：存储单个敌人生成点的配置信息
 * 
 * <p>CSV配置格式：type,x,y,patrolMinX,patrolMaxX,health,group
 * 
 * @see LevelDefinition
 */
public class SpawnDefinition {
    /** 类型标识：rifleman/knife/bazooka/grenadier/heavyGunner/biker/hostage/boss1/boss2 */
    private String type;
    /** 生成位置横坐标 */
    private int x;
    /** 生成位置纵坐标 */
    private int y;
    /** 巡逻范围最小横坐标 */
    private int patrolMinX;
    /** 巡逻范围最大横坐标 */
    private int patrolMaxX;
    /** 生命值 */
    private int health;
    /** 生成组号，用于分批生成或分组管理（预留） */
    private int group;

    /**
     * 创建生成点定义
     * @param type 类型标识
     * @param x 生成位置横坐标
     * @param y 生成位置纵坐标
     * @param patrolMinX 巡逻范围最小横坐标
     * @param patrolMaxX 巡逻范围最大横坐标
     * @param health 生命值
     * @param group 生成组号
     */
    public SpawnDefinition(String type, int x, int y, int patrolMinX, int patrolMaxX, int health, int group) {
        this.type=type;
        this.x=x;
        this.y=y;
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        this.health=health;
        this.group=group;
    }

    public String getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getPatrolMinX() {
        return patrolMinX;
    }

    public int getPatrolMaxX() {
        return patrolMaxX;
    }

    public int getHealth() {
        return health;
    }

    public int getGroup() {
        return group;
    }
}