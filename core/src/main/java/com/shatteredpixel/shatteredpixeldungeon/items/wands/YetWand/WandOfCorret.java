package com.shatteredpixel.shatteredpixeldungeon.items.wands.YetWand;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class WandOfCorret extends DamageWand {

    {
        image = ItemSpriteSheet.WAND_DISINTEGRATION;

        collisionProperties = Ballistica.WONT_STOP;
    }


    public int min(int lvl){
        return hero.hasTalent(Talent.NOHOPE_LANG) && Dungeon.hero.HP < Dungeon.hero.HT/4 ? (int) (1.5f * 12 + Dungeon.depth) : 12 + Dungeon.depth;
    }

    public int max(int lvl){
        return hero.hasTalent(Talent.NOHOPE_LANG) && Dungeon.hero.HP < Dungeon.hero.HT/4 ? (int) (1.5f * 12 + Dungeon.depth) : 12 + Dungeon.depth;
    }

    @Override
    public int targetingPos(Hero user, int dst) {
        return dst;
    }

    @Override
    public void onZap(Ballistica beam) {

        int maxDistance = Math.min(distance(), beam.dist);

        ArrayList<Char> chars = new ArrayList<>();

        for (int c : beam.subPath(1, maxDistance)) {
            Char ch;
            if ((ch = Actor.findChar(c)) != null) {
                // 检查角色是否是被动状态且未被发现
                if (ch instanceof Mob && ((Mob) ch).state == ((Mob) ch).PASSIVE
                        && !(Dungeon.level.mapped[c] || Dungeon.level.visited[c])) {
                    // 避免伤害未被发现的被动角色
                } else {
                    chars.add(ch);
                }
            }

            CellEmitter.center(c).burst(PurpleParticle.BURST, Random.IntRange(1, 2));
        }

        int fixedDamage = 12 + Dungeon.depth;
        int fixedDamagePlus = 0;

        boolean damageDealt = false;
        for (Char ch : chars) {
            if (!damageDealt) {
                wandProc(ch, chargesPerCast());
                if (ch.properties().contains(Char.Property.DEMONIC) || ch.properties().contains(Char.Property.UNDEAD)) {
                    fixedDamage = (int) (fixedDamage * 1.5f);
                }
                ch.damage(fixedDamage + fixedDamagePlus, this);
                ch.sprite.centerEmitter().burst(PurpleParticle.BURST, Random.IntRange(1, 2));
                ch.sprite.flash();
                damageDealt = true;
            }
        }

        Belief creaditSkills = Dungeon.hero.buff(Belief.class);
        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1 && creaditSkills != null && hero.buff(Talent.NoBeliefUsedCooldown.class) == null) {
            Buff.affect(hero, Talent.NoBeliefUsedCooldown.class, 500f);
        } else if(creaditSkills!= null) {
            creaditSkills.DownBelief(5);
        }

        if (hero.hasTalent(Talent.DEVOTIONAL)){
            Buff.affect(hero, Barrier.class).setShield(4 * hero.pointsInTalent(Talent.DEVOTIONAL));
            hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(4 * hero.pointsInTalent(Talent.DEVOTIONAL)), FloatingText.SHIELDING );
        }
    }

    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
        //no direct effect, see magesStaff.reachfactor
    }

    private int distance() {
        return buffedLvl()*2 + 6;
    }

    @Override
    public void fx(Ballistica beam, Callback callback) {

        int cell = beam.path.get(Math.min(beam.dist, distance()));
        curUser.sprite.parent.add(new Beam.DeathRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld( cell )));
        callback.call();
    }

    @Override
    public void staffFx(MagesStaff.StaffParticle particle) {
        particle.color(0x220022);
        particle.am = 0.6f;
        particle.setLifespan(1f);
        particle.acc.set(10, -10);
        particle.setSize( 0.5f, 3f);
        particle.shuffleXY(1f);
    }

}

