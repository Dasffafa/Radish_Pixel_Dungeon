package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RectorSkills extends Item {

    public static class CORRECT extends RectorSkills {
        {
            image = ItemSpriteSheet.CORRECT;
        }

        @Override
        public String desc() {
            String desc = "";
            desc += Messages.get(this, "desc",12 + Dungeon.depth);
            return desc;
        }
    }

    public static class LIGHTIMUEE extends RectorSkills {
        {
            image = ItemSpriteSheet.LIGHTIMUEE;
        }
        @Override
        public String desc() {
            String desc = "";
            int level = ((Dungeon.depth/5)+1)*8;
            desc += Messages.get(this, "desc",level, Dungeon.depth+10);
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

}
