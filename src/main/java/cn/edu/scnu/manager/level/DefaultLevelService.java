package cn.edu.scnu.manager.level;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 关卡服务默认实现
 * 
 * <p>职责：
 * <ul>
 * <li>从配置文件加载关卡定义（properties）</li>
 * <li>从CSV文件加载生成点配置</li>
 * <li>根据配置创建敌人生成点</li>
 * <li>检测玩家位置并触发Boss生成</li>
 * <li>检测Boss死亡并判定关卡完成</li>
 * </ul>
 * 
 * <p>设计注意：
 * <ul>
 * <li>配置文件位于src/main/resources/text/目录下</li>
 * <li>关卡配置文件名格式：level{id}.properties</li>
 * <li>生成点配置文件名格式：level{id}-spawns.csv</li>
 * <li>Boss不会在关卡加载时立即生成，而是在玩家到达触发点后才生成</li>
 * <li>关卡完成条件：Boss已生成且所有Boss死亡</li>
 * </ul>
 * 
 * @see LevelService
 */
public class DefaultLevelService implements LevelService {
    /** 关卡配置属性前缀 */
    private static final String LEVEL_PROPERTIES_PREFIX="level.";
    /** 配置文件路径 */
    private static final String TEXT_PATH="text/";

    /** 当前关卡ID */
    private int currentLevelId=0;
    /** 当前关卡定义 */
    private LevelDefinition currentLevel;
    /** 关卡是否完成 */
    private boolean levelComplete=false;
    /** Boss是否已生成 */
    private boolean bossSpawned=false;

    /**
     * 加载指定关卡
     * <p>清理当前世界元素，加载新关卡配置和生成点
     * @param levelId 关卡ID
     */
    @Override
    public void loadLevel(int levelId) {
        this.currentLevelId=levelId;
        this.levelComplete=false;
        this.bossSpawned=false;

        clearWorldElements();

        currentLevel=loadLevelDefinition(levelId);
        loadSpawns(currentLevel);
    }

    /**
     * 清理世界元素（不含玩家和UI）
     * <p>清理范围：MAPS、HOSTAGE、ENEMY、BOSS、PLAYFILE、ENEMYFILE、EFFECT
     */
    private void clearWorldElements() {
        ElementManager em=ElementManager.getManager();
        em.clearElement(GameElement.MAPS);
        em.clearElement(GameElement.HOSTAGE);
        em.clearElement(GameElement.ENEMY);
        em.clearElement(GameElement.BOSS);
        em.clearElement(GameElement.PLAYFILE);
        em.clearElement(GameElement.ENEMYFILE);
        em.clearElement(GameElement.EFFECT);
    }

    /**
     * 从properties文件加载关卡定义
     * @param levelId 关卡ID
     * @return 关卡定义对象
     */
    private LevelDefinition loadLevelDefinition(int levelId) {
        String propertiesFileName=String.format("level%d.properties", levelId);
        Properties properties=new Properties();

        try (InputStream in=DefaultLevelService.class.getClassLoader()
                .getResourceAsStream(TEXT_PATH + propertiesFileName)) {
            if (in == null) {
                throw new RuntimeException("关卡配置文件不存在: " + propertiesFileName);
            }
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("加载关卡配置失败: " + propertiesFileName, e);
        }

        LevelDefinition level=new LevelDefinition();
        level.setId(Integer.parseInt(properties.getProperty(LEVEL_PROPERTIES_PREFIX + "id")));
        level.setMapKey(properties.getProperty(LEVEL_PROPERTIES_PREFIX + "mapKey"));
        level.setWorldWidth(Integer.parseInt(properties.getProperty(LEVEL_PROPERTIES_PREFIX + "worldWidth")));
        level.setWorldHeight(Integer.parseInt(properties.getProperty(LEVEL_PROPERTIES_PREFIX + "worldHeight")));
        level.setGroundY(Integer.parseInt(properties.getProperty(LEVEL_PROPERTIES_PREFIX + "groundY")));
        level.setPlayerStartX(Integer.parseInt(properties.getProperty(LEVEL_PROPERTIES_PREFIX + "playerStartX")));
        level.setPlayerStartY(Integer.parseInt(properties.getProperty(LEVEL_PROPERTIES_PREFIX + "playerStartY")));
        level.setBossTriggerX(Integer.parseInt(properties.getProperty(LEVEL_PROPERTIES_PREFIX + "bossTriggerX")));
        level.setFinishX(Integer.parseInt(properties.getProperty(LEVEL_PROPERTIES_PREFIX + "finishX")));

        loadSpawnDefinitions(level, levelId);

        return level;
    }

