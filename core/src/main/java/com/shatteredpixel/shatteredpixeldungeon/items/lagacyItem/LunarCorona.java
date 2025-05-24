package com.shatteredpixel.shatteredpixeldungeon.items.lagacyItem;

import com.shatteredpixel.shatteredpixeldungeon.items.lagacyItem.utils.LegacyItemRing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class LunarCorona extends LegacyItemRing {
    {
        icon = ItemSpriteSheet.Icons.RING_WEALTH+3;
    }
    @Override
    protected RingBuff buff( ) {
        return new Phase();
    }

    public class Phase extends RingBuff {
        {
            type = buffType.NEUTRAL;
            announced = true;
        }

        private boolean Waxing = true;

        private static final String WAXING	= "waxing";
        private static final String PHASEINC	= "phaseinc";

        @Override
        public int icon() {
            return BuffIndicator.COMBO;
        }

    }
}
