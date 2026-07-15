package cn.edu.scnu.element.boss;

import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.effect.MuzzleEffect;
import cn.edu.scnu.element.weapon.BossShell;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.ImageIcon;

//第二关使用 BossShell 攻击的塔式坦克 Boss
public class TowerBoss extends AbstractBoss {
    private static final String IDLE_ANIMATION="boss2.idle"; //塔式 Boss 待机动画键
    private static final String ATTACK_ANIMATION="boss2.attack"; //塔式 Boss 攻击动画键
    private static final String WRECK_ANIMATION="boss2.wreck"; //塔式 Boss 残骸图片键
    private static final int IDLE_INTERVAL=3; //待机动画换帧间隔
    private static final int ATTACK_INTERVAL=3; //攻击动画换帧间隔
    private static final int ATTACK_FRAME=2; //炮弹释放帧
    private static final int ATTACK_COOLDOWN_FRAMES=70; //攻击冷却逻辑帧数
    private static final int ACTIVATION_RANGE=550; //玩家触发战斗的水平范围
    private static final int ATTACK_RANGE=470; //Boss 开始开炮的水平范围

    //供 GameLoad 通过反射创建模板
    public TowerBoss() {
    }

    //使用待机首帧和配置数据创建塔式 Boss
    private TowerBoss(int x,int y,ImageIcon icon,int hp,int attack) {
        super(x,y,icon,hp,attack,GameLoad.getInt("sprite.boss.tower.scalePercent"));
        activationRange=ACTIVATION_RANGE;
        attackRange=ATTACK_RANGE;
    }

    //按 x,y,hp,attack 格式创建塔式 Boss
    @Override
    public ElementObj createElement(String str) {
        String[] data=str.split(",");
        if(data.length!=4) throw new IllegalArgumentException("TowerBoss 配置格式应为 x,y,hp,attack");
        return new TowerBoss(Integer.parseInt(data[0].trim()),Integer.parseInt(data[1].trim()),
                GameLoad.getImages(IDLE_ANIMATION).get(0),Integer.parseInt(data[2].trim()),
                Integer.parseInt(data[3].trim()));
    }

    //获取待机动画键
    @Override protected String getIdleAnimation() { return IDLE_ANIMATION; }
    //获取攻击动画键
    @Override protected String getAttackAnimation() { return ATTACK_ANIMATION; }
    //获取残骸图片键
    @Override protected String getWreckAnimation() { return WRECK_ANIMATION; }
    //获取待机动画换帧间隔
    @Override protected int getIdleInterval() { return IDLE_INTERVAL; }
    //获取攻击动画换帧间隔
    @Override protected int getAttackInterval() { return ATTACK_INTERVAL; }
    //获取炮弹释放帧
    @Override protected int getAttackFrame() { return ATTACK_FRAME; }
    //获取攻击冷却逻辑帧数
    @Override protected int getAttackCooldownFrames() { return ATTACK_COOLDOWN_FRAMES; }

    //创建一枚 BossShell 和一次枪口特效
    @Override
    protected void releaseAttack() {
        java.awt.Rectangle bounds=getCurrentDrawBounds();
        int direction=facingRight ? 1 : -1;
        int muzzleX=facingRight ? bounds.x+bounds.width : bounds.x;
        int muzzleY=bounds.y+(int)(bounds.height*0.28);
        int effectWidth=GameLoad.getImages("effect.muzzle").get(0).getIconWidth();
        int effectHeight=GameLoad.getImages("effect.muzzle").get(0).getIconHeight();
        ElementManager manager=ElementManager.getManager();
        manager.addElement(new BossShell(muzzleX,muzzleY,findPlayer(),getAttack()),
                GameElement.ENEMYFILE);
        manager.addElement(new MuzzleEffect(direction>0 ? muzzleX : muzzleX-effectWidth,
                muzzleY-effectHeight/2,direction),GameElement.EFFECT);
    }
}
