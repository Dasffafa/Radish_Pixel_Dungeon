package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

public class WndBless extends Window {
    private static final int WIDTH		= 100;
    private static final int BTN_SIZE	= 32;
    private static final int BTN_GAP	= 6;
    private static final int GAP		= 6;

    public static Item S1;
    public static Item S2;
    public static Item S3;
    public static Item S4;

    public WndBless(Belief belief) {

        S1 = new RectorSkills.CORRECT();
        S2 = new RectorSkills.LIGHTIMUEE();
        S3 = new RectorSkills.CLEAN();
        S4 = hero.subClass == HeroSubClass.BATTLEPREIST ? new RectorSkills.BLESS() : new RectorSkills.PRAYERS();

        IconTitle titlebar = new IconTitle();
        titlebar.setRect(0, -3, WIDTH, 0);
        titlebar.icon(new BuffIcon(BuffIndicator.BLESS, true));
        titlebar.label(Messages.get(belief,"action_name"));
        add( titlebar );
        RenderedTextBlock message = PixelScene.renderTextBlock( (Messages.get(this,"select_skills")), 6 );
        message.maxWidth(WIDTH);
        message.setPos(0, titlebar.bottom() + GAP);
        add( message );

        RewardButton shop1 = new RewardButton( S1 );
        shop1.setRect( (WIDTH) / 2f - BTN_SIZE, message.top() + message.height() + BTN_GAP, BTN_SIZE,
                BTN_SIZE );
        add( shop1 );

        RewardButton shop2 = new RewardButton( S2 );
        shop2.setRect( shop1.right(), shop1.top(), BTN_SIZE, BTN_SIZE );
        add(shop2);

        RewardButton shop3 = new RewardButton( S3 );
        shop3.setRect( shop1.left(), shop1.bottom(), BTN_SIZE, BTN_SIZE );
        add(shop3);

        RewardButton shop4 = new RewardButton( S4 );
        shop4.setRect( shop3.right(), shop2.bottom(), BTN_SIZE, BTN_SIZE );
        add(shop4);

        resize(WIDTH, (int) shop3.bottom());
    }

    private class RewardWindow extends WndInfoItem {

        public RewardWindow( Item item ) {
            super(item);
            Belief creaditSkills = hero.buff(Belief.class);
            StyledButton btnConfirm = new StyledButton(Chrome.Type.RED_BUTTON,Messages.get(this, "ac_ask")){
                @Override
                protected void onClick() {
                    float cooldown;
                    switch (hero.pointsInTalent(Talent.ACT_GODPROGRESS)){
                        default:
                        case 1:
                            cooldown = 500f;
                            break;
                        case 2:
                            cooldown = 425f;
                            break;
                        case 3:
                            cooldown = 350f;
                            break;
                    }
                    //惩戒
                    if(item == S1) {
                        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1  && creaditSkills != null && hero.buff(Talent.NoBeliefUsedCooldown.class) == null) {
                            creaditSkills.useSkills(Belief.SkillList.valueOf("CORRECT"));
                            WndBless.this.hide();
                        } else if(creaditSkills != null && creaditSkills.credibility>=5){
                            creaditSkills.useSkills(Belief.SkillList.valueOf("CORRECT"));
                            WndBless.this.hide();
                        } else if(hero.buff(Talent.NoBeliefUsedCooldown.class) != null){
                            GLog.n(Messages.get(WndBless.class,"not_talent"));
                        } else {
                            GLog.n(Messages.get(WndBless.class,"not_enough_credibility"));
                        }
                    //净化
                    } else if(item == S2){
                        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1 && creaditSkills != null && hero.buff(Talent.NoBeliefUsedCooldown.class) == null) {
                            Buff.affect(hero, Talent.NoBeliefUsedCooldown.class, cooldown);
                            creaditSkills.useSkills(Belief.SkillList.valueOf("LIGHTIMUEE"));
                            WndBless.this.hide();
                        } else if(creaditSkills != null && creaditSkills.credibility>=12){
                            creaditSkills.useSkills(Belief.SkillList.valueOf("LIGHTIMUEE"));
                            WndBless.this.hide();
                            creaditSkills.DownBelief(12);
                        } else if(hero.buff(Talent.NoBeliefUsedCooldown.class) != null){
                            GLog.n(Messages.get(WndBless.class,"not_talent"));
                        } else {
                            GLog.n(Messages.get(WndBless.class,"not_enough_credibility"));
                        }
                    } else if(item == S3){
                        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1 && creaditSkills != null && hero.buff(Talent.NoBeliefUsedCooldown.class) == null) {
                            Buff.affect(hero, Talent.NoBeliefUsedCooldown.class,  cooldown);
                            creaditSkills.useSkills(Belief.SkillList.valueOf("CLEAN"));
                            WndBless.this.hide();
                        } else if (creaditSkills != null && creaditSkills.credibility>=15){
                            creaditSkills.useSkills(Belief.SkillList.valueOf("CLEAN"));
                            WndBless.this.hide();
                            creaditSkills.DownBelief(15);
                        } else if(hero.buff(Talent.NoBeliefUsedCooldown.class) != null){
                            GLog.n(Messages.get(WndBless.class,"not_talent"));
                        } else {
                            GLog.n(Messages.get(WndBless.class,"not_enough_credibility"));
                        }
                    //祷告
                    } else if(item == S4){
                        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1 && creaditSkills != null && hero.buff(Talent.NoBeliefUsedCooldown.class) == null) {
                            Buff.affect(hero, Talent.NoBeliefUsedCooldown.class,  cooldown);
                            creaditSkills.useSkills(hero.subClass == HeroSubClass.BATTLEPREIST ? Belief.SkillList.valueOf("BATTLE") : Belief.SkillList.valueOf("PRAYERS"));
                            WndBless.this.hide();
                        } else if(creaditSkills != null && creaditSkills.credibility>=20) {
                            creaditSkills.useSkills(hero.subClass == HeroSubClass.BATTLEPREIST ? Belief.SkillList.valueOf("BATTLE") : Belief.SkillList.valueOf("PRAYERS"));
                            WndBless.this.hide();
                            creaditSkills.DownBelief(hero.subClass == HeroSubClass.BATTLEPREIST ? 15 : 20);
                        } else if(hero.buff(Talent.NoBeliefUsedCooldown.class) != null){
                            GLog.n(Messages.get(WndBless.class,"not_talent"));
                        } else {
                            GLog.n(Messages.get(WndBless.class,"not_enough_credibility"));
                        }
                    }
                    hide();
                    RewardWindow.this.hide();
                }
            };
            btnConfirm.setRect(0, height+2, width, 16);
            add(btnConfirm);

            resize(width, (int)btnConfirm.bottom());
        }
    }

    public class RewardButton extends Component {

        protected NinePatch bg;
        protected ItemSlot slot;

        public RewardButton( Item item ){
            bg = Chrome.get( Chrome.Type.WINDOW);
            add( bg );

            slot = new ItemSlot( item ){
                @Override
                protected void onPointerDown() {
                    bg.brightness( 1.2f );
                    Sample.INSTANCE.play( Assets.Sounds.CLICK );
                }
                @Override
                protected void onPointerUp() {
                    bg.resetColor();
                }
                @Override
                protected void onClick() {
                    ShatteredPixelDungeon.scene().addToFront(new RewardWindow(item));
                }
            };
            add(slot);
        }

        @Override
        protected void layout() {
            super.layout();

            bg.x = x;
            bg.y = y;
            bg.size( width, height );

            slot.setRect( x + 2, y + 2, width - 4, height - 4 );

        }
    }
}
