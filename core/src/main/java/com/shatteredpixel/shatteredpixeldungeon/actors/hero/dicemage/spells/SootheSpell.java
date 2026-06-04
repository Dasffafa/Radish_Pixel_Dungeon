/*
 * 抚慰法术 - 天赋法术
 * 消耗4点魔力，治疗视野内所有盟友10/15/20HP并给予持续15/15/20回合的1/2/2点再生
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class SootheSpell extends DiceMageSpell {

    @Override
    public String nameKey() {
        return "spell_soothe";
    }

    @Override
    public int mpCost() {
        return 4;
    }

    @Override
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;

        // 检查是否有抚慰天赋
        int points = hero.pointsInTalent(Talent.LEARN_SOOTHE);
        if (points <= 0) return false;

        return super.canCast();
    }

    @Override
    protected void onCast(Hero hero) {
        int points = hero.pointsInTalent(Talent.LEARN_SOOTHE);

        int healAmount = points == 1 ? 10 : (points == 2 ? 15 : 20);
        int regenAmount = points <= 2 ? 1 : 2;
        int regenDuration = points <= 1 ? 15 : 20;

        int allyCount = 0;

        // 治疗所有盟友（包括英雄）
        // 治疗英雄自身
        if (hero.HP < hero.HT) {
            hero.HP = Math.min(hero.HP + healAmount, hero.HT);
            allyCount++;
        }

        // 治疗其他盟友（镜像、棱镜等）
        for (Char ch : Dungeon.level.mobs) {
            if (ch != hero && ch.alignment == Char.Alignment.ALLY && Dungeon.level.heroFOV[ch.pos]) {
                if (ch.HP < ch.HT) {
                    ch.HP = Math.min(ch.HP + healAmount, ch.HT);
                    allyCount++;
                }
                // 给予再生效果
                Buff.affect(ch, Healing.class).setHeal(regenAmount, 0, regenDuration);
            }
        }

        if (allyCount > 0) {
            GLog.p("抚慰！治疗了" + allyCount + "个盟友！（每回合+" + regenAmount + "HP，持续" + regenDuration + "回合）");
        } else {
            GLog.i("抚慰！但没有受伤的盟友。");
        }

        hero.spendAndNext(1f);
    }
}