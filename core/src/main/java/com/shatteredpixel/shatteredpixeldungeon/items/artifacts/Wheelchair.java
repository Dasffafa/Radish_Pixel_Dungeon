package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WheelchairRush;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CatapultStartBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WheelchairCrashBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Speed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Wheelchair extends Artifact {

    public static final String AC_RIDE = "RIDE";
    public static final String AC_CRASH = "CRASH";

    {
        image = ItemSpriteSheet.ARTIFACT_WHEELCHAIR;

        levelCap = 10;

        charge = 3;
        chargeCap = 3;

        defaultAction = AC_RIDE;
        usesTargeting = true;
    }

    private int moveDistance = 0;

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (hero.buff(MagicImmune.class) != null) {
            return actions;
        }
        if (isEquipped(hero) && charge > 0 && !cursed) {
            actions.add(AC_RIDE);
        }
        // 轮椅翻车：月华英雄在加速状态下可使用
        if (isEquipped(hero) && !cursed && hero.heroClass == HeroClass.MOONLIGHT) {
            int points = hero.pointsInTalent(Talent.WHEELCHAIR_CRASH);
            if (points > 0 && hasAnySpeedBuff(hero)) {
                actions.add(AC_CRASH);
            }
        }
        return actions;
    }

    // 检查是否有任何加速状态
    private boolean hasAnySpeedBuff(Hero hero) {
        return hero.buff(Speed.class) != null
                || hero.buff(WheelchairRush.class) != null
                || hero.buff(Haste.class) != null
                || hero.buff(Stamina.class) != null;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (hero.buff(MagicImmune.class) != null) return;

        if (action.equals(AC_RIDE)) {
            curUser = hero;

            if (!isEquipped(hero)) {
                GLog.i(Messages.get(Artifact.class, "need_to_equip"));
                usesTargeting = false;
            } else if (charge < 1) {
                GLog.i(Messages.get(this, "no_charge"));
                usesTargeting = false;
            } else if (cursed) {
                GLog.w(Messages.get(this, "cursed"));
                usesTargeting = false;
            } else {
                usesTargeting = true;
                GameScene.selectCell(rideTargeter);
            }
        } else if (action.equals(AC_CRASH)) {
            curUser = hero;
            usesTargeting = false;
            performCrash(hero);
        }
    }

    // 执行轮椅翻车
    private void performCrash(Hero hero) {
        int points = hero.pointsInTalent(Talent.WHEELCHAIR_CRASH);
        if (points <= 0) return;

        // 计算最大加速状态回合数
        int maxTurns = 0;

        Speed speed = hero.buff(Speed.class);
        if (speed != null) maxTurns = Math.max(maxTurns, (int)speed.cooldown());

        WheelchairRush rush = hero.buff(WheelchairRush.class);
        if (rush != null) maxTurns = Math.max(maxTurns, (int)rush.cooldown());

        Haste haste = hero.buff(Haste.class);
        if (haste != null) maxTurns = Math.max(maxTurns, (int)haste.cooldown());

        Stamina stamina = hero.buff(Stamina.class);
        if (stamina != null) maxTurns = Math.max(maxTurns, (int)stamina.cooldown());

        NaturesPower.naturesPowerTracker natPower = hero.buff(NaturesPower.naturesPowerTracker.class);
        if (natPower != null) maxTurns = Math.max(maxTurns, (int)natPower.cooldown());

        // 结束所有加速效果
        Buff.detach(hero, Speed.class);
        Buff.detach(hero, WheelchairRush.class);
        Buff.detach(hero, Haste.class);
        Buff.detach(hero, Stamina.class);
        Buff.detach(hero, NaturesPower.naturesPowerTracker.class);

        // 计算伤害：基础伤害 + 加速回合数
        int baseDamage = 15 + (points - 1) * 8; // +1:15, +2:23, +3:31 (用户说30，用15+8*(points-1)得到15/23/31，改成30就+2时23)
        if (points == 3) baseDamage = 30; // 调整+3为30
        int damage = baseDamage + maxTurns;

        // 对5x5范围内敌人造成伤害
        for (Mob mob : Dungeon.level.mobs) {
            if (Dungeon.level.distance(hero.pos, mob.pos) <= 2) {
                // 2格距离是5x5范围
                mob.damage(damage, this);
                CellEmitter.get(mob.pos).burst(SparkParticle.FACTORY, 6);
            }
        }

        // 施加轮椅翻车效果（5回合缠绕）
        Buff.affect(hero, WheelchairCrashBuff.class, WheelchairCrashBuff.DURATION);

        // 视觉和音效
        CellEmitter.get(hero.pos).burst(SparkParticle.FACTORY, 12);
        Sample.INSTANCE.play(Assets.Sounds.BONES);

        GLog.p(Messages.get(Wheelchair.class, "crash_success", damage, maxTurns));

        hero.spendAndNext(1f);
        Talent.onArtifactUsed(hero);
        updateQuickslot();
    }

    // 跳跃范围：2 + 0.2*等级（向下取整）
    // 弹射起步+2：跳跃距离+1
    public int jumpRange() {
        int range = 2 + (int)(level() * 0.2f);
        // 检查弹射起步天赋+2
        if (curUser != null) {
            if (curUser.heroClass == HeroClass.MOONLIGHT && curUser.pointsInTalent(Talent.CATAPULT_START) >= 2) {
                range += 1;
            }
        }
        return range;
    }

    // 加速持续时间：10 + 1*等级
    public float speedDuration() {
        return 10f + level();
    }

    // 充能上限：3 + 等级，最大10
    public int maxCharge() {
        return Math.min(10, 3 + level());
    }

    private CellSelector.Listener rideTargeter = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {
            if (target != null) {
                // 检查范围
                int range = jumpRange();
                if (Dungeon.level.distance(curUser.pos, target) > range) {
                    GLog.w(Messages.get(Wheelchair.class, "out_of_range", range));
                    return;
                }

                // 检查目标位置是否可行
                if (Dungeon.level.solid[target] || Actor.findChar(target) != null) {
                    GLog.w(Messages.get(Wheelchair.class, "invalid_target"));
                    return;
                }

                // 执行跳跃
                performJump(curUser, target);
            }
        }

        @Override
        public String prompt() {
            int range = jumpRange();
            return Messages.get(Wheelchair.class, "prompt", range);
        }
    };

    private void performJump(Hero hero, int targetPos) {
        // 检查弹射起步效果Buff
        CatapultStartBuff catapultBuff = hero.buff(CatapultStartBuff.class);
        if (catapultBuff != null) {
            // 有弹射起步效果，不消耗充能
            Buff.detach(hero, CatapultStartBuff.class);
            GLog.p("弹射起步！本次使用不消耗充能！");
        } else {
            // 消耗充能
            charge--;
        }

        Invisibility.dispel(hero);
        Talent.onArtifactUsed(hero);
        updateQuickslot();

        final int dest = targetPos;
        final float duration = speedDuration();

        hero.busy();
        Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

        hero.sprite.jump(hero.pos, dest, 0, 0.1f, new Callback() {
            @Override
            public void call() {
                hero.pos = dest;
                Dungeon.level.occupyCell(hero);
                Dungeon.observe();
                GameScene.updateFog();

                // 视觉效果2
                CellEmitter.get(hero.pos).burst(SparkParticle.FACTORY, 6);

                // 获得轮椅狂飙效果
                Buff.affect(hero, WheelchairRush.class, duration);

                hero.spendAndNext(1f);
            }
        });
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new wheelchairCharge();
    }

    @Override
    public void charge(Hero target, float amount) {
        if (cursed || target.buff(MagicImmune.class) != null) return;
        chargeCap = maxCharge();
        if (charge < chargeCap) {
            partialCharge += 0.2f * amount;
            while (partialCharge >= 1f) {
                partialCharge--;
                charge++;
                if (charge >= chargeCap) {
                    charge = chargeCap;
                    break;
                }
            }
            updateQuickslot();
        }
    }

    // 记录移动距离，用于升级
    public void onHeroMove(Hero hero, int distance) {
        if (cursed) return;
        moveDistance += distance;

        // 升级所需移动距离：1000 + 100*等级
        int upgradeThreshold = 1000 + 100 * level();
        if (moveDistance >= upgradeThreshold && level() < levelCap) {
            moveDistance -= upgradeThreshold;
            upgrade();
            chargeCap = maxCharge();
            GLog.p(Messages.get(this, "levelup"));
            updateQuickslot();
        }
    }

    @Override
    public String desc() {
        String desc = Messages.get(this, "desc");

        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n";
            if (cursed) {
                desc += Messages.get(this, "desc_cursed");
            } else {
                desc += Messages.get(this, "desc_equipped",
                        charge, chargeCap,
                        jumpRange(),
                        (int)speedDuration());
            }
        }

        return desc;
    }

    private static final String MOVE_DISTANCE = "move_distance";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(MOVE_DISTANCE, moveDistance);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        moveDistance = bundle.getInt(MOVE_DISTANCE);
        chargeCap = maxCharge();
    }

    public class wheelchairCharge extends ArtifactBuff {

        @Override
        public boolean act() {
            chargeCap = maxCharge();

            if (charge < chargeCap && !cursed && target.buff(MagicImmune.class) == null) {
                // 充能速度：每 (80 - level - charge) 回合恢复1充能
                // 加上神器充能效果：每回合恢复0.2充能
                float chargeGain = 0.2f; // 基础神器充能
                chargeGain += 1f / Math.max(1, 80 - level() - charge); // 额外充能
                chargeGain *= RingOfEnergy.artifactChargeMultiplier(target);

                partialCharge += chargeGain;

                while (partialCharge >= 1f) {
                    partialCharge--;
                    charge++;
                    if (charge >= chargeCap) {
                        charge = chargeCap;
                        break;
                    }
                }
                updateQuickslot();
            }

            spend(TICK);
            return true;
        }

        // 当英雄移动时触发
        public void onMove(int distance) {
            Wheelchair.this.onHeroMove((Hero) target, distance);
        }
    }
}