/*
 * 瘴气法术 - 天赋法术
 * 消耗3点魔力，对目标地格3*3/5*5/7*7范围内的单位造成3点伤害并给予伤害值*4回合的中毒
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;

public class MiasmaSpell extends DiceMageSpell {

    @Override
    public String nameKey() {
        return "spell_miasma";
    }

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;

        int points = hero.pointsInTalent(Talent.LEARN_MIASMA);
        if (points <= 0) return false;

        return super.canCast();
    }

    @Override
    protected void onCast(Hero hero) {
        int points = hero.pointsInTalent(Talent.LEARN_MIASMA);
        int range = points == 1 ? 1 : (points == 2 ? 2 : 3); // 3x3/5x5/7x7 => radius 1/2/3
        int baseDamage = 3;

        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) {
                    MagicPoint mp = hero.buff(MagicPoint.class);
                    if (mp != null) mp.addPoints(mpCost());
                    return;
                }

                int hitCount = 0;
                // 对范围内所有单位造成伤害和中毒
                for (int i : PathFinder.NEIGHBOURS9) {
                    int pos = cell + i;
                    if (pos < 0 || pos >= Dungeon.level.length()) continue;

                    Char ch = Actor.findChar(pos);
                    if (ch != null) {
                        ch.damage(baseDamage, this);
                        // 中毒：伤害值*4回合
                        Buff.affect(ch, Poison.class).set(baseDamage * 4f);
                        hitCount++;
                    }
                }

                if (hitCount > 0) {
                    GLog.p("瘴气！对" + hitCount + "个单位造成了" + baseDamage + "点伤害并施加中毒！");
                } else {
                    GLog.i("瘴气！但范围内没有目标。");
                }

                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(MiasmaSpell.class, "prompt");
            }
        });
    }
}