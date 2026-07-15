package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.effect.MuzzleEffect;
import cn.edu.scnu.element.weapon.EnemyBullet;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

//每次攻击以三个关键帧连续发射敌方子弹的重机枪兵
public class HeavyGunnerEnemy extends AbstractEnemy {
    private static final String MOVE_ANIMATION="enemy.heavyGunner.move"; //重机枪兵移动动画键
    private static final String ATTACK_ANIMATION="enemy.heavyGunner.attack"; //重机枪兵攻击动画键
    private static final String DEATH_ANIMATION="enemy.death"; //敌人公共死亡动画键
    private static final int MOVE_INTERVAL=4; //移动动画换帧间隔
    private static final int ATTACK_INTERVAL=2; //攻击动画换帧间隔
    private static final int DEATH_INTERVAL=2; //死亡动画换帧间隔
    private static final double MOVE_SPEED=0.8; //水平移动速度
    private static final int DETECT_RANGE=900; //在视口内发现玩家的水平范围
    private static final int ATTACK_RANGE=900; //在视口内开始攻击的水平范围
    private static final int ATTACK_FRAME=1; //子弹释放帧
    private static final int ATTACK_COOLDOWN_FRAMES=80; //攻击冷却逻辑帧数

    //供 GameLoad 通过反射创建模板
    public HeavyGunnerEnemy() {
    }

    //使用移动首帧和配置数据创建重机枪兵
    private HeavyGunnerEnemy(int x,int y,ImageIcon icon,int hp,int attack,
                             int patrolMinX,int patrolMaxX) {
        super(x,y,icon,hp,attack,GameLoad.getInt("sprite.enemy.heavyGunner.scalePercent"));
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    //按统一六项配置创建重机枪兵
    @Override
    public ElementObj createElement(String str) {
        int[] data=parseEnemyConfig(str,"重机枪兵");
        return new HeavyGunnerEnemy(data[0],data[1],GameLoad.getImages(MOVE_ANIMATION).get(0),
                data[2],data[3],data[4],data[5]);
    }

    //根据当前行为状态播放自然尺寸动画
    @Override
    protected void updateImage(long gameTime) {
        if(state==EnemyState.DEAD) playAnimation(DEATH_ANIMATION,gameTime,DEATH_INTERVAL,false);
        else if(state==EnemyState.ATTACK) playAnimation(ATTACK_ANIMATION,gameTime,ATTACK_INTERVAL,false);
        else playAnimation(MOVE_ANIMATION,gameTime,MOVE_INTERVAL,true);
    }

    //获取子弹释放帧
    @Override
    protected int getAttackFrame() { return ATTACK_FRAME; }

    //获取攻击冷却逻辑帧数
    @Override
    protected int getAttackCooldownFrames() { return ATTACK_COOLDOWN_FRAMES; }

    //冷却期间玩家过近时为三连发拉开距离
    @Override
    protected int getRetreatRange() { return 170; }

    //每次关键帧创建一颗敌方子弹和一次枪口特效
    @Override
    protected void releaseAttack() {
        java.awt.Rectangle bounds=getCurrentDrawBounds();
        int direction=facingRight ? 1 : -1;
        int muzzleX=facingRight ? bounds.x+(int)(bounds.width*0.95) : bounds.x+(int)(bounds.width*0.05);
        int muzzleY=bounds.y+(int)(bounds.height*0.56);
        int bulletWidth=GameLoad.getImages("weapon.enemyBullet").get(0).getIconWidth();
        int bulletHeight=GameLoad.getImages("weapon.enemyBullet").get(0).getIconHeight();
        int effectWidth=GameLoad.getImages("effect.muzzle").get(0).getIconWidth();
        int effectHeight=GameLoad.getImages("effect.muzzle").get(0).getIconHeight();
        ElementManager manager=ElementManager.getManager();
        manager.addElement(new EnemyBullet(direction>0 ? muzzleX : muzzleX-bulletWidth,
                muzzleY-bulletHeight/2,direction,getAttack()),GameElement.ENEMYFILE);
        manager.addElement(new MuzzleEffect(direction>0 ? muzzleX : muzzleX-effectWidth,
                muzzleY-effectHeight/2,direction),GameElement.EFFECT);
    }

    //攻击动画第 1、2、3 帧各释放一发，形成三连发
    @Override
    protected boolean shouldReleaseAttackAtFrame(int frame) {
        return frame>=1 && frame<=3;
    }
}
