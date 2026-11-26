package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Druid;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.EventManager;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import rpdtest.H;
import rpdtest.Scenario;

public class Extra_DruidArrowHit implements Scenario {
    private static int hits;
    private static DamageType type;

    public static void onHit(AttackHitEvent event) {
        if (event.getAttacker() == Dungeon.hero) {
            hits++;
            type = event.getDamage().getType();
        }
    }

    @Override
    public String describe() {
        return "雷云不会提前终止灵箭命中结算，附魔箭仍造成法术伤害";
    }

    @Override
    public void run(H h) {
        Arena.start(h, HeroClasses.HUNTRESS);
        SpiritBow bow = h.give(new SpiritBow());
        Rat rat = Arena.dummy(h, Arena.c(12, 10));
        h.world("准备雷云和必定命中的灵箭", () -> {
            rat.HP = 1;
            rat.defenseSkill = 0;
            EventManager.registerSubscriber(AttackHitEvent.class, Extra_DruidArrowHit.class, "onHit", 1000);
            Buff.affect(Dungeon.hero, Druid.EnchantArrow.class, 20f);
            Druid.ThunderField field = Buff.affect(Dungeon.hero, Druid.ThunderField.class);
            field.cell = rat.pos;
            field.turns = 10;
            field.depth = Dungeon.depth;
            field.branch = Dungeon.branchId;
            rpdtest.Reflect.set(field, "time", 100f);
        });
        h.use(bow, SpiritBow.AC_SHOOT);
        h.selectCell(Arena.c(12, 10));
        h.waitReady();
        h.check(hits == 1, "灵箭击杀目标后仍发布一次命中事件，实际=" + hits);
        h.check(type == DamageType.MAGICAL, "附魔箭以法术伤害结算，实际=" + type);
    }
}
