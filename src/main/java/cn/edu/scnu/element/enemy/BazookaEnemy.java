package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.effect.MuzzleEffect;
import cn.edu.scnu.element.weapon.Rocket;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

//使用火箭弹进行中远程攻击的火箭筒兵
public class BazookaEnemy extends AbstractEnemy {
    private static final String MOVE_ANIMATION="enemy.bazooka.move"; //火箭筒兵移动动画键
    private static final String ATTACK_ANIMATION="enemy.bazooka.attack"; //火箭筒兵攻击动画键
    private static final String DEATH_ANIMATION="enemy.death"; //敌人公共死亡动画键
    private static final int MOVE_INTERVAL=3; //移动动画换帧间隔
    private static final int ATTACK_INTERVAL=3; //攻击动画换帧间隔
    private static final int DEATH_INTERVAL=2; //死亡动画换帧间隔
    private static final double MOVE_SPEED=1.0; //水平移动速度
    private static final int DETECT_RANGE=900; //在视口内发现玩家的水平范围
    private static final int ATTACK_RANGE=900; //在视口内开始攻击的水平范围
    private static final int ATTACK_FRAME=2; //火箭释放帧
    private static final int ATTACK_COOLDOWN_FRAMES=60; //攻击冷却逻辑帧数

    //供 GameLoad 通过反射创建模板
    public BazookaEnemy() {
    }

    //使用移动首帧和配置数据创建火箭筒兵
    private BazookaEnemy(int x,int y,ImageIcon icon,int hp,int attack,
                         int patrolMinX,int patrolMaxX) {
        super(x,y,icon,hp,attack,GameLoad.getInt("sprite.enemy.bazooka.scalePercent"));
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    //按 x,y,hp,attack,patrolMinX,patrolMaxX 创建火箭筒兵
    @Override
    public ElementObj createElement(String str) {
        int[] data=parseEnemyConfig(str,"火箭筒兵");
        return new BazookaEnemy(data[0],data[1],
                GameLoad.getImages(MOVE_ANIMATION).get(0),data[2],
                data[3],data[4],data[5]);
    }

    //根据当前行为状态播放自然尺寸动画
    @Override
    protected void updateImage(long gameTime) {
        if(state==EnemyState.DEAD) playAnimation(DEATH_ANIMATION,gameTime,DEATH_INTERVAL,false);
        else if(state==EnemyState.ATTACK) playAnimation(ATTACK_ANIMATION,gameTime,ATTACK_INTERVAL,false);
        else playAnimation(MOVE_ANIMATION,gameTime,MOVE_INTERVAL,true);
    }

    //获取火箭释放帧
    @Override
    protected int getAttackFrame() { return ATTACK_FRAME; }

    //获取攻击冷却逻辑帧数
    @Override
    protected int getAttackCooldownFrames() { return ATTACK_COOLDOWN_FRAMES; }

    //玩家过近时为火箭飞行留出安全距离
    @Override
    protected int getRetreatRange() { return 230; }

    //创建一枚敌方 Rocket 和一次枪口特效
    @Override
    protected void releaseAttack() {
        java.awt.Rectangle bounds=getCurrentDrawBounds();
        int direction=facingRight ? 1 : -1;
        int muzzleX=facingRight ? bounds.x+(int)(bounds.width*0.98) : bounds.x+(int)(bounds.width*0.02);
        int muzzleY=bounds.y+(int)(bounds.height*0.28);
        ImageIcon rocketFrame=GameLoad.getImage("weapon.rocket");
        int rocketWidth=rocketFrame.getIconWidth();
        int rocketHeight=rocketFrame.getIconHeight();
        int effectWidth=GameLoad.getImages("effect.muzzle").get(0).getIconWidth();
        int effectHeight=GameLoad.getImages("effect.muzzle").get(0).getIconHeight();
        ElementManager manager=ElementManager.getManager();
        ElementObj player=findPlayer();
        Rocket rocket;
        if(player==null) {
            rocket=new Rocket(direction>0 ? muzzleX : muzzleX-rocketWidth,
                    muzzleY-rocketHeight/2,direction,getAttack());
        }else {
            java.awt.Rectangle target=player.getRectangle();
            rocket=Rocket.aimed(muzzleX,muzzleY,
                    (int)Math.round(target.getCenterX()),
                    (int)Math.round(target.getCenterY()),getAttack());
        }
        manager.addElement(rocket,GameElement.ENEMYFILE);
        manager.addElement(new MuzzleEffect(direction>0 ? muzzleX : muzzleX-effectWidth,
                muzzleY-effectHeight/2,direction),GameElement.EFFECT);
    }
}
