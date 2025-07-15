package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.shatteredpixel.shatteredpixeldungeon.items.Item.curItem;
import static com.shatteredpixel.shatteredpixeldungeon.items.Item.curUser;
import static com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot;
import static com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth.genMidValueConsumable;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.VitaeBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.YetWand.WandOfCorret;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EndGuard;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Belief extends Buff implements ActionIndicator.Action {

    public float credibility;
    public boolean not_link = false;

    {
        type = buffType.POSITIVE;
        revivePersists = true;
    }

    public enum SkillList {
        CORRECT, LIGHTIMUEE, CLEAN, PRAYERS, BATTLE;
        public String desc(){
            switch (this){
                case CORRECT:
                case LIGHTIMUEE:
                case CLEAN:
                case PRAYERS:
                case BATTLE:
                    return Messages.get(this, name()+"desc");
                default:
                    return Messages.get(this, desc());
            }
        }
        public String title(){
            return Messages.get(this, name() + ".name");
        }
    }

    public void useSkills(Belief.SkillList skillList){
        switch (skillList){
            case CORRECT:
                curUser = hero;
                curItem = new WandOfCorret();
                GameScene.selectCell( zapper );

                break;
            case LIGHTIMUEE:
                if (hero.hasTalent(Talent.NOHOPE_LANG) && Dungeon.hero.HP < Dungeon.hero.HT/4){
                    int originStamina = 10 + Dungeon.depth/5 - 1;
                    int originVitae = Dungeon.depth/5*8;

                    int originVitaePlus = Dungeon.depth/5 * 12;
                    int adrenaline =  Dungeon.depth/5 * 4 - 1;

                    if(hero.subClass == HeroSubClass.BATTLEPREIST){
                        Buff.affect(hero, VitaeBuff.class).setVitae((int) (originVitaePlus * 1.5f));
                        Buff.affect(target, Adrenaline.class,adrenaline);
                    } else {
                        Buff.affect(hero, VitaeBuff.class).setVitae((int) (originVitae * 1.5f));
                    }

                    Buff.affect(hero, Stamina.class,originStamina  * 1.5f);
                } else {
                    if(hero.subClass == HeroSubClass.BATTLEPREIST){
                        Buff.affect(hero, VitaeBuff.class).setVitae(Dungeon.depth/5 * 12);
                        Buff.affect(target, Adrenaline.class,Dungeon.depth/5 * 4-1);
                        GLog.p("1e1");
                    } else {
                        Buff.affect(hero, VitaeBuff.class).setVitae(Dungeon.depth/5*8);
                    }
                    Buff.affect(hero, Stamina.class, Dungeon.depth+10-1);
                }
                GLog.p(Messages.get(Belief.class, "lightimuee_success"));
                break;
            case CLEAN:
                curUser = hero;
                if (hero.hasTalent(Talent.NOHOPE_LANG) && Dungeon.hero.HP < Dungeon.hero.HT/4){
                    ArrayList<Item> items = hero.belongings.getAllItems(Item.class);
                    for (Item w : items.toArray(new Item[0])){
                       w.cursed = false;
                    }
                    Buff.prolong( curUser, Light.class, 200f);
                    Buff.prolong( curUser, Bless.class, 40f);
                } else {
                    Scroll s = new ScrollOfRemoveCurse();
                    s.anonymize();
                    curItem = s;
                    s.doRead();
                    Buff.prolong( curUser, Light.class, 100f);
                    Buff.prolong( curUser, Bless.class, 20f);
                }

                if (hero.hasTalent(Talent.DEVOTIONAL)){
                    Buff.affect(hero, Barrier.class).setShield(4 * hero.pointsInTalent(Talent.DEVOTIONAL));
                    hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(4 * hero.pointsInTalent(Talent.DEVOTIONAL)), FloatingText.SHIELDING );
                }
                break;
            case PRAYERS:
                if (hero.hasTalent(Talent.NOHOPE_LANG) && Dungeon.hero.HP < Dungeon.hero.HT/4){
                    switch (Random.Int(4)){
                        //财富蓝色掉落
                        case 0:
                            Item i = genMidValueConsumable();
                            Dungeon.level.drop(i,target.pos);
                            new Flare( 6, 32 ).color(0x00AAFF, true).show( hero.sprite, 2f );
                            GLog.p(Messages.get(Belief.class, "prayers_success",i.name()));
                            break;
                        case 1:
                            hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(hero.maxExp()), FloatingText.EXPERIENCE);
                            hero.earnExp( hero.maxExp()/2, getClass() );
                            new Flare( 6, 32 ).color(0xFFFF00, true).show( hero.sprite, 2f );
                            GLog.p(Messages.get(Belief.class, "experience_success",hero.maxExp()/2));
                            break;
                        case 2:
                            ArrayList<Item> belongingsItems = hero.belongings.getAllItems(Item.class);
                            for (Item w : belongingsItems.toArray(new Item[0])){
                                ScrollOfRemoveCurse.uncurse( hero, w );
                                Sample.INSTANCE.play( Assets.Sounds.RAY );
                            }
                            new Flare( 6, 32 ).color(0x111111, true).show( hero.sprite, 2f );
                            GLog.p(Messages.get(Belief.class, "curse_remove_success"));
                            break;
                        case 3:
                            hero.HP += Math.min(hero.HT / 3, hero.HT);
                            if (hero.HP > hero.HT) {
                                hero.HP = hero.HT;
                            }
                            new Flare( 6, 32 ).color(0xFF1493, true).show( hero.sprite, 2f );
                            GLog.p(Messages.get(Belief.class, "heal_success"));
                            break;
                    }
                }
                switch (Random.Int(4)){
                    //财富蓝色掉落
                    case 0:
                        Item i = genMidValueConsumable();
                        Dungeon.level.drop(i,target.pos);
                        new Flare( 6, 32 ).color(0x00AAFF, true).show( hero.sprite, 2f );
                        GLog.p(Messages.get(Belief.class, "prayers_success",i.name()));
                    break;
                    case 1:
                        hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(hero.maxExp()), FloatingText.EXPERIENCE);
                        hero.earnExp( hero.maxExp()/2, getClass() );
                        new Flare( 6, 32 ).color(0xFFFF00, true).show( hero.sprite, 2f );
                        GLog.p(Messages.get(Belief.class, "experience_success",hero.maxExp()/2));
                    break;
                    case 2:
                        ArrayList<Item> belongingsItems = hero.belongings.getAllItems(Item.class);
                        for (Item w : belongingsItems.toArray(new Item[0])){
                            ScrollOfRemoveCurse.uncurse( hero, w );
                            Sample.INSTANCE.play( Assets.Sounds.RAY );
                        }
                        new Flare( 6, 32 ).color(0x111111, true).show( hero.sprite, 2f );
                        GLog.p(Messages.get(Belief.class, "curse_remove_success"));
                    break;
                    case 3:
                        hero.HP += Math.min(hero.HT / 3, hero.HT);
                        if (hero.HP > hero.HT) {
                            hero.HP = hero.HT;
                        }
                        new Flare( 6, 32 ).color(0xFF1493, true).show( hero.sprite, 2f );
                        GLog.p(Messages.get(Belief.class, "heal_success"));
                    break;
                }
                break;
            case BATTLE:
                if(hero.pointsInTalent(Talent.IRON_SUN)>=2){
                    Buff.affect(hero, Barrier.class).setShield( hero.lvl );
                }
                ChampionHero.getElite(hero,6, 60f);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean act() {
        FaithObstruction failed = Dungeon.hero.buff(FaithObstruction.class);
        if (credibility >= 5.0f){
            ActionIndicator.setAction(this);
        } else {
            ActionIndicator.clearAction();
        }
        not_link = failed != null;
        spend(TICK);
        return true;
    }

    /**
     * 增加信仰值
     * @param value 信仰值
     */
    public void getBelief(float value) {
        credibility += (float) (Math.floor(value * 100) / 100);
        hero.sprite.showStatusWithIcon(Window.TITLE_COLOR, String.valueOf(value), FloatingText.BELIEF);
    }

    /**
     * 减少信仰值
     * @param value 信仰值
     */
    public void DownBelief(float value) {
        credibility -= (float) (Math.floor(value * 100) / 100);
        hero.sprite.showStatus(Window.RADISH, "-" + value);
    }

    @Override
    public String iconTextDisplay() {
        return String.valueOf(Math.floor(credibility * 100) / 100);
    }

    public static String CREDIBILITY = "credibility";
    public static String NOT_LINK = "not_link";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(CREDIBILITY, credibility);
        bundle.put(NOT_LINK, not_link);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        credibility = bundle.getFloat(CREDIBILITY);
        not_link    = bundle.getBoolean(NOT_LINK);

        if (credibility>5){
            ActionIndicator.setAction(this);
        } else {
            ActionIndicator.clearAction();
        }
    }

    @Override
    public String desc(){
        if(hero.pointsInTalent(Talent.IRON_SUN)>=1){
            int buffCnt = 0;
            for(Object i: hero.buffs(Buff.class).toArray()) {
                if(((Buff) i).icon()!= BuffIndicator.NONE){
                    buffCnt+=3;
                }
            }
            return Messages.get(this, "desc2",Math.floor(credibility * 100) / 100, buffCnt);
        }
        return Messages.get(this, "desc",Math.floor(credibility * 100) / 100);
    }

    @Override
    public int icon() {
        return not_link ? BuffIndicator.NONE : BuffIndicator.BELIEF_LINK;
    }

    @Override
    public String actionName() {
        return Messages.get(this, "action_name");
    }

    @Override
    public int actionIcon() {
        return HeroIcon.BLESS;
    }

    @Override
    public int indicatorColor() {
        if(credibility>=20){
            return Window.TITLE_COLOR;
        } else if(credibility>=15){
            return Window.SHPX_COLOR;
        } else if(credibility>=12){
            return 0x99ccbb;
        } else {
            return 0x55AAFF;
        }
    }

    @Override
    public void doAction() {
        if(not_link){
            GLog.w(Messages.get(this, "not_link_error"));
        } else {
            GameScene.show(new WndBless(this));
        }

    }


    protected static CellSelector.Listener zapper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {

            if (target != null) {

                //FIXME this safety check shouldn't be necessary
                //it would be better to eliminate the curItem static variable.
                final Wand curWand;
                if (curItem instanceof Wand) {
                    curWand = (Wand) curItem;
                } else {
                    return;
                }

                final Ballistica shot = new Ballistica( curUser.pos, target, curWand.collisionProperties(target));
                int cell = shot.collisionPos;

                if (target == curUser.pos || cell == curUser.pos) {
                    if (target == curUser.pos && curUser.hasTalent(Talent.SHIELD_BATTERY)){
                        float shield = curUser.HT * (0.04f*curWand.curCharges);
                        if (curUser.pointsInTalent(Talent.SHIELD_BATTERY) == 2) shield *= 1.5f;

                        if(hero.belongings.weapon() instanceof EndGuard) {
                            EndGuard w2 = (EndGuard) hero.belongings.weapon;
                            if (w2 != null) {
                                Buff.affect(curUser, Barrier.class).setShield((int) (Math.round(shield) + (0.2f * ( w2.level() +1 ))));
                            }
                        } else {
                            Buff.affect(curUser, Barrier.class).setShield(Math.round(shield));
                        }

                        curWand.curCharges = 0;
                        curUser.sprite.operate(curUser.pos);
                        Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
                        ScrollOfRecharging.charge(curUser);
                        updateQuickslot();
                        curUser.spend(Actor.TICK);
                        return;
                    }
                    GLog.i( Messages.get(Wand.class, "self_target") );
                    return;
                }

                curUser.sprite.zap(cell);

                //attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
                if (Actor.findChar(target) != null)
                    QuickSlotButton.target(Actor.findChar(target));
                else
                    QuickSlotButton.target(Actor.findChar(cell));

                if (curWand.tryToZap(curUser, target)) {

                    curUser.busy();

                    if (curWand.cursed){
                        if (!curWand.cursedKnown){
                            GLog.n(Messages.get(Wand.class, "curse_discover", curWand.name()));
                        }
                        CursedWand.cursedZap(curWand,
                                curUser,
                                new Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT),
                                new Callback() {
                                    @Override
                                    public void call() {
                                        curWand.wandUsed();
                                    }
                                });
                    } else {
                        curWand.fx(shot, new Callback() {
                            public void call() {
                                curWand.onZap(shot);
                                if (Random.Float() < WondrousResin.extraCurseEffectChance()){
                                    WondrousResin.forcePositive = true;
                                    CursedWand.cursedZap(curWand,
                                            curUser,
                                            new Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT), new Callback() {
                                                @Override
                                                public void call() {
                                                    WondrousResin.forcePositive = false;
                                                    curWand.wandUsed();
                                                }
                                            });
                                } else {
                                    curWand.wandUsed();
                                }
                            }
                        });
                    }
                    curWand.cursedKnown = true;

                }

            }
        }

        @Override
        public String prompt() {
            return Messages.get(Wand.class, "prompt");
        }
    };
}
