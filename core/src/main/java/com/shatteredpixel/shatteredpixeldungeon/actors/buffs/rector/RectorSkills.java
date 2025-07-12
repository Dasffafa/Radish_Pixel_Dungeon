package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RectorSkills extends Item {

    public static class CORRECT extends RectorSkills {
        {
            image = ItemSpriteSheet.CORRECT;
        }

        @Override
        public String name(){
            String name = "";
            if(Dungeon.hero.subClass == HeroSubClass.BATTLEPREIST){
                name += Messages.get(this, "name_plus");
            }  else {
                name += Messages.get(this, "name");
            }
            return name;
        }

        @Override
        public String desc() {
            String desc = "";
            if(Dungeon.hero.subClass == HeroSubClass.BATTLEPREIST){
                desc += Messages.get(this, "desc_plus", Dungeon.depth+10);
            } else {
                desc += Messages.get(this, "desc",12 + Dungeon.depth);
            }
            return desc;
        }
    }

    public static class LIGHTIMUEE extends RectorSkills {
        {
            image = ItemSpriteSheet.LIGHTIMUEE;
        }


        @Override
        public String name(){
            String name = "";
            if(Dungeon.hero.subClass == HeroSubClass.BATTLEPREIST){
                name += Messages.get(this, "name_plus");
            }  else {
                name += Messages.get(this, "name");
            }
            return name;
        }

        @Override
        public String desc() {
            String desc = "";
            int level = Dungeon.depth/5*8;

            if(Dungeon.hero.subClass == HeroSubClass.BATTLEPREIST){
                desc += Messages.get(this, "desc_plus",Dungeon.depth/5 *12 ,  Dungeon.depth+10,Dungeon.depth/5 * 4);
            }  else {
                desc += Messages.get(this, "desc",level, Dungeon.depth+10);
            }

            return desc;
        }
    }

    public static class CLEAN extends RectorSkills {
        {
            image = ItemSpriteSheet.CLEAN;
        }
    }

    public static class PRAYERS extends RectorSkills {
        {
            image = ItemSpriteSheet.PRAYERS;
        }
    }

    public static class BLESS extends RectorSkills {
        {
            image = ItemSpriteSheet.BLESS;
        }
    }

}
