package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bee;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class LongStick extends MeleeWeapon {

    //TODO 待添加
    {
        image = ItemSpriteSheet.LONG_STARK;
        tier = 3;
        ACC = 1;
        DLY = 1;
    }

    @Override
    public int min(int lvl) {
        return 4+lvl;
    }

    @Override
    public int max(int lvl) {
        return 20 + lvl * 4;
    }

    @Override
    public float delayFactor( Char owner ) {
        float Boost = 0;
        if(Dungeon.hero!=null){
            Boost += Dungeon.hero.defenseSkill(new Rat());
            Boost -= Dungeon.hero.lvl;
            Boost = Math.max(Boost,0);
            Boost *= 0.01f;
            Boost = Math.min(1f,Boost);
        }
        float dF = baseDelay(owner) * (1f/speedMultiplier(owner)) - Boost;
        return Math.max(dF,0.1f);
    }


    @Override
    public float accuracyFactor(Char owner, Char target) {
        float Boost = 0;
        if(Dungeon.hero!=null){
            Boost += Dungeon.hero.defenseSkill(new Rat());
            Boost -= Dungeon.hero.lvl;
            Boost = Math.max(Boost,0);
            Boost *= 0.01f;
            Boost = Math.min(1f,Boost);
        }

        return super.accuracyFactor(owner,target) + Boost;
    }
}


