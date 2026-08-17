package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 收集（法力学派 L1，主动）：把7格范围内地上所有物品拉到自己身边。
 */
public class GatherSpell extends DiceMageSpell {

    private static final int RANGE = 7;

    @Override
    public Talent school() {
        return Talent.SCHOOL_MANA;
    }

    @Override
    public int level() {
        return 1;
    }

    @Override
    public int mpCost() {
        return 1;
    }

    @Override
    public String sndImageName() {
        return "gather";
    }

    @Override
    protected void onCast(Hero hero) {
        if (!spendMagic(hero)) return;

        int grabbed = 0;
        // valueList() 返回快照，收集过程中堆被 pickUp()->destroy() 移除也不会影响遍历
        for (Heap h : Dungeon.level.heaps.valueList()) {
            if (h == null || h.isEmpty() || h.type != Heap.Type.HEAP) continue;
            if (Dungeon.level.distance(hero.pos, h.pos) > RANGE) continue;
            while (!h.isEmpty()) {
                Item item = h.peek();
                if (item.doPickUp(hero, h.pos)) {
                    h.pickUp();
                    grabbed++;
                } else {
                    break;
                }
            }
        }

        if (grabbed > 0) {
            GLog.i(Messages.get(GatherSpell.this, "grab_success", grabbed));
            CellEmitter.center(hero.pos).burst(Speck.factory(Speck.STAR), 8);
        } else {
            GLog.w(Messages.get(GatherSpell.this, "no_item"));
        }
        hero.spendAndNext(1f);
    }
}
