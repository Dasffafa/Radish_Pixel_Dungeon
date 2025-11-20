package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RiverCrystal extends Trinket {

    {
        image = ItemSpriteSheet.RIVER_GLASS;
    }


    @Override
    protected int upgradeEnergyCost() {
        return 6+2*level();
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc", level()+1);
        } else {
            return Messages.get(this, "typical_stats_desc", 1);
        }

    }
}
