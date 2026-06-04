package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * 砥砺锋芒天赋
 * 以一件已鉴定(+1)或任意(+2)武器/护甲为代价鉴定同类型物品
 */
public class SharpeningEdgeTalent {

    public static final String AC_SHARPENING_EDGE = "SHARPENING_EDGE";

    /**
     * 检查是否可以使用砥砺锋芒
     * @param hero 英雄
     * @param sacrificeItem 作为代价的物品
     * @return 是否可以使用
     */
    public static boolean canUse(Hero hero, Item sacrificeItem) {
        // 只对月华英雄生效
        if (hero.heroClass != HeroClass.MOONLIGHT) return false;

        // 检查天赋点数
        int points = hero.pointsInTalent(Talent.SHARPENING_EDGE);
        if (points <= 0) return false;

        // +1时需要已鉴定的物品，+2时任意物品都可以
        if (points == 1 && !sacrificeItem.isIdentified()) return false;

        return true;
    }

    /**
     * 获取可以作为目标的物品列表
     * @param hero 英雄
     * @param sacrificeType 代价物品的类型（KindOfWeapon 或 Armor）
     * @return 可鉴定的目标物品列表
     */
    public static List<Item> getTargetItems(Hero hero, Class<?> sacrificeType) {
        List<Item> targets = new ArrayList<>();

        for (Item item : hero.belongings) {
            // 同类型且未鉴定
            if (sacrificeType.isInstance(item) && !item.isIdentified()) {
                targets.add(item);
            }
        }

        return targets;
    }

    /**
     * 执行砥砺锋芒
     * @param hero 英雄
     * @param sacrificeItem 代价物品
     * @param targetItem 目标物品
     */
    public static void execute(Hero hero, Item sacrificeItem, Item targetItem) {
        // 消耗代价物品（先卸下再丢弃）
        if (sacrificeItem instanceof EquipableItem && sacrificeItem.isEquipped(hero)) {
            ((EquipableItem)sacrificeItem).doUnequip(hero, false, true);
        }
        sacrificeItem.detach(hero.belongings.backpack);

        // 鉴定目标物品
        if (!ShardOfOblivion.passiveIDDisabled()) {
            targetItem.identify();
        }

        GLog.p(Messages.get(SharpeningEdgeTalent.class, "success", targetItem.name()));
        hero.spendAndNext(1f);
        hero.sprite.operate(hero.pos);
    }

    /**
     * 显示选择目标物品的窗口
     * @param hero 英雄
     * @param sacrificeItem 代价物品
     */
    public static void showTargetSelectionWindow(Hero hero, Item sacrificeItem) {
        Class<?> sacrificeType = sacrificeItem instanceof KindOfWeapon ? KindOfWeapon.class : Armor.class;
        List<Item> targets = getTargetItems(hero, sacrificeType);

        if (targets.isEmpty()) {
            GLog.w(Messages.get(SharpeningEdgeTalent.class, "no_target"));
            return;
        }

        // 构建选项名称
        String[] options = new String[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            options[i] = targets.get(i).name();
        }

        GameScene.show(new WndOptions(
                new ItemSprite(sacrificeItem),
                Messages.titleCase(Messages.get(SharpeningEdgeTalent.class, "title")),
                Messages.get(SharpeningEdgeTalent.class, "prompt"),
                options
        ) {
            @Override
            protected void onSelect(int index) {
                if (index >= 0 && index < targets.size()) {
                    SharpeningEdgeTalent.execute(hero, sacrificeItem, targets.get(index));
                }
            }
        });
    }
}