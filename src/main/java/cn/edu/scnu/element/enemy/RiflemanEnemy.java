package cn.edu.scnu.element.enemy;

/**
 * 步枪兵敌人。
 *
 * 行为：
 * - 中距离发现玩家
 * - 停在射程内单发射击
 * - 低血量，攻击冷却适中
 * - 攻击动画 frame_02（索引 1）生成敌方子弹
 *
 * 素材：
 * - 移动：enemy.rifleman.move（4 帧，64×80，朝左）
 * - 攻击：enemy.rifleman.attack（4 帧，106×80，朝左）
 */

import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;

public class RiflemanEnemy extends AbstractEnemy {

    public RiflemanEnemy(int x, int y) {
        super(x, y, 40, 70,
                GameLoad.getImages("enemy.rifleman.move").get(0));
        this.health = 30;
        this.maxHealth = 30;
        this.moveSpeed = 1.5;
        this.detectRange = 400;  //发现玩家距离
        this.attackRange = 300;  //攻击玩家距离
    }

    @Override
    protected String getMoveAnimationKey() {
        return "enemy.rifleman.move";
    }

    @Override
    protected String getAttackAnimationKey() {
        return "enemy.rifleman.attack";
    }

    @Override
    protected int getAttackFrame() {
        return 1;
    } // frame_02，索引为 1

    @Override
    protected long getAttackCooldown() {
        return 2000;
    }  // 2秒冷却时间

    @Override
    protected void performAttack(long gameTime) {
        // 第一天占位：等 B 的 WeaponService 合并后实现
        // 实现方式：
        // if (weaponService != null) {
        //     WeaponRequest request = new WeaponRequest(
        //         WeaponType.ENEMY_BULLET,  // 武器类型
        //         Camp.ENEMY,               // 阵营
        //         getMuzzleX(),             // 枪口 X
        //         getMuzzleY(),             // 枪口 Y
        //         facingRight,              // 朝向
        //         10,                       // 伤害值
        //         facingRight ? 8.0 : -8.0, // 水平速度
        //         0                         // 垂直速度
        //     );
        //     weaponService.fire(request);
        // }
    }

    private int getMuzzleX() {
        int srcW = GameLoad.getImages(getAttackAnimationKey()).get(0).getIconWidth();
        int drawW = (int) Math.round(srcW * scale);
        int footX = getX() + getW() / 2;
        int drawX = footX - drawW / 2;
        double ratioX = facingRight ? (1.0 - 0.04) : 0.04;
        return drawX + (int) Math.round(drawW * ratioX);
    }

    private int getMuzzleY() {
        int srcH = GameLoad.getImages(getAttackAnimationKey()).get(0).getIconHeight();
        int drawH = (int) Math.round(srcH * scale);
        int footY = getY() + getH();
        int drawY = footY - drawH;
        return drawY + (int) Math.round(drawH * 0.52);
    }
}