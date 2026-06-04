/*
 * 爆燃法术 - 天赋法术
 * 消耗6点魔力，对目标造成80/105/130点伤害
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class BlazeSpell extends DiceMageSpell {

    @Override
    public String nameKey() {
        return "spell_blaze";
    }

    @Override
    public int mpCost() {
        return 6;
    }

    @Override
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;

        int points = hero.pointsInTalent(Talent.LEARN_BLAZE);
        if (points <= 0) return false;

        return super.canCast();
    }

    @Override
    protected void onCast(Hero hero) {
        int points = hero.pointsInTalent(Talent.LEARN_BLAZE);
        int damage = points == 1 ? 80 : (points == 2 ? 105 : 130);

        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) {
                    MagicPoint mp = hero.buff(MagicPoint.class);
                    if (mp != null) mp.addPoints(mpCost());
                    return;
                }

                Char target = Actor.findChar(cell);
                if (target == null || target.alignment == Char.Alignment.ALLY) {
                    GLog.w("必须选择一个敌人！");
                    return;
                }

                // 造成伤害
                target.damage(damage, this);
                target.sprite.showStatus(CharSprite.NEGATIVE, Integer.toString(damage));

                // 视觉效果
                MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.FIRE, hero.sprite, target.pos, new Callback() {
                    @Override
                    public void call() {
                        Sample.INSTANCE.play(Assets.Sounds.BLAST);
                    }
                });

                if (target.isAlive()) {
                    GLog.p("爆燃！对目标造成了" + damage + "点伤害！");
                } else {
                    GLog.p("爆燃！对目标造成了" + damage + "点伤害并击杀了它！");
                }

                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return "选择一个敌人";
            }
        });
    }
}