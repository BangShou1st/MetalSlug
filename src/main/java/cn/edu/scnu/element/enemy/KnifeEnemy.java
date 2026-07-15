package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.weapon.MeleeHitBox;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

//在近距离生成短寿命近战判定的刀兵
public class KnifeEnemy extends AbstractEnemy {
    private static final String MOVE_ANIMATION="enemy.knife.move"; //刀兵移动动画键
    private static final String ATTACK_ANIMATION="enemy.knife.attack"; //刀兵攻击动画键
    private static final String DEATH_ANIMATION="enemy.death"; //敌人公共死亡动画键
    private static final int MOVE_INTERVAL=2; //移动动画换帧间隔
    private static final int ATTACK_INTERVAL=2; //攻击动画换帧间隔
    private static final int DEATH_INTERVAL=2; //死亡动画换帧间隔
    private static final double MOVE_SPEED=2.5; //水平移动速度
    private static final int DETECT_RANGE=240; //发现玩家范围
    private static final int ATTACK_RANGE=55; //开始近战范围
    private static final int ATTACK_FRAME=2; //近战判定释放帧
    private static final int ATTACK_COOLDOWN_FRAMES=30; //攻击冷却逻辑帧数

    //供 GameLoad 通过反射创建模板
    public KnifeEnemy() {
    }

    //使用移动首帧和配置数据创建刀兵
    private KnifeEnemy(int x,int y,ImageIcon icon,int hp,int attack,
                       int patrolMinX,int patrolMaxX) {
        super(x,y,icon,hp,attack,GameLoad.getInt("sprite.enemy.knife.scalePercent"));
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    //按统一六项配置创建刀兵
    @Override
    public ElementObj createElement(String str) {
        int[] data=parseEnemyConfig(str,"刀兵");
        return new KnifeEnemy(data[0],data[1],GameLoad.getImages(MOVE_ANIMATION).get(0),
                data[2],data[3],data[4],data[5]);
    }

    //根据当前行为状态播放自然尺寸动画
    @Override
    protected void updateImage(long gameTime) {
        if(state==EnemyState.DEAD) playAnimation(DEATH_ANIMATION,gameTime,DEATH_INTERVAL,false);
        else if(state==EnemyState.ATTACK) playAnimation(ATTACK_ANIMATION,gameTime,ATTACK_INTERVAL,false);
        else playAnimation(MOVE_ANIMATION,gameTime,MOVE_INTERVAL,true);
    }

    //获取近战判定释放帧
    @Override
    protected int getAttackFrame() { return ATTACK_FRAME; }

    //获取攻击冷却逻辑帧数
    @Override
    protected int getAttackCooldownFrames() { return ATTACK_COOLDOWN_FRAMES; }

    //在朝向一侧创建一个短寿命近战判定
    @Override
    protected void releaseAttack() {
        int hitWidth=Math.max(18,(int)Math.round(getW()*0.65));
        int hitHeight=Math.max(18,(int)Math.round(getH()*0.42));
        int edgeInset=(int)Math.round(getW()*0.08);
        int hitX=facingRight ? getX()+getW()-edgeInset
                : getX()-hitWidth+edgeInset;
        int hitY=getY()+getH()-hitHeight;
        ElementManager.getManager().addElement(
                new MeleeHitBox(hitX,hitY,hitWidth,hitHeight,getAttack()),
                GameElement.ENEMYFILE);
    }
}
