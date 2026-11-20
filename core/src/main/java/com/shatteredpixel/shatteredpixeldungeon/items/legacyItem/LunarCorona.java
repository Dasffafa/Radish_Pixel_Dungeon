package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.IDamageModifier;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.utils.LegacyItemRing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class LunarCorona extends LegacyItemRing {
    {
        icon = ItemSpriteSheet.Icons.RING_WEALTH;
    }

    private boolean Waxing = true;
    private int PhaseInc = 0;

    private static final String WAXING = "waxing";
    private static final String PHASEINC = "phaseinc";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(WAXING, Waxing);
        bundle.put(PHASEINC, PhaseInc);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(WAXING)) {
            Waxing = bundle.getBoolean(WAXING);
        }
        PhaseInc = bundle.getInt(PHASEINC);
    }

    @Override
    protected RingBuff buff( ) {
        return new Phase();
    }

    public class Phase extends RingBuff implements IDamageModifier {
        {
            type = buffType.NEUTRAL;
            announced = true;
        }

        public boolean isWaxing(){
            return Waxing;
        }
        @Override
        public boolean act() {
            PhaseInc++;
            if(PhaseInc >= 50){
                PhaseInc = 0;
                Waxing = !Waxing;
            }
            return super.act();
        }

        @Override
        public float rawMultiply(int dmg, DamageInfo info) {
            return Waxing ? 2f : 0.5f;
        }

        @Override
        public String icon() {
            return BuffIndicator.COMBO;
        }

        public class Wax{};
        public class Wan{};

        @Override
        public String desc() {
            return Messages.get(this, "desc", Waxing?Messages.get(new Wax(),"name"):Messages.get(new Wan(),"name"));
        }

    }
}
