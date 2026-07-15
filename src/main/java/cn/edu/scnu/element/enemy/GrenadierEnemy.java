package cn.edu.scnu.element.enemy;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.weapon.Grenade;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

//使用敌方手雷进行范围攻击的投弹兵
public class GrenadierEnemy extends AbstractEnemy {
    private static final String MOVE_ANIMATION="enemy.grenadier.move"; //投弹兵移动动画键
    private static final String ATTACK_ANIMATION="enemy.grenadier.attack"; //投弹兵攻击动画键
    private static final String DEATH_ANIMATION="enemy.death"; //敌人公共死亡动画键
    private static final int MOVE_INTERVAL=3; //移动动画换帧间隔
    private static final int ATTACK_INTERVAL=3; //攻击动画换帧间隔
    private static final int DEATH_INTERVAL=2; //死亡动画换帧间隔
    private static final double MOVE_SPEED=1.2; //水平移动速度
    private static final int DETECT_RANGE=350; //发现玩家范围
    private static final int ATTACK_RANGE=280; //开始攻击范围
    private static final int ATTACK_FRAME=3; //手雷释放帧
    private static final int ATTACK_COOLDOWN_FRAMES=55; //攻击冷却逻辑帧数

    //供 GameLoad 通过反射创建模板
    public GrenadierEnemy() {
    }

    //使用移动首帧和配置数据创建投弹兵
    private GrenadierEnemy(int x,int y,ImageIcon icon,int hp,int attack,
                           int patrolMinX,int patrolMaxX) {
        super(x,y,icon,hp,attack,GameLoad.getInt("sprite.enemy.grenadier.scalePercent"));
        this.patrolMinX=patrolMinX;
        this.patrolMaxX=patrolMaxX;
        moveSpeed=MOVE_SPEED;
        detectRange=DETECT_RANGE;
        attackRange=ATTACK_RANGE;
    }

    //按统一六项配置创建投弹兵
    @Override
    public ElementObj createElement(String str) {
        int[] data=parseEnemyConfig(str,"投弹兵");
        return new GrenadierEnemy(data[0],data[1],GameLoad.getImages(MOVE_ANIMATION).get(0),
                data[2],data[3],data[4],data[5]);
    }

    //根据当前行为状态播放自然尺寸动画
    @Override
    protected void updateImage(long gameTime) {
        if(state==EnemyState.DEAD) playAnimation(DEATH_ANIMATION,gameTime,DEATH_INTERVAL,false);
        else if(state==EnemyState.ATTACK) playAnimation(ATTACK_ANIMATION,gameTime,ATTACK_INTERVAL,false);
        else playAnimation(MOVE_ANIMATION,gameTime,MOVE_INTERVAL,true);
    }

    //获取手雷释放帧
    @Override
    protected int getAttackFrame() { return ATTACK_FRAME; }

    //获取攻击冷却逻辑帧数
    @Override
    protected int getAttackCooldownFrames() { return ATTACK_COOLDOWN_FRAMES; }

    //在投掷关键帧创建一枚使用当前地图地面的敌方手雷
    @Override
    protected void releaseAttack() {
        java.awt.Rectangle bounds=getCurrentDrawBounds();
        int direction=facingRight ? 1 : -1;
        int releaseX=bounds.x+bounds.width/2;
        int releaseY=bounds.y+bounds.height/5;
        ElementManager.getManager().addElement(
                new Grenade(releaseX,releaseY,direction,getAttack()),GameElement.ENEMYFILE);
    }
}
