package com.shatteredpixel.shatteredpixeldungeon.items.lagacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

// 决斗对剑
public class DualDuelDaggers extends MeleeWeapon {
    {
        image = ItemSpriteSheet.SAI;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;

        tier = 1;

        DLY = 0.8f;
    }

    private int lastDamage = 0;

    @Override
    public int min(int lvl) {
        return 8+lvl;
//        return 1;
    }

    @Override
    public int max(int lvl) {
        return 20+lvl*5;
//        return 1;
    }


    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if(damage == lastDamage && Dungeon.hero != null){
            ArtifactRecharge.chargeArtifacts(Dungeon.hero, 1000);
            Buff.affect(attacker, Recharging.class, 40f);
            PotionOfHealing.heal(attacker);
        }
        lastDamage = damage;
        return damage;
    }
}
