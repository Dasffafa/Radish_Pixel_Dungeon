package com.shatteredpixel.shatteredpixeldungeon.items.lagacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
// 村正
public class Muramasa extends MeleeWeapon {
    {
        image = ItemSpriteSheet.KATANA;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;
        tier = 1;
    }

    @Override
    public int min(int lvl) {
        return 1;
    }

    @Override
    public int max(int lvl) {
        return 30+lvl*8;
    }
}
