package com.shatteredpixel.shatteredpixeldungeon.items.lagacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

// 正宗
public class Masamune extends MeleeWeapon {
    {
        image = ItemSpriteSheet.KATANA;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;
        tier = 1;
    }

    @Override
    public int min(int lvl) {
        return 8+lvl*2;
    }

    @Override
    public int max(int lvl) {
        return 20+lvl*4;
    }
}
