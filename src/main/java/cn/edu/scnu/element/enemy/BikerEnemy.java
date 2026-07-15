package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.effect.MuzzleEffect;
import cn.edu.scnu.element.weapon.Rocket;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

//高速接近玩家并停车发射火箭的摩托兵
public class BikerEnemy extends AbstractEnemy {
    private static final String MOVE_ANIMATION="enemy.biker.move"; //摩托兵移动动画键
    private static final String ATTACK_ANIMATION="enemy.biker.attack"; //摩托兵攻击动画键
    private static final String DEATH_ANIMATION="enemy.death"; //敌人公共死亡动画键
    private static final int MOVE_INTERVAL=1; //移动动画换帧间隔
    private static final int ATTACK_INTERVAL=2; //攻击动画换帧间隔
    private static final int DEATH_INTERVAL=2; //死亡动画换帧间隔
    private static final double MOVE_SPEED=4.0; //水平移动速度
    private static final int DETECT_RANGE=900; //在视口内发现玩家的水平范围
    private static final int ATTACK_RANGE=900; //在视口内开始发射火箭的水平范围
    private static final int ATTACK_FRAME=2; //火箭释放帧
    private static final int ATTACK_COOLDOWN_FRAMES=45; //攻击冷却逻辑帧数

    //供 GameLoad 通过反射创建模板
    public BikerEnemy() {
    }

    //使用移动首帧和配置数据创建摩托兵
    private BikerEnemy(int x,int y,ImageIcon icon,int hp,int attack,
                       int patrolMinX,int patrolMaxX) {
        super(x,y,icon,hp,attack,GameLoad.getInt("sprite.enemy.biker.scalePercent"));
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    //按统一六项配置创建摩托兵
    @Override
    public ElementObj createElement(String str) {
        int[] data=parseEnemyConfig(str,"摩托兵");
        return new BikerEnemy(data[0],data[1],GameLoad.getImages(MOVE_ANIMATION).get(0),
                data[2],data[3],data[4],data[5]);
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

    //玩家过近时利用高速重新拉开火箭射击距离
    @Override
    protected int getRetreatRange() { return 220; }

    //摩托兵交战时保持高速接近和撤离特点
    @Override
    protected double getEngagedSpeedMultiplier() { return 1.2; }

    //在攻击关键帧从摩托兵武器位置发射火箭
    @Override
    protected void releaseAttack() {
        java.awt.Rectangle bounds=getCurrentDrawBounds();
        int direction=facingRight ? 1 : -1;
        int muzzleX=bounds.x+(int)Math.round(bounds.width*
                (facingRight ? 0.78 : 0.22));
        int muzzleY=bounds.y+(int)Math.round(bounds.height*0.30);
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
