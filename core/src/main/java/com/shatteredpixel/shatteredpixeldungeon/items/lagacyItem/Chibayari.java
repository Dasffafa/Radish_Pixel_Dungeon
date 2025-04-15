package com.shatteredpixel.shatteredpixeldungeon.items.lagacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Flail;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

// 传承·千叶枪
// DoggingDog on 20250415
public class Chibayari extends MeleeWeapon {
    {
        image = ItemSpriteSheet.THROWING_SPEAR;
        hitSound = Assets.Sounds.HIT_STAB;
        hitSoundPitch = 1.1f;

        tier = 1;
        RCH = 100000;    //lots of extra reach
    }

    int distance = 0;


    @Override
    public int max(int lvl) {
        return 20+lvl*4;
    }

    @Override
    public int min(int lvl) {
        return 2+lvl*3;
    }

    @Override
    public int damageRoll( Char owner ) {
        return (int) ((1-distance*0.15f)  * (float)Char.combatRoll( min(), max() ));
    }


    @Override
    public float accuracyFactor(Char owner, Char target) {
        float ACC = super.accuracyFactor(owner, target);

        this.distance = Dungeon.level.distance(owner.pos,target.pos);
        this.distance = Math.max(2,distance-2);
        ACC -= Math.max(0,ACC-((float) distance) * 0.15f);

        return ACC;
    }
}
