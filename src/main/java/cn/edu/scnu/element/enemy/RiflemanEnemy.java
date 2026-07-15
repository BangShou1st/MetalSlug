package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.effect.MuzzleEffect;
import cn.edu.scnu.element.weapon.EnemyBullet;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

//使用固定配置字符串创建并驱动步枪兵行为
public class RiflemanEnemy extends AbstractEnemy {
    private static final String MOVE_ANIMATION="enemy.rifleman.move"; //步枪兵移动动画键
    private static final String ATTACK_ANIMATION="enemy.rifleman.attack"; //步枪兵攻击动画键
    private static final String DEATH_ANIMATION="enemy.death"; //敌人公共死亡动画键
    private static final int MOVE_INTERVAL=2; //移动动画每两帧切换一次
    private static final int ATTACK_INTERVAL=2; //攻击动画每两帧切换一次
    private static final int DEATH_INTERVAL=2; //死亡动画每两帧切换一次
    private static final double MOVE_SPEED=1.5; //步枪兵水平移动速度
    private static final int DETECT_RANGE=400; //步枪兵发现玩家的水平范围
    private static final int ATTACK_RANGE=300; //步枪兵开始攻击的水平范围
    private static final int ATTACK_FRAME=2; //步枪兵释放攻击的动画帧
    private static final int ATTACK_COOLDOWN_FRAMES=40; //两次攻击之间的冷却逻辑帧数

    //供 GameLoad 通过反射创建步枪兵模板
    public RiflemanEnemy() {
    }

    //使用已加载的移动首帧和配置数据创建步枪兵实体
    private RiflemanEnemy(int x, int y, ImageIcon icon, int hp, int attack,
                          int patrolMinX, int patrolMaxX) {
        super(x,y,icon,hp,attack,GameLoad.getInt("sprite.enemy.rifleman.scalePercent"));
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    //按 x,y,hp,attack,patrolMinX,patrolMaxX 格式创建步枪兵
    @Override
    public ElementObj createElement(String str) {
        int[] data=parseEnemyConfig(str,"步枪兵");
        ImageIcon icon=GameLoad.getImages(MOVE_ANIMATION).get(0);
        return new RiflemanEnemy(
                data[0],data[1],icon,data[2],data[3],data[4],data[5]);
    }

    //根据当前状态复用公共动画播放器切换图片
    @Override
    protected void updateImage(long gameTime) {
        switch (state) {
            case DEAD:
                playAnimation(DEATH_ANIMATION, gameTime,
                        DEATH_INTERVAL, false);
                break;
            case ATTACK:
                playAnimation(ATTACK_ANIMATION, gameTime,
                        ATTACK_INTERVAL, false);
                break;
            default:
                playAnimation(MOVE_ANIMATION, gameTime,
                        MOVE_INTERVAL, true);
                break;
        }
    }

    //获取步枪兵的唯一攻击触发帧
    @Override
    protected int getAttackFrame() {
        return ATTACK_FRAME;
    }

    //获取步枪兵攻击结束后的冷却逻辑帧数
    @Override
    protected int getAttackCooldownFrames() {
        return ATTACK_COOLDOWN_FRAMES;
    }

    //在唯一攻击帧创建步枪兵子弹和枪口特效
    @Override
    protected void releaseAttack() {
        java.awt.Rectangle bounds=getCurrentDrawBounds();
        int direction=facingRight ? 1 : -1;
        int muzzleX=bounds.x+(int)Math.round(bounds.width*
                (facingRight ? 0.96 : 0.04));
        int muzzleY=bounds.y+(int)Math.round(bounds.height*0.52);
        int bulletWidth=GameLoad.getImages("weapon.enemyBullet").get(0).getIconWidth();
        int bulletHeight=GameLoad.getImages("weapon.enemyBullet").get(0).getIconHeight();
        int muzzleWidth=GameLoad.getImages("effect.muzzle").get(0).getIconWidth();
        int muzzleHeight=GameLoad.getImages("effect.muzzle").get(0).getIconHeight();
        int bulletX=direction>0 ? muzzleX : muzzleX-bulletWidth;
        int effectX=direction>0 ? muzzleX : muzzleX-muzzleWidth;

        ElementManager manager=ElementManager.getManager();
        manager.addElement(new EnemyBullet(
                bulletX,muzzleY-bulletHeight/2,direction,getAttack()),
                GameElement.ENEMYFILE);
        manager.addElement(new MuzzleEffect(
                effectX,muzzleY-muzzleHeight/2,direction),GameElement.EFFECT);
    }
}
