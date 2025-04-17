package com.shatteredpixel.shatteredpixeldungeon.items.lagacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

// 古武士刀
// DoggingDog on 20250417
public class AntiqueKatana extends MeleeWeapon {

    {
        image = ItemSpriteSheet.KATANA;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;
        tier = 1;
    }

    @Override
    public int min(int lvl) {
        return 1+lvl;
    }

    @Override
    public int max(int lvl) {
        return 8+lvl*2;
    }

    @Override
    public Weapon enchant(Enchantment ench ) {
        if (Dungeon.hero != null){
            this.detach(Dungeon.hero.belongings.backpack);
            Dungeon.hero.belongings.backpack.items.add((Item) new Masamune().identify());
        }
        return super.enchant(ench);
    }

    // 正宗
    public class Masamune extends MeleeWeapon{
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

    // 村正
    public class Muramasa extends MeleeWeapon{
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

}
