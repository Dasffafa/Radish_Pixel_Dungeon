package com.shatteredpixel.shatteredpixeldungeon.items.lagacyItem;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.CrabArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.lagacyItem.utils.LegacyItemArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

// 玄冥
// DoggingDog on 20250517
public class Turtleir extends LegacyItemArmor {
    {
        image = ItemSpriteSheet.STONE_SHOCK+5;
    }

    public Turtleir() {
        super(1);
    }

    @Override
    public int DRMax(int lvl){
        return 10 + Math.max(2 * lvl + augment.defenseFactor(5 * lvl), 5 * lvl);
    }

    @Override
    public int DRMin(int lvl){
        return 6 + 2 * lvl;
    }

    @Override
    protected ArmorBuff buff( ) {
        return new Turtleir.Mass_Energy();
    }

    @Override
    public int proc(Char attacker, Char defender, int damage ) {
        if(damage != 0 && buff instanceof Mass_Energy){
            ((Mass_Energy) buff).add(Mass_Energy.DURATION);
        }else if (damage == 0 && buff instanceof Mass_Energy){
            ((Mass_Energy) buff).clr();
        }
        return super.proc(attacker,defender,damage);
    }


    public class Mass_Energy extends ArmorBuff {
        {
            type = buffType.POSITIVE;
            announced = true;
        }
        private static final String ENERGY_LEVEL = "Energy_Lvl";

        private int lvl = 0;
        public static final int DURATION = 1;

        @Override
        public int icon() {
            return BuffIndicator.ARMOR;
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", this.lvl);
        }

        @Override
        public String iconTextDisplay() {
            return lvl +"/8";
        }

        public float slowRate(){
            return 1f - lvl * 0.1f;
        }


        public int absorbDamage(int dmg){
            return (int) (dmg * (1f - lvl * 0.15f));
        }

        public void add(int Lv){
            if(this.lvl > 0)
                this.lvl += Lv;
            else{
                this.lvl = Lv;
            }
        }

        public void clr(){
            this.lvl = 0;
        }

    }

}
