package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchool;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RoundedFrame;
import com.shatteredpixel.shatteredpixeldungeon.ui.SNDItems;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.UITheme;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** 骰子法师升级时：从2个候选学派二选一，或随机提升某学派，或跳过。 */
public class WndDiceMageTalentChoice extends Window {

    private static final int WIDTH = 170;
    private static final int PAD = 4;
    private static final int CARD_HEIGHT = 55;
    private static final int BUTTON_HEIGHT = 18;

    public WndDiceMageTalentChoice() {
        chrome.hardlight(DiceMageUI.DARK);

        DiceMageSchool first = weightedPick();
        DiceMageSchool second = weightedPick();
        if (second == first) {
            second = weightedPick();
        }

        SchoolChoice firstCard = null;
        SchoolChoice secondCard = null;
        float cardsBottom = PAD;
        if (first != null) {
            firstCard = new SchoolChoice(first);
            firstCard.setRect(PAD, PAD, WIDTH - PAD * 2, CARD_HEIGHT);
            add(firstCard);
            cardsBottom = firstCard.bottom();
        }
        if (second != null) {
            secondCard = new SchoolChoice(second);
            secondCard.setRect(PAD, cardsBottom + PAD, WIDTH - PAD * 2, CARD_HEIGHT);
            add(secondCard);
            cardsBottom = secondCard.bottom();
        }

        DiceMageUI.DiceButton random = new DiceMageUI.DiceButton("随机提升某学派") {
            @Override
            protected void onClick() {
                choose(randomAvailableSchool());
            }
        };
        random.textColor(DiceMageUI.CREAM);
        random.setRect(22, cardsBottom + PAD, WIDTH - 44, BUTTON_HEIGHT);
        add(random);

        DiceMageUI.DiceButton skip = new DiceMageUI.DiceButton("跳过") {
            @Override
            protected void onClick() {
                skip();
            }
        };
        skip.textColor(DiceMageUI.GREY_LINE);
        skip.setRect(22, random.bottom() + PAD, WIDTH - 44, BUTTON_HEIGHT);
        add(skip);

        resize(WIDTH, (int) (skip.bottom() + PAD));
    }

    public static boolean canShow() {
        Hero hero = Dungeon.hero;
        if (hero == null || hero.subClass != HeroSubClass.DICE_MAGE || hero.talentPointsAvailable(3) <= 0) {
            return false;
        }
        return hasAnyAvailableSchool();
    }

    private static boolean hasAnyAvailableSchool() {
        for (DiceMageSchool s : DiceMageSchool.values()) {
            if (DiceMageSchools.canInvest(s)) return true;
        }
        return false;
    }

    private static DiceMageSchool weightedPick() {
        ArrayList<DiceMageSchool> pool = new ArrayList<>();
        for (DiceMageSchool s : DiceMageSchool.values()) {
            if (DiceMageSchools.canInvest(s)) pool.add(s);
        }
        if (pool.isEmpty()) return null;

        float total = 0f;
        for (DiceMageSchool s : pool) total += s.weight;
        float roll = Random.Float(total);
        float acc = 0f;
        for (DiceMageSchool s : pool) {
            acc += s.weight;
            if (roll <= acc) return s;
        }
        return pool.get(pool.size() - 1);
    }

    private static DiceMageSchool randomAvailableSchool() {
        ArrayList<DiceMageSchool> pool = new ArrayList<>();
        for (DiceMageSchool s : DiceMageSchool.values()) {
            if (DiceMageSchools.canInvest(s)) pool.add(s);
        }
        if (pool.isEmpty()) return null;
        return pool.get(Random.Int(pool.size()));
    }

    private void choose(DiceMageSchool school) {
        if (school == null) return;
        Hero hero = Dungeon.hero;
        if (school == DiceMageSchool.SPECIAL) {
            DiceMageSchools.ensureSpecialRolled(hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint.class));
        }
        hero.upgradeTalent(school.talent);
        hide();
        if (canShow()) GameScene.show(new WndDiceMageTalentChoice());
    }

    private void skip() {
        Dungeon.hero.upgradeTalent(Talent.D3_SKIPPED);
        hide();
        if (canShow()) GameScene.show(new WndDiceMageTalentChoice());
    }

    @Override
    public void onBackPressed() {
        // 必须通过其中一个选项花费点数
    }

    private class SchoolChoice extends Button {

        private final DiceMageSchool school;
        private final DiceMageSpell spell;
        private RoundedFrame frame;
        private Image icon;
        private RenderedTextBlock name;
        private RenderedTextBlock desc;
        private ColorBlock divider;

        SchoolChoice(DiceMageSchool school) {
            this.school = school;
            if (school == DiceMageSchool.SPECIAL) {
                DiceMageSchools.ensureSpecialRolled(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint.class));
            }
            int nextLevel = Math.min(3, Dungeon.hero.pointsInTalent(school.talent) + 1);
            this.spell = DiceMageSchools.spellForLevel(school, nextLevel);
            Image snd = null;
            if (spell != null && spell.sndImageName() != null) {
                snd = SNDItems.get(spell.sndImageName());
            }
            icon = snd != null ? snd : new TalentIcon(school.talent);
            if (snd != null) {
                // 与 TalentIcon(16px×0.65) 显示尺寸一致
                float s = 0.65f * 16f / snd.width();
                icon.scale.set(s);
            } else {
                icon.scale.set(0.65f);
            }
            add(icon);
            name = PixelScene.renderTextBlock(school.talent.title(), 6);
            name.hardlight(DiceMageUI.SKY_BLUE);
            add(name);
            String d = spell != null ? spell.desc() : school.talent.desc();
            desc = PixelScene.renderTextBlock(d, 6);
            desc.hardlight(Window.WHITE);
            add(desc);
        }

        @Override
        protected void createChildren() {
            frame = UITheme.roundedFrame(DiceMageUI.PANEL, DiceMageUI.GREY_LINE);
            add(frame);
            divider = new ColorBlock(1, 1, 0xFF000000 | DiceMageUI.GREY_LINE);
            add(divider);
            super.createChildren();
        }

        @Override
        protected void layout() {
            super.layout();
            frame.setRect(x, y, width, height);
            float headerBottom = y + 16;
            divider.x = x + 2;
            divider.y = headerBottom;
            divider.size(width - 4, 1);
            icon.x = x + 4;
            icon.y = y + 2;
            name.setPos(x + 18, y + 5);
            desc.maxWidth((int) width - 8);
            desc.setPos(x + 4, headerBottom + 4);
        }

        @Override
        protected void onPointerDown() {
            frame.setLineColor(DiceMageUI.SKY_BLUE);
        }

        @Override
        protected void onPointerUp() {
            frame.setLineColor(DiceMageUI.GREY_LINE);
        }

        @Override
        protected void onClick() {
            choose(school);
        }
    }
}
