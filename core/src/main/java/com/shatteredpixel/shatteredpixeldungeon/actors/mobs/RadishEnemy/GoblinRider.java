package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GoblinRiderSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * 哥布林骑手：一区跆拳洞穴小 Boss。
 * 远程投掷长矛（伤害略高于石子），近战与青蛙一起攻击。
 * 掉落 3 只捕鱼矛 + 金钥匙。
 */
public class GoblinRider extends Goblin {

    {
        spriteClass = GoblinRiderSprite.class;

        // 在普通哥布林基础上上调
        HP = HT = 26;
        defenseSkill = 5;

        EXP = 8;
        maxLvl = 12;

        loot = null;
        lootChance = 0f;
    }

    private static final String SPEARREMAIN = "SpearRemain";
    private int spearRemain = 3;

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
        // 哥布林 2~3 + 青蛙 1~5
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
        return new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos
                && (spearRemain > 0 || Dungeon.level.adjacent(pos, enemy.pos));
    }

    @Override
    protected boolean doAttack(Char enemy) {
        if (Dungeon.level.adjacent(pos, enemy.pos)) {
            return super.doAttack(enemy);
        }
        spearRemain--;
        spearRemain = Math.max(spearRemain, 0);
        if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
            sprite.zap(enemy.pos);
            return false;
        } else {
            throwSpear();
            return true;
        }
    }

    /** 投掷长矛：伤害等于石子伤害再略高。 */
    private void throwSpear() {
        spend(1f);
        Invisibility.dispel(this);
        if (hit(this, enemy, true)) {
            int dmg = Random.NormalIntRange(3, 6);
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
            dmg = defenseProc(enemy, dmg);
            dmg -= enemy.drRoll();
            enemy.damage(DamageInfo.of(dmg, DamageType.PHYSICAL, this, new ThrowingStone()));

            if (!enemy.isAlive() && enemy == Dungeon.hero) {
                Dungeon.fail(getClass());
                GLog.n(Messages.get(this, "kill"));
            }
        } else {
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
        }
    }

    @Override
    public void onZapComplete() {
        throwSpear();
        next();
    }

    @Override
    public void die(Object cause) {
        // 掉落 3 只捕鱼矛
        for (int i = 0; i < 3; i++) {
            Dungeon.level.drop(new FishingSpear(), pos).sprite.drop();
        }
        // 金钥匙
        Dungeon.level.drop(new SkeletonKey(Dungeon.depth), pos).sprite.drop();
        super.die(cause);
    }

    @Override
    public String description() {
        return Messages.get(this, "desc");
    }
}
