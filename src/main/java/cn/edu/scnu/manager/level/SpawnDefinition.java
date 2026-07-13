package cn.edu.scnu.manager.level;

/**
 * 单个敌人/人质的生成定义。
 * 从 spawn CSV 文件中读取构建。
 */

public class SpawnDefinition {
    private String type;  //敌人类型识别。
    //类型标识：rifleman / knife / bazooka / grenadier / heavyGunner / biker / hostage / boss1 / boss2
    private int x; //敌人生成X坐标
    private int y; //生成Y坐标
    private int patrolMinX; //巡逻最小X坐标
    private int patrolMaxX; //巡逻最大X坐标
    private int health; //生命值
    private int group;   //组编号，当进入某一场景后，敌人分组出发，成组出现或停止。

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPatrolMinX() {
        return patrolMinX;
    }

    public void setPatrolMinX(int patrolMinX) {
        this.patrolMinX = patrolMinX;
    }

    public int getPatrolMaxX() {
        return patrolMaxX;
    }

    public void setPatrolMaxX(int patrolMaxX) {
        this.patrolMaxX = patrolMaxX;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}