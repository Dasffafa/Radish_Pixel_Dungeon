package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

public class WhiteKingGodSword extends MeleeWeapon {

    {
        tier = 3;
        image = ItemSpriteSheet.WHITE_KING_GOD_SWORD;
    }

    @Override
    public int min(int lvl) {
        return 3 + lvl * 2;
    }

    @Override
    public boolean doEquip(Hero hero) {
        if(hero.belongings.weapon instanceof WhiteKingGodSword){
            GLog.n(Messages.get(this, "already_wielding"));
            return false;
        }
        // 装备可能失败（手里是脱不下来的诅咒武器、村正狂热），确认装上了再挂 buff。
        // 没装上却挂着 buff 时，视野里的敌人会被白白标记成「已经被圣剑打过」
        if (super.doEquip(hero)) {
            Buff.affect(hero, OnlyOneEyeAttack.class);
            return true;
        }
        return false;
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single){
        // 同理，圣剑被诅咒卸不下来时 buff 不能摘，否则剑还在手上、效果却没了
        if (super.doUnequip(hero, collect, single)) {
            if(hero.buff(OnlyOneEyeAttack.class) != null)
                hero.buff(OnlyOneEyeAttack.class).detach();
            return true;
        }
        return false;
    }

    @Override
    public int max(int lvl) {
        return 16 + lvl * 3;
    }

    public static class OnlyOneEyeAttack extends Buff {
        {
            type = buffType.POSITIVE;
        }

        @Override
        public boolean act() {
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY && !mob.eyeAttack) {
                    if (Dungeon.hero.belongings.weapon instanceof WhiteKingGodSword) {
                    			WhiteKingGodSword weapon = (WhiteKingGodSword) Dungeon.hero.belongings.weapon;
                    			int vault_damage = (int) (weapon.damageRoll(Dungeon.hero) * 0.6f);

                    			int damage = Math.round(vault_damage * (weapon.augment == Augment.DAMAGE ? 1.5f : (weapon.augment == Augment.SPEED ? 0.7f : 1f)) + 0.1f * weapon.buffedLvl());

                        Buff.affect(mob, Paralysis.class, 100f);
                        ((MissileSprite)target.sprite.parent.recycle( MissileSprite.class )).resetFromAbove(mob,mob.pos, new WKNOR(), new Callback() {
                            @Override
                            public void call() {
                                int enchantedDamage = weapon.proc(Dungeon.hero, mob, damage);
                                mob.damage(DamageInfo.of(enchantedDamage, DamageType.PHYSICAL, Dungeon.hero, this)
										.setFloatingTextIcon(FloatingText.PHYS_DMG_NO_BLOCK));
                                Buff.detach( mob, Paralysis.class);
                            }
                        });
                    }
                    mob.eyeAttack = true;
                }
            }
            spend(TICK);
            return true;
        }

    }


    public String desc() {
    		String s;
    		if(Dungeon.hero != null){
    			int min = Math.round(((min() * 0.6f) * (augment == Augment.DAMAGE ? 1.5f : (augment == Augment.SPEED ? 0.7f : 1f))) + 0.1f * buffedLvl());
    			int max = Math.round(((max() * 0.6f) * (augment == Augment.DAMAGE ? 1.5f : (augment == Augment.SPEED ? 0.7f : 1f))) + 0.1f * buffedLvl());
    			s = Messages.get(this, "desc", min,max);
    		} else {
    			s = Messages.get(this, "desc", 0,0);
    		}
       
        return s;
    }

    public static class WKNOR extends WhiteKingGodSword{
        @Override
        public Emitter emitter() {
            Emitter e = new Emitter();
            e.pos(0, 10);
            e.fillTarget = false;
            e.pour(MagicMissile.MagicParticle.ATTRACTING, 0.05f);
            return e;
        }
    }

}
