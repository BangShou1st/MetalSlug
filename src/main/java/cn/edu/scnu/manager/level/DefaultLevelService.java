package cn.edu.scnu.manager.level;

/**
 * 关卡服务默认实现。
 *
 * 职责：
 * - 读取关卡配置文件（.properties + .csv）
 * - 按配置生成敌人、人质等对象
 * - 管理 Boss 触发逻辑
 * - 判断关卡完成条件
 *
 * 不负责：
 * - 不修改公共框架
 * - 不创建线程
 * - 不处理碰撞
 */

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.enemy.RiflemanEnemy;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class DefaultLevelService implements LevelService {

    private int currentLevelId;
    private boolean levelComplete = false;
    private LevelDefinition currentLevel;
    private PlayerLocator playerLocator;
    // private CameraReadable camera;  // 后续需要时注入

    private boolean bossSpawned = false; //Boss触发状态，默认未触发

    public DefaultLevelService(PlayerLocator playerLocator) {
        this.playerLocator = playerLocator;
    }

    @Override
    public void loadLevel(int levelId) {
        this.currentLevelId = levelId;
        this.levelComplete = false;
        this.bossSpawned = false;
        // 1. 清理旧对象（注意：与 A 确认是否需要保留 UI 和 Player）
        ElementManager em = ElementManager.getManager();
        em.clearElement(GameElement.MAPS);
        em.clearElement(GameElement.HOSTAGE);
        em.clearElement(GameElement.ENEMY);
        em.clearElement(GameElement.BOSS);
        em.clearElement(GameElement.PLAYFILE);
        em.clearElement(GameElement.ENEMYFILE);
        em.clearElement(GameElement.EFFECT);

        // 2. 读取关卡配置
        currentLevel = loadLevelDefinition(levelId);

        // 3. 按 spawn 配置生成敌人
        for (SpawnDefinition spawn : currentLevel.getSpawns()) {
            ElementObj obj = createEntity(spawn);
            if (obj != null) {
                GameElement category = getCategory(spawn.getType());
                em.addElement(obj, category);
            }
        }
    }

    @Override
    public void update(long gameTime) {
        if (levelComplete) return;

        // 检查是否需要触发 Boss
        if (!bossSpawned && currentLevel != null) {
            ElementObj player = playerLocator.findPlayer();
            if (player != null && player.getX() >= currentLevel.getBossTriggerX()) {
                spawnBoss();
                bossSpawned = true;
            }
        }

        // 检查 Boss 是否死亡 → 关卡完成
        if (bossSpawned) {
            List<ElementObj> bosses = ElementManager.getManager()
                    .getElementByKey(GameElement.BOSS);
            if (bosses.isEmpty()) {
                levelComplete = true;
            }
        }
    }

    @Override
    public boolean isLevelComplete() {
        return levelComplete;
    }

    /**
     * 从 properties 文件加载关卡定义。
     */
    private LevelDefinition loadLevelDefinition(int levelId) {
        LevelDefinition def = new LevelDefinition();
        String filename = "text/level" + levelId + ".properties";

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is == null) {
                // 配置文件不存在时返回默认值（开发阶段）
                return createDefaultLevel(levelId);
            }
            Properties props = new Properties();
            props.load(is);

            def.setId(Integer.parseInt(props.getProperty("level.id")));
            def.setMapKey(props.getProperty("level.mapKey"));
            def.setWorldWidth(Integer.parseInt(props.getProperty("level.worldWidth")));
            def.setWorldHeight(Integer.parseInt(props.getProperty("level.worldHeight")));
            def.setGroundY(Integer.parseInt(props.getProperty("level.groundY")));
            def.setPlayerStartX(Integer.parseInt(props.getProperty("level.playerStartX")));
            def.setPlayerStartY(Integer.parseInt(props.getProperty("level.playerStartY")));
            def.setBossTriggerX(Integer.parseInt(props.getProperty("level.bossTriggerX")));
            def.setFinishX(Integer.parseInt(props.getProperty("level.finishX")));

            // 加载 spawn 配置
            def.setSpawns(loadSpawns(levelId));

        } catch (Exception e) {
            System.err.println("加载关卡配置失败: " + e.getMessage());
            return createDefaultLevel(levelId);
        }

        return def;
    }

    //从 CSV 文件加载生成点列表。
    private List<SpawnDefinition> loadSpawns(int levelId) {
        List<SpawnDefinition> spawns = new ArrayList<>();
        String filename = "text/level" + levelId + "-spawns.csv";

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is == null) return spawns;
            Scanner scanner = new Scanner(is, "UTF-8");
            if (scanner.hasNextLine()) scanner.nextLine();//跳过表头

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                SpawnDefinition sd = new SpawnDefinition();
                sd.setType(parts[0].trim());
                sd.setX(Integer.parseInt(parts[1].trim()));
                sd.setY(Integer.parseInt(parts[2].trim()));
                sd.setPatrolMinX(Integer.parseInt(parts[3].trim()));
                sd.setPatrolMaxX(Integer.parseInt(parts[4].trim()));
                spawns.add(sd);
            }
        } catch (Exception e) {
            System.err.println("加载生成点配置失败: " + e.getMessage());
        }

        return spawns;
    }

    /**
     * 根据 spawn 类型创建对应的实体对象。
     * 使用工厂映射，不使用反射。
     */
    private ElementObj createEntity(SpawnDefinition spawn) {
        switch (spawn.getType().toLowerCase()) {
            case "rifleman":
                RiflemanEnemy rifleman = new RiflemanEnemy(spawn.getX(), spawn.getY());
                rifleman.setPatrolRange(spawn.getPatrolMinX(), spawn.getPatrolMaxX());
                return rifleman;
            // 后续添加更多敌人类型：
            // case "knife": return new KnifeEnemy(...);
            // case "bazooka": return new BazookaEnemy(...);
            // case "hostage": return new Hostage(...);
            // case "boss1": return new TankBoss(...);
            // case "boss2": return new TowerBoss(...);
            default:
                System.err.println("未知的生成类型: " + spawn.getType());
                return null;
        }
    }

    //根据类型返回对应的 GameElement 分类。
    private GameElement getCategory(String type) {
        switch (type.toLowerCase()) {
            case "rifleman":
            case "knife":
            case "bazooka":
            case "grenadier":
            case "heavygunner":
            case "biker":
                return GameElement.ENEMY;
            case "boss1":
            case "boss2":
                return GameElement.BOSS;
            case "hostage":
                return GameElement.HOSTAGE;
            default:
                return GameElement.ENEMY;
        }
    }

    //生成 Boss（后续实现，第一天留占位）。
    private void spawnBoss() {
        // 后续：根据 currentLevelId 创建对应 Boss
        // Level 1 → TankBoss
        // Level 2 → TowerBoss
    }

    //创建默认关卡数据（配置文件不存在时使用）。
    private LevelDefinition createDefaultLevel(int levelId) {
        LevelDefinition def = new LevelDefinition();
        def.setId(levelId);
        def.setMapKey("map.level1");
        def.setWorldWidth(5565);
        def.setWorldHeight(570);
        def.setGroundY(500);
        def.setPlayerStartX(120);
        def.setPlayerStartY(430);
        def.setBossTriggerX(4700);
        def.setFinishX(5400);

        // 默认只生成一个步枪兵用于测试
        List<SpawnDefinition> spawns = new ArrayList<>();
        SpawnDefinition sd = new SpawnDefinition();
        sd.setType("rifleman");
        sd.setX(700);
        sd.setY(430);
        sd.setPatrolMinX(620);
        sd.setPatrolMaxX(850);
        spawns.add(sd);
        def.setSpawns(spawns);

        return def;
    }
}