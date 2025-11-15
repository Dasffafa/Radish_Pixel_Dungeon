package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EndGuard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WandOfNewStar extends DamageWand {

    {
        image = ItemSpriteSheet.WAND_NEWSTAR;
        defaultAction = AC_ALIAS_ZAP;
    }

    private static final String AC_ALIAS_ZAP = "alias_zap";

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add(AC_ALIAS_ZAP);
        actions.remove(AC_ZAP);
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute( hero, action );

        if (action.equals( AC_ALIAS_ZAP )) {
            doAliasZap(hero);
        }
    }

    private void doAliasZap(Hero user){

        curUser = user;
        curItem = this;

        GameScene.selectCell( o_zapper );
    }

    private void GetZap(Ballistica bolt){
        int radius = 3 + (level() / 4);

        ArrayList<Integer> area = new ArrayList<>();

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                int distance = Math.abs(x) + Math.abs(y);
                if (distance <= radius) {
                    int offset = y * Dungeon.level.width() + x;
                    area.add(offset);
                }
            }
        }

        for (int offset : area) {
            int pos = bolt.collisionPos + offset;
            Char ch = Actor.findChar(pos);
            if (ch != null) {
                if (ch.alignment == Char.Alignment.ENEMY) {
                    ch.damage(buffedLvl() == 0 ? 1 : buffedLvl(), new DM100.LightningBolt());
                    ch.sprite.burst(0xFFFFFFFF, buffedLvl() / 2 + 2);
                } else if(ch.alignment == Char.Alignment.ALLY || ch instanceof Hero) {
                    Buff.affect(ch, Barrier.class).setShield(level()+2);
                }
            }
        }
    }

    @Override
    public void onZap(Ballistica bolt) {

        Char target = Actor.findChar(bolt.collisionPos);

        if (target != null) {
            if(target.alignment != Char.Alignment.ALLY && !(target instanceof Hero)) {
                GLog.w(Messages.get(this, "must_contain"));
                return;
            }
        }

        GetZap(bolt);

        Sample.INSTANCE.play(Assets.Sounds.ZAP);
    }

    protected CellSelector.Listener o_zapper = new CellSelector.Listener() {

        @Override
        public void onSelect(Integer target) {
            if (target != null) {
                final Wand curWand;
                if (curItem instanceof Wand) {
                    curWand = (Wand) Wand.curItem;
                } else {
                    return;
                }

                Char targetChar = Actor.findChar(target);
                if (targetChar == null || targetChar.alignment != curUser.alignment) {
                    GLog.w(Messages.get(WandOfNewStar.class, "must_contain"));
                    return;
                }

                final Ballistica shot = new Ballistica(curUser.pos, target, curWand.collisionProperties(target));
                int cell = shot.collisionPos;

                if (target == curUser.pos){
                    int radius = 3 + (level() / 4);

                    ArrayList<Integer> area = new ArrayList<>();

                    for (int y = -radius; y <= radius; y++) {
                        for (int x = -radius; x <= radius; x++) {
                            int distance = Math.abs(x) + Math.abs(y);
                            if (distance <= radius) {
                                int offset = y * Dungeon.level.width() + x;
                                area.add(offset);
                            }
                        }
                    }

                    for (int offset : area) {
                        int pos = shot.collisionPos + offset;
                        Char ch = Actor.findChar(pos);
                        if (ch != null) {
                            if (ch.alignment == Char.Alignment.ENEMY) {
                                ch.damage(buffedLvl() == 0 ? 1 : buffedLvl(), new DM100.LightningBolt());
                                ch.sprite.burst(0xFFFFFFFF, buffedLvl() / 2 + 2);
                            } else if(ch.alignment == Char.Alignment.ALLY || ch instanceof Hero) {
                                Buff.affect(ch, Barrier.class).setShield(level()+2);
                            }
                        }
                    }

                  if (curUser.hasTalent(Talent.SHIELD_BATTERY)) {
                      float shield = curUser.HT * (0.04f * curWand.curCharges);
                      if (curUser.pointsInTalent(Talent.SHIELD_BATTERY) == 2) shield *= 1.5f;

                      if (hero.belongings.weapon() instanceof EndGuard) {
                          EndGuard w2 = (EndGuard) hero.belongings.weapon;
                          if (w2 != null) {
                              Buff.affect(curUser, Barrier.class).setShield((int) (Math.round(shield) + (0.2f * (w2.level() + 1))));
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

    @Override
    public String statsDesc() {
        int radius = 3+ (level()/4)*2;
        if (levelKnown)
            return Messages.get(this, "stats_desc", radius ,radius, min(), max(),level()+2);
        else
            return Messages.get(this, "stats_desc", 3, 3, min(0), max(0),2);
    }


    @Override
    public void fx(Ballistica bolt, Callback callback) {
        MagicMissile.boltFromChar(curUser.sprite.parent,
                MagicMissile.RAINBOW_CONE,
                curUser.sprite,
                bolt.collisionPos,
                callback);
        Sample.INSTANCE.play(Assets.Sounds.ZAP);
    }


    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {

    }

    @Override
    public int min(int lvl) {
        return 2+lvl;
    }

    @Override
    public int max(int lvl) {
        return 5+lvl*4;
    }
}
