package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DruidOakSage;
import com.watabou.utils.Bundle;
import rpdtest.H;
import rpdtest.Scenario;

public class Extra_DruidOakInteraction implements Scenario {
    @Override
    public String describe() {
        return "橡木智者可交互选择跟随目标，旧存档恢复后仍可交互";
    }

    @Override
    public void run(H h) {
        Arena.start(h);
        DruidOakSage sage = h.spawn(new DruidOakSage(), Arena.c(11, 10));
        h.checkFatal(h.get(sage::isAlive), "新召唤的橡木智者被判定为存活");
        h.tryTap(Arena.c(11, 10));
        h.tryTap(Arena.c(10, 10));
        h.check(h.get(() -> {
            Bundle bundle = new Bundle();
            sage.storeInBundle(bundle);
            return bundle.getInt("follow_id") == Dungeon.hero.id();
        }), "真实点击交互后选择英雄为跟随目标");
        h.saveAndReload();
        DruidOakSage restored = h.get(() -> (DruidOakSage) Dungeon.level.mobs.stream()
                .filter(m -> m instanceof DruidOakSage).findFirst().get());
        h.check(h.get(restored::isAlive), "读档后的橡木智者仍存活");
        h.world("恢复旧版零生命存档", () -> {
            Bundle bundle = new Bundle();
            restored.storeInBundle(bundle);
            bundle.put("HP", 0);
            bundle.put("HT", 0);
            restored.restoreFromBundle(bundle);
        });
        h.check(h.get(restored::isAlive), "旧版零生命存档恢复后仍存活");
    }
}