    /**
     * 从CSV文件加载生成点配置
     * <p>CSV格式：type,x,y,patrolMinX,patrolMaxX,health,group
     * @param level 关卡定义对象
     * @param levelId 关卡ID
     */
    private void loadSpawnDefinitions(LevelDefinition level, int levelId) {
        String csvFileName=String.format("level%d-spawns.csv", levelId);

        try (InputStream in=DefaultLevelService.class.getClassLoader()
                .getResourceAsStream(TEXT_PATH + csvFileName)) {
            if (in == null) {
                throw new RuntimeException("生成配置文件不存在: " + csvFileName);
            }

            Scanner scanner=new Scanner(new InputStreamReader(in, StandardCharsets.UTF_8));
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line=scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts=line.split(",");
                if (parts.length >= 6) {
                    String type=parts[0].trim();
                    int x=Integer.parseInt(parts[1].trim());
                    int y=Integer.parseInt(parts[2].trim());
                    int patrolMinX=Integer.parseInt(parts[3].trim());
                    int patrolMaxX=Integer.parseInt(parts[4].trim());
                    int health=parts.length > 5 ? Integer.parseInt(parts[5].trim()) : 10;
                    int group=parts.length > 6 ? Integer.parseInt(parts[6].trim()) : 0;

                    level.addSpawn(new SpawnDefinition(type, x, y, patrolMinX, patrolMaxX, health, group));
                }
            }
            scanner.close();
        } catch (Exception e) {
            throw new RuntimeException("加载生成配置失败: " + csvFileName, e);
        }
    }

    /**
     * 根据生成点配置创建元素实例
     * <p>注意：Boss不会在此处生成，而是在玩家到达触发点后由spawnBoss()生成
     * @param level 关卡定义对象
     */
    private void loadSpawns(LevelDefinition level) {
        ElementManager em=ElementManager.getManager();

        for (SpawnDefinition spawn : level.getSpawns()) {
            /** Boss延迟生成，不在初始加载时创建 */
            if (spawn.getType().startsWith("boss")) {
                continue;
            }

            ElementObj element=createElement(spawn);
            if (element != null) {
                GameElement category=getElementCategory(spawn.getType());
                em.addElement(element, category);
            }
        }
    }

    /**
     * 根据生成点类型创建元素实例
     * @param spawn 生成点定义
     * @return 元素实例
     */
    private ElementObj createElement(SpawnDefinition spawn) {
        String objKey=getObjKey(spawn.getType());
        ElementObj template=GameLoad.getObj(objKey);
        if (template == null) {
            return null;
        }

        /** 构建配置字符串，格式：x,y,hp,attack,patrolMinX,patrolMaxX */
        String configStr=String.format("%d,%d,%d,%d,%d,%d",
                spawn.getX(), spawn.getY(), spawn.getHealth(), 10,
                spawn.getPatrolMinX(), spawn.getPatrolMaxX());

        return template.createElement(configStr);
    }

    /**
     * 将生成点类型转换为对象配置键
     * @param type 生成点类型
     * @return 对象配置键
     */
    private String getObjKey(String type) {
        switch (type.toLowerCase()) {
            case "rifleman":
                return "rifleman";
            case "knife":
                return "knife";
            case "bazooka":
                return "bazooka";
            case "grenadier":
                return "grenadier";
            case "heavyGunner":
            case "heavygunner":
                return "heavyGunner";
            case "biker":
                return "biker";
            case "hostage":
                return "hostage";
            case "boss1":
                return "tankBoss";
            case "boss2":
                return "towerBoss";
            default:
                return type;
        }
    }

    /**
     * 根据生成点类型获取元素分类
     * @param type 生成点类型
     * @return GameElement分类
     */
    private GameElement getElementCategory(String type) {
        if (type.startsWith("boss")) {
            return GameElement.BOSS;
        } else if ("hostage".equalsIgnoreCase(type)) {
            return GameElement.HOSTAGE;
        } else {
            return GameElement.ENEMY;
        }
    }

    /**
     * 每帧更新关卡状态
     * <p>负责：Boss触发检测、通关判定
     * @param gameTime 游戏时间
     */
    @Override
    public void update(long gameTime) {
        if (levelComplete) {
            return;
        }

        /** 检测玩家是否到达Boss触发点 */
        ElementObj player=findPlayer();
        if (player != null && !bossSpawned && currentLevel != null) {
            if (player.getX() >= currentLevel.getBossTriggerX()) {
                spawnBoss();
                bossSpawned=true;
            }
        }

        /** 检测Boss死亡 */
        checkBossDeath();
    }

    /**
     * 生成Boss并激活
     */
    private void spawnBoss() {
        if (currentLevel == null) {
            return;
        }

        ElementManager em=ElementManager.getManager();

        for (SpawnDefinition spawn : currentLevel.getSpawns()) {
            if (spawn.getType().startsWith("boss")) {
                String objKey=getObjKey(spawn.getType());
                ElementObj template=GameLoad.getObj(objKey);
                if (template != null) {
                    /** Boss配置格式：x,y,hp */
                    String configStr=String.format("%d,%d,%d",
                            spawn.getX(), spawn.getY(), spawn.getHealth());
                    ElementObj boss=template.createElement(configStr);
                    if (boss != null) {
                        em.addElement(boss, GameElement.BOSS);
                        /** 激活Boss使其开始攻击 */
                        if (boss instanceof cn.edu.scnu.element.boss.AbstractBoss) {
                            ((cn.edu.scnu.element.boss.AbstractBoss) boss).activate();
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查Boss是否全部死亡
     * <p>当Boss已生成且所有Boss死亡时，设置levelComplete=true
     */
    private void checkBossDeath() {
        ElementManager em=ElementManager.getManager();
        List<ElementObj> bosses=em.getElementByKey(GameElement.BOSS);

        boolean allDead=true;
        for (ElementObj boss : bosses) {
            if (boss.isLive()) {
                allDead=false;
                break;
            }
        }

        /** 只有Boss已生成且存在Boss且全部死亡时，才判定关卡完成 */
        if (bossSpawned && bosses.size() > 0 && allDead) {
            levelComplete=true;
        }
    }

    /** 从ElementManager查找第一个存活的玩家 */
    private ElementObj findPlayer() {
        for (ElementObj player : ElementManager.getManager()
                .getElementByKey(GameElement.PLAY)) {
            if (player.isLive()) {
                return player;
            }
        }
        return null;
    }

    @Override
    public boolean isLevelComplete() {
        return levelComplete;
    }

    @Override
    public int getCurrentLevelId() {
        return currentLevelId;
    }

    /**
     * 获取当前关卡定义（供外部读取关卡配置信息）
     * @return 当前关卡定义
     */
    public LevelDefinition getCurrentLevel() {
        return currentLevel;
    }
}