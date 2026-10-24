package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GoblinRiderSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * 哥布林骑手：一区跆拳洞穴（苔藓分支第 2 层）的小 Boss。
 *
 * 远程：投掷长矛，伤害略高于石子（3~6）。
 * 近战：与坐骑青蛙一起攻击（哥布林 2~3 + 青蛙 1~5）。
 * 掉落：3 只捕鱼矛 + 金钥匙。
 */
public class GoblinRider extends Goblin {

    {
        spriteClass = GoblinRiderSprite.class;

        // 在普通哥布林（HP 20、命中 14）基础上上调
        HP = HT = 26;
        defenseSkill = 5;

        EXP = 8;
        maxLvl = 12;

        loot = null;
        lootChance = 0f;
    }

    private static final String SPEARREMAIN = "SpearRemain";
    private int spearRemain = 3;

    // 本次攻击是否为远程投矛（由 doAttack 设置，onAttackComplete 读取）
    private boolean rangedAttack = false;

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SPEARREMAIN, spearRemain);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        spearRemain = bundle.getInt(SPEARREMAIN);
    }

    @Override
    public int damageRoll() {
        // 哥布林 2~3 + 青蛙 1~5，一起打一次
        return Random.NormalIntRange(2, 3) + Char.combatRoll(1, 5);
    }

    @Override
    public int attackSkill(Char target) {
        return 16;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 4) + Char.combatRoll(0, 2);
    }

    @Override
    protected boolean canAttack(Char enemy) {
        if (Dungeon.level.adjacent(pos, enemy.pos)) {
            return true;
        }
        if (spearRemain <= 0) {
            return false;
        }
        return new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
    }

    /**
     * 近战走标准流程（sprite.attack），远程走投矛流程（sprite.zap）。
     * 两者都必须返回 false 让动画先播，并在动画结束时统一由
     * onAttackComplete / onZapComplete 结算并 spend()，否则回合不结束会卡死。
     */
    @Override
    protected boolean doAttack(Char enemy) {
        boolean visible = sprite != null && (sprite.visible || enemy.sprite.visible);

        if (Dungeon.level.adjacent(pos, enemy.pos)) {
            rangedAttack = false;
            if (visible) {
                sprite.attack(enemy.pos);
                return false;
            } else {
                attack(enemy);
                Invisibility.dispel(this);
                spend(attackDelay());
                return true;
            }
        }

        // 远程投矛
        rangedAttack = true;
        spearRemain = Math.max(spearRemain - 1, 0);
        if (visible) {
            sprite.zap(enemy.pos);
            return false;
        } else {
            throwSpear(enemy);
            Invisibility.dispel(this);
            spend(attackDelay());
            rangedAttack = false;
            return true;
        }
    }

    @Override
    public void onAttackComplete() {
        if (rangedAttack) {
            launchSpear();
            rangedAttack = false;
            // 不调用 super，避免再次造成近战伤害
        } else {
            super.onAttackComplete();
        }
    }

    @Override
    public void onZapComplete() {
        if (rangedAttack) {
            launchSpear();
            rangedAttack = false;
        } else {
            next();
        }
    }

    /** 投出飞行中的捕鱼矛，命中后结算伤害。 */
    private void launchSpear() {
        final Char target = enemy;
        if (target == null || !target.isAlive() || sprite == null || sprite.parent == null) {
            next();
            return;
        }

        int cell = new Ballistica(pos, target.pos, Ballistica.PROJECTILE).collisionPos;

        ((MissileSprite) sprite.parent.recycle(MissileSprite.class)).reset(
                sprite, cell, new FishingSpear(), new Callback() {
                    @Override
                    public void call() {
                        throwSpear(target);
                        Invisibility.dispel(GoblinRider.this);
                        spend(attackDelay());
                        next();
                    }
                });
    }

    /** 投矛命中结算：伤害 3~6（石子为 2~4）。 */
    private void throwSpear(Char target) {
        if (target == null || !target.isAlive()) return;

        if (hit(this, target, true)) {
            int dmg = Random.NormalIntRange(3, 6);
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
            dmg = defenseProc(target, dmg);
            dmg -= target.drRoll();
            target.damage(DamageInfo.of(dmg, DamageType.PHYSICAL, this, new ThrowingStone()));

            if (!target.isAlive() && target == Dungeon.hero) {
                Dungeon.fail(getClass());
                GLog.n(Messages.get(this, "kill"));
            }
        } else {
            target.sprite.showStatus(CharSprite.NEUTRAL, target.defenseVerb());
            // 未命中，矛掉落在目标脚下
            Dungeon.level.drop(new FishingSpear(), target.pos).sprite.drop();
        }
    }

    @Override
    public void die(Object cause) {
        // 掉落 3 只捕鱼矛
        for (int i = 0; i < 3; i++) {
            Dungeon.level.drop(new FishingSpear(), pos).sprite.drop();
        }
        // 金钥匙（用于开启本层赐福卷轴房间的金宝箱）
        Dungeon.level.drop(new GoldenKey(Dungeon.depth), pos).sprite.drop();
        super.die(cause);
    }

    @Override
    public String description() {
        return Messages.get(this, "desc");
    }
}
