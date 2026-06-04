/*
 * 烈酒法术 - 天赋法术
 * 消耗3点魔力，清除一名友方单位除饥饿外的所有负面状态并给予25/35/45点护盾
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

public class LiquorSpell extends DiceMageSpell {

    @Override
    public String nameKey() {
        return "spell_liquor";
    }

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;

        int points = hero.pointsInTalent(Talent.LEARN_LIQUOR);
        if (points <= 0) return false;

        return super.canCast();
    }

    @Override
    protected void onCast(Hero hero) {
        int points = hero.pointsInTalent(Talent.LEARN_LIQUOR);
        int shieldAmount = points == 1 ? 25 : (points == 2 ? 35 : 45);

        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) {
                    // 取消施法，退还魔力
                    MagicPoint mp = hero.buff(MagicPoint.class);
                    if (mp != null) mp.addPoints(mpCost());
                    return;
                }

                Char target = Actor.findChar(cell);
                if (target == null || target.alignment != Char.Alignment.ALLY) {
                    GLog.w("必须选择一个友方单位！");
                    return;
                }

                // 清除负面状态（保留饥饿）
                target.buffs().stream()
                    .filter(b -> b.type == Buff.buffType.NEGATIVE && !(b instanceof Hunger))
                    .forEach(b -> b.detach());

                // 给予护盾
                Barrier barrier = Buff.affect(target, Barrier.class);
                barrier.incShield(shieldAmount);

                target.sprite.showStatus(CharSprite.POSITIVE, "+" + shieldAmount + " " + Messages.get(Barrier.class, "shield"));

                GLog.p("烈酒！清除了负面状态并给予" + shieldAmount + "点护盾！");

                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return "选择一个友方单位";
            }
        });
    }
}