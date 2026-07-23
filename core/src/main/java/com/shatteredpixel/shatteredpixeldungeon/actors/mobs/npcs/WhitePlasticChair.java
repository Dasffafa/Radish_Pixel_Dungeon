package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WhitePlasticChairSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class WhitePlasticChair extends NPC {

    {
        spriteClass = WhitePlasticChairSprite.class;

        properties.add(Property.IMMOVABLE);
    }

    @Override
    public String name() {
        return Messages.get(this, "name");
    }

    @Override
    public String description() {
        return Messages.get(this, "desc");
    }

    @Override
    public int defenseSkill(Char enemy) {
        return INFINITE_EVASION;
    }

    @Override
    public void damage(int dmg, Object src) {
        // do nothing
    }

    @Override
    public boolean add(Buff buff) {
        return false;
    }

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public boolean interact(Char c) {
        sprite.turnTo(pos, c.pos);
        if (c == Dungeon.hero) {
            GLog.i(Messages.get(this, "desc"));
        }
        return true;
    }
}
