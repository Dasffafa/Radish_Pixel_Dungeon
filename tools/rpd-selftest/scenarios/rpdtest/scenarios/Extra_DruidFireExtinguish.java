package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Druid;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import rpdtest.H;
import rpdtest.Scenario;

public class Extra_DruidFireExtinguish implements Scenario {
    @Override
    public String describe() {
        return "药水和冰霜法杖可以熄灭火风暴的地面火";
    }

    @Override
    public void run(H h) {
        Arena.start(h);
        int cell = Arena.c(12, 10);
        PotionOfHealing potion = h.give(new PotionOfHealing());
        h.world("放置火风暴火焰", () -> GameScene.add(Blob.seed(cell, 100, Druid.FirestormFire.class)));
        h.use(potion, PotionOfHealing.AC_THROW);
        h.chooseOption(0);
        h.selectCell(cell);
        h.waitReady();
        h.check(h.get(() -> Blob.volumeAt(cell, Druid.FirestormFire.class) == 0), "药水泼洒熄灭火风暴火焰");
        WandOfFrost wand = h.give(new WandOfFrost());
        Arena.dummy(h, cell);
        h.world("再次放置火风暴火焰", () -> GameScene.add(Blob.seed(cell, 100, Druid.FirestormFire.class)));
        h.zap(wand, cell);
        h.check(h.get(() -> Blob.volumeAt(cell, Druid.FirestormFire.class) == 0), "冰霜法杖熄灭火风暴火焰");
    }
}
