package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WndInfoImpSuperTalent extends WndTitledMessage {

    public WndInfoImpSuperTalent(HeroClass cls, HeroSubClass subCls){
        super( new HeroIcon(subCls), Messages.titleCase(subCls.title()), subCls.super_desc());
        if (cls == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses.JUMBLE) {
            if (Dungeon.hero != null && Dungeon.hero.heroClass == cls && Dungeon.hero.subClass == subCls
                    && Dungeon.hero.talents.get(3) != null && !Dungeon.hero.talents.get(3).isEmpty()) {
                TalentsPane.TalentTierPane pane = new TalentsPane.TalentTierPane(
                        new LinkedHashMap<>(Dungeon.hero.talents.get(3)), 4, TalentButton.Mode.INFO);
                pane.setRect(0, height + 5, width, pane.height());
                add(pane);
                resize(width, (int) pane.bottom());
            } else {
                RenderedTextBlock explanation = PixelScene.renderTextBlock(Messages.get(this, "jumble_rule"), 6);
                explanation.maxWidth(width);
                explanation.setPos(0, height + 5);
                add(explanation);
                resize(width, (int) explanation.bottom());
            }
            return;
        }

        ArrayList<LinkedHashMap<Talent, Integer>> talentList = new ArrayList<>();
        Talent.initClassTalents(cls, talentList);
        Talent.initSubclassTalents(subCls, talentList);
        Talent.initT4Talents(cls,subCls,talentList);

        TalentsPane.TalentTierPane talentPane = new TalentsPane.TalentTierPane(talentList.get(3), 4, TalentButton.Mode.INFO);
        talentPane.title.text( Messages.titleCase(Messages.get(WndHeroInfo.class, "talents")));
        talentPane.setRect(0, height + 5, width, talentPane.height());
        add(talentPane);
        resize(width, (int) talentPane.bottom());

    }

}
