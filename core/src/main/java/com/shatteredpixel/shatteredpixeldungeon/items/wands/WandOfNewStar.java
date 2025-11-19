package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WandOfNewStar extends DamageWand {

    {
        image = ItemSpriteSheet.WAND_NEWSTAR;
        defaultAction = AC_ALIAS_ZAP;
    }

    public static final String AC_ALIAS_ZAP = "alias_zap";

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

    private void GetZap(Char ch,int small){
        final Wand curWand;
        if (curItem instanceof Wand) {
            curWand = (Wand) Wand.curItem;
        } else {
            return;
        }
        Ballistica bolt;
        int radius = (1 + (level() / 4))-small;

        int x = ch.pos % Dungeon.level.width();
        int y = ch.pos / Dungeon.level.width();

        if (Math.max(x, Dungeon.level.width()-x) >= Math.max(y, Dungeon.level.height()-y)){
            if (x > Dungeon.level.width()/2){
                bolt = new Ballistica(ch.pos, ch.pos - 1, Ballistica.WONT_STOP);
            } else {
                bolt = new Ballistica(ch.pos, ch.pos + 1, Ballistica.WONT_STOP);
            }
        } else {
            if (y > Dungeon.level.height()/2){
                bolt = new Ballistica(ch.pos, ch.pos - Dungeon.level.width(), Ballistica.WONT_STOP);
            } else {
                bolt = new Ballistica(ch.pos, ch.pos + Dungeon.level.width(), Ballistica.WONT_STOP);
            }
        }

        ConeAOE aoe = new ConeAOE(bolt, radius, 360, Ballistica.STOP_TARGET);

        for (Ballistica ray : aoe.outerRays){
            ((MagicMissile)ch.sprite.parent.recycle( MagicMissile.class )).reset(
                    MagicMissile.STAR,
                    ch.sprite,
                    ray.path.get(Math.min(radius / 2, ray.path.size()-1)),
                    null
            );
        }


        ((MagicMissile)ch.sprite.parent.recycle( MagicMissile.class )).reset(
                MagicMissile.STAR,
                ch.sprite,
                bolt.path.get(Math.min(radius / 2, bolt.path.size()-1)),
                () -> {
                    for (int pos : aoe.cells){
                        Char enemy = Actor.findChar(pos);
                        if (enemy != null) {
                            int shield = level() + 2;
                            if (enemy.alignment == Char.Alignment.ENEMY) {
                                enemy.damage(damageRoll() == 0 ? 1:damageRoll(), new DM100.LightningBolt());
                                enemy.sprite.burst(0xFFFFFFFF, level() / 2 + 2);
                            } else if(enemy.alignment == Char.Alignment.ALLY || enemy instanceof Hero) {
                                Buff.affect(enemy, Barrier.class).setShield(shield);
                            }
                        }
                    }

                    int target = ch.pos;
                    final Ballistica shot = new Ballistica(curUser.pos, target, curWand.collisionProperties(target));
                    int cell = shot.collisionPos;
                    curUser.sprite.zap(cell);
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
                });
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


        Sample.INSTANCE.play(Assets.Sounds.ZAP);
    }

    @Override
    public void fx(Ballistica beam, Callback callback) {
        Char target = Actor.findChar(beam.collisionPos);
        if(target != null){
            target.sprite.parent.add(
                    new Beam.DeathRay(target.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(target.pos)));
        }
        callback.call();
    }

    @Override
    public void staffFx(MagesStaff.StaffParticle particle) {
            particle.color( ColorMath.random(0xE44D3C, Window.RADISH) );
            particle.am = 1f;
            particle.setLifespan(7f);
            particle.setSize( 0.8f, 1.2f);
            float radius = 2f;
            int point = Random.Int(10);
            float angle;
            if (point % 2 == 0) {
                angle = point * 36f;
            } else {
                angle = point * 36f;
                radius *= 0.4f;
            }

            float rad = angle * (float)Math.PI / 180f;

            float offsetX = (float)Math.cos(rad) * radius;
            float offsetY = (float)Math.sin(rad) * radius;

            particle.x += offsetX;
            particle.y += offsetY;

            particle.shuffleXY(0.4f);

            particle.speed.x = -offsetY * 0.55f;
            particle.speed.y = offsetX * 0.55f;
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

                curUser.busy();
                curUser.sprite.zap(cell);

                if(curCharges > 0 && !curWand.cursed){
                    GetZap(targetChar,0);
                }

                if (Actor.findChar(target) != null) {
                    if (targetChar.alignment != Char.Alignment.ENEMY) {
                        target = targetChar.pos;
                        QuickSlotButton.target(Actor.findChar(target));
                    }
                } else {
                    QuickSlotButton.target(Actor.findChar(cell));
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
    public String upgradeStat1(int level) {
        int radius = 3+ (level()/4)*2;
        return Integer.toString(radius);
    }

    @Override
    public String upgradeStat2(int level) {
        return Integer.toString(level+2);
    }

    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
        int triggerChance = 20;
        int wandTotalLevel = 0;
        if (level() <= 12) {
            triggerChance += level() * 2;
        } else {
            triggerChance += 24;
            triggerChance += (level() - 12) / 2;
        }

        triggerChance = Math.min(triggerChance, 100);

        ArrayList<Wand> wands = hero.belongings.getAllItems(Wand.class);
        for (Wand w : wands.toArray(new Wand[0])){
            wandTotalLevel += w.level();
        }

        wandTotalLevel += staff.level();
        if (Random.Int(100) < triggerChance ) {
            if(hero.buff(Healing.StarHealing.class)==null){
                Buff.affect(hero, Healing.StarHealing.class).setHeal(wandTotalLevel, 0, wandTotalLevel/4);
            }
        }
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
