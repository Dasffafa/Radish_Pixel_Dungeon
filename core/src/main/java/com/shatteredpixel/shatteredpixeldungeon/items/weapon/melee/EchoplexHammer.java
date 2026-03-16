package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class EchoplexHammer extends MeleeWeapon {

    {
        image = ItemSpriteSheet.CALLHAMR;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 0.8f;
        tier = 5;
    }

    @Override
    public int STRReq(int lvl) {
        return (9 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    public int min(int lvl) {
        return 10 + lvl * 2;
    }
    @Override
    public int max(int lvl) {
        return 30 + lvl * 5;
    }

    public int proc(Char attacker, Char defender, int damage ) {
        int dmg = damage;
        for (ChampionEnemy buff : defender.buffs(ChampionEnemy.class)){
            dmg = (int) Math.ceil(dmg * buff.damageTakenFactor());
        }
        if (defender.HP <= dmg){
            KillEffect(this, attacker, defender);
        }
        return super.proc(attacker, defender, damage);
    }

    public static void KillEffect( Weapon weapon , Char attacker, Char defender ){
        Sample.INSTANCE.play( Assets.Sounds.LARGE_BELL , 0.7f , Random.Float(0.8f,1.2f));

        Char killedMob = null;

        int radius = 10;
        WandOfBlastWave.BlastWave.blast(defender.pos, radius);

        Mob[] mobs = Dungeon.level.mobs.toArray(new Mob[0]);
        for (Mob mob : mobs) {
            if (mob.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[mob.pos]) {
                mob.damage(10 + 2 * weapon.level() , weapon );
                if (!mob.isAlive()) {
                    killedMob = mob;
                    mob.die(attacker);
                }
            }
        }
        Actor.addDelayed(mobs[0],1);
        if (killedMob != null){
            KillEffect(weapon, attacker, killedMob);
        }
        // 不可以删除
        Dungeon.observe();
    }

    @Override
    public String desc() {

        String desc;

        if(isIdentified()){
            desc = Messages.get(this, "desc",10 + 2 * level());
        } else {
            desc = Messages.get(this, "normal_desc",10);
        }

        return desc;
    }

}
