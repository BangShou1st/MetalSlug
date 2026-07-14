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
        super(x, y, icon, hp, attack);
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    //按 x,y,hp,attack,patrolMinX,patrolMaxX 格式创建步枪兵
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        if (data.length != 6) {
            throw new IllegalArgumentException(
                    "步枪兵配置格式应为 x,y,hp,attack,patrolMinX,patrolMaxX");
        }

        int x=Integer.parseInt(data[0].trim());
        int y=Integer.parseInt(data[1].trim());
        int hp=Integer.parseInt(data[2].trim());
        int attack=Integer.parseInt(data[3].trim());
        int patrolMinX=Integer.parseInt(data[4].trim());
        int patrolMaxX=Integer.parseInt(data[5].trim());

        if (patrolMinX > patrolMaxX) {
            throw new IllegalArgumentException(
                    "步枪兵 patrolMinX 不能大于 patrolMaxX");
        }
        if (x < patrolMinX || x > patrolMaxX) {
            throw new IllegalArgumentException("步枪兵 x 必须位于巡逻区间内");
        }

        ImageIcon icon=GameLoad.getImages(MOVE_ANIMATION).get(0);
        return new RiflemanEnemy(
                x, y, icon, hp, attack, patrolMinX, patrolMaxX);
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
        ImageIcon frame=getIcon();
        int drawWidth=frame.getIconWidth();
        int drawHeight=frame.getIconHeight();
        int drawX=getX()+getW()/2-drawWidth/2;
        int drawY=getY()+getH()-drawHeight;
        int direction=-1;
        int muzzleX=drawX+(int)Math.round(drawWidth*0.04);
        if(facingRight) {
            direction=1;
            muzzleX=drawX+(int)Math.round(drawWidth*0.96);
        }
        int muzzleY=drawY+(int)Math.round(drawHeight*0.52);
        int bulletX=muzzleX;
        int effectX=muzzleX;
        if(direction<0) {
            bulletX-=23;
            effectX-=32;
        }

        ElementManager manager=ElementManager.getManager();
        manager.addElement(new EnemyBullet(
                bulletX,muzzleY-3,direction,getAttack()),
                GameElement.ENEMYFILE);
        manager.addElement(new MuzzleEffect(
                effectX,muzzleY-17,direction),GameElement.EFFECT);
    }
}
