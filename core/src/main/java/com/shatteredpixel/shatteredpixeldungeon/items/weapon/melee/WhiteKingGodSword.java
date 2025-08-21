package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class WhiteKingGodSword extends MeleeWeapon {

    {
        tier = 3;
        image = ItemSpriteSheet.DARTS+19;
    }

    @Override
    public int min(int lvl) {
        return 3 + lvl * 2;
    }

    @Override
    public boolean doEquip(Hero hero) {
        Buff.affect(hero, OnlyOneEyeAttack.class);
        return super.doEquip(hero);
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single){
        if(hero.buff(OnlyOneEyeAttack.class) != null)
            hero.buff(OnlyOneEyeAttack.class).detach();
        return super.doUnequip(hero, collect, single);
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
        public int icon() {
            return BuffIndicator.NONE;
        }

        @Override
        public boolean act() {
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
                if(Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY){
                    if (!mob.eyeAttack) {
                        WhiteKingGodSword weapon = Dungeon.hero.belongings.weapon instanceof WhiteKingGodSword ?
                                (WhiteKingGodSword)Dungeon.hero.belongings.weapon : null;
                        if (weapon != null) {
                            int damage = Math.round(weapon.damageRoll(Dungeon.hero) * (0.6f + 0.1f * weapon.level()));
                            ((MissileSprite)target.sprite.parent.recycle( MissileSprite.class )).
                                    reset( target.pos, mob.pos, new WhiteKingGodSword(), () -> mob.damage(damage, this));
                        }
                        mob.eyeAttack = true;
                    }

                }
            }
            spend(TICK);
            return true;
        }
    }


    public String desc() {
        String s = super.desc();
        int min = Math.round(min() * (0.6f + 0.1f * level()));
        int max = Math.round(max() * (0.6f + 0.1f * level()));
        s = Messages.get(this, "desc", min,max);
        return s;
    }

}
