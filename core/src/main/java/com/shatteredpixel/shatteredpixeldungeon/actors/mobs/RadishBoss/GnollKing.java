package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollKingSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class GnollKing extends Mob {

    private int stickCooldown;
    public int summonCooldownLimit;
    private int summonCooldown;

    {
        spriteClass = GnollKingSprite.class;
        HT = HP = 150;
        defenseSkill = 15;

        maxLvl = 30;

        HUNTING = new Hunting();

        properties.add(Property.BOSS);
    }

    private int lastEnemyPos = -1;

    @Override
    protected boolean act() {

        boolean result = super.act();

        if (state == WANDERING){
            leapPos = -1;
            charging = false;
        }

        AiState lastState = state;

        if (paralysed <= 0) leapCooldown --;

        if (!(lastState == WANDERING && state == HUNTING)) {
            if (enemy != null) {
                lastEnemyPos = enemy.pos;
            } else {
                lastEnemyPos = Dungeon.hero.pos;
            }
        }

        if(summonCooldown <= 0){
            summonCooldown = 25;
            summonGnollShadow(pos);
        } else {
            summonCooldown--;
        }


        return result;
    }

    private void summonGnollShadow(int pos){
        if(summonCooldownLimit <= 0){
            ((GnollKingSprite)sprite).shadow( pos );
            Mob gs1 = new GnollGuardShadow();
            gs1.state = gs1.HUNTING;
            GameScene.add(gs1);
            ScrollOfTeleportation.appear(gs1,178);

            Mob gs2 = new GnollGuardShadow();
            gs2.state = gs2.HUNTING;
            GameScene.add(gs2);
            ScrollOfTeleportation.appear(gs2,182);
            summonCooldownLimit  = 2;
        } else {
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
                if (mob instanceof GnollGuardShadow) {
                    Buff.affect(mob, Barrier.class).setShield( 20 );
                }
            }
        }

    }

    public static class GnollGuardShadow extends GnollGuard {

        {
            maxLvl = -1;
        }

        @Override
        public void die( Object cause ) {
            super.die(cause);
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
                if (mob instanceof GnollKing) {
                    ((GnollKing) mob).summonCooldownLimit--;
                }
            }
        }

    }

    @Override
    public int attackSkill( Char target ) {
        return 20;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Char.combatRoll(0, 6);
    }

    @Override
    public int damageRoll() {
        return Char.combatRoll( 10, 25 );
    }

    private static final String STICK_COOLDOWN = "stick_cooldown";
    private static final String LAST_ENEMY_POS = "last_enemy_pos";
    private static final String LEAP_POS = "leap_pos";
    private static final String LEAP_CD = "leap_cd";
    private static final String CHARGING = "charging";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(STICK_COOLDOWN, stickCooldown);

        bundle.put(LAST_ENEMY_POS, lastEnemyPos);
        bundle.put(LEAP_POS, leapPos);
        bundle.put(LEAP_CD, leapCooldown);
        bundle.put(CHARGING, charging);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        stickCooldown = bundle.getInt(STICK_COOLDOWN);

        lastEnemyPos = bundle.getInt(LAST_ENEMY_POS);
        leapPos = bundle.getInt(LEAP_POS);
        leapCooldown = bundle.getFloat(LEAP_CD);
        charging = bundle.getBoolean(CHARGING);
    }

    private int leapPos = -1;
    private float leapCooldown = 0;
    private boolean charging = false;
    private List<Char> leapTargets = new ArrayList<>();
    private List<Integer> leapPath = new ArrayList<>();

    public class Hunting extends Mob.Hunting {

        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {

            if (leapPos != -1){

                leapCooldown = 8;

                if (rooted){
                    leapPos = -1;
                    charging = false;
                    leapTargets.clear();
                    leapPath.clear();
                    return true;
                }

                Ballistica b = new Ballistica(pos, leapPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                int targetPos = b.collisionPos;
                int distance = (int) Math.min(4, Dungeon.level.trueDistance(pos, targetPos));
                List<Integer> path = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID).subPath(1, distance);
                int endPos = path.get(path.size() - 1);

                // 保存突进路径
                leapPath.clear();
                leapPath.addAll(path);

                // 收集沿途的所有目标
                leapTargets.clear();
                for (int p : path) {
                    Char victim = Actor.findChar(p);
                    if (victim != null && victim != GnollKing.this) {
                        leapTargets.add(victim);
                    }
                }

                sprite.visible = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[targetPos] || Dungeon.level.heroFOV[endPos];
                ((GnollKingSprite)sprite).stick( endPos );
                sprite.jump(pos, leapPos, 0f, 0.5f, new Callback() {
                    @Override
                    public void call() {
                        for (Char victim : leapTargets) {
                            if(!(victim instanceof GnollShamanKing)){
                                int damage = 25;
                                victim.damage(damage, this);
                                victim.sprite.flash();
                                Sample.INSTANCE.play(Assets.Sounds.HIT);

                                if (victim.isAlive()) {
                                    victim.sprite.showStatus(CharSprite.NEUTRAL, victim.defenseVerb());
                                }
                            }
                        }
                        leapTargets.clear();
                        leapPath.clear();

                        // 检查终点是否有其他角色
                        Char endPosChar = Actor.findChar(leapPos);
                        if (endPosChar != null && endPosChar != GnollKing.this) {
                            // 找到终点周围8个方向中可用的随机位置
                            int newPos = -1;
                            List<Integer> validPositions = new ArrayList<>();
                            for (int i : PathFinder.NEIGHBOURS8) {
                                int p = leapPos + i;
                                // 检查坐标是否在地图范围内、可通行且没有角色
                                if (p >= 0 && p < Dungeon.level.length() &&
                                        Dungeon.level.passable[p] &&
                                        Actor.findChar(p) == null) {
                                    validPositions.add(p);
                                }
                            }

                            if (!validPositions.isEmpty()) {
                                // 随机选择一个有效的位置
                                newPos = validPositions.get(Random.Int(validPositions.size()));
                                Actor.add(new Pushing(GnollKing.this, leapPos, newPos));
                            } else {
                                // 如果没有可用位置，就不移动
                                newPos = pos;
                            }

                            pos = newPos;
                        } else {
                            // 如果终点没有其他角色，直接移动到终点
                            pos = leapPos;
                        }

                        Dungeon.level.occupyCell(GnollKing.this);
                        leapPos = -1;
                        charging = false;
                        sprite.idle();
                        next();
                    }
                });
                return false;
            }

            enemySeen = enemyInFOV;
            if (enemyInFOV && !isCharmedBy( enemy ) && canAttack( enemy )) {

                return doAttack( enemy );

            } else {

                if (enemyInFOV) {
                    target = enemy.pos;
                } else if (enemy == null) {
                    state = WANDERING;
                    target = Dungeon.level.randomDestination( GnollKing.this );
                    return true;
                }

                if (leapCooldown <= 0 && enemyInFOV && !rooted
                        && Dungeon.level.distance(pos, enemy.pos) >= 1) {

                    int targetPos = enemy.pos;
                    if (lastEnemyPos != enemy.pos){
                        int closestIdx = 0;
                        for (int i = 1; i < PathFinder.CIRCLE8.length; i++){
                            if (Dungeon.level.trueDistance(lastEnemyPos, enemy.pos+PathFinder.CIRCLE8[i])
                                    < Dungeon.level.trueDistance(lastEnemyPos, enemy.pos+PathFinder.CIRCLE8[closestIdx])){
                                closestIdx = i;
                            }
                        }
                        targetPos = enemy.pos + PathFinder.CIRCLE8[(closestIdx+4)%8];
                    }

                    Ballistica b = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                    if (b.collisionPos != targetPos && targetPos != enemy.pos){
                        targetPos = enemy.pos;
                        b = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                    }
                    if (b.collisionPos == targetPos){
                        int distance = (int) Math.min(4, Dungeon.level.trueDistance(pos, targetPos));
                        List<Integer> path = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID).subPath(1, distance);
                        int actualEndPos = path.get(path.size() - 1);

                        if (!charging) {
                            charging = true;
                            leapPos = actualEndPos;

                            leapPath.clear();
                            leapPath.addAll(path);

                            spend(TICK);

                            if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[leapPos]){
                                GLog.w(Messages.get(GnollKing.this, "leap"));

                                for (int p : leapPath) {
                                    sprite.parent.addToBack(new TargetedCell(p, 0xFF0000));
                                }

                                ((GnollKingSprite)sprite).stickAttack( leapPos );
                                Dungeon.hero.interrupt();
                            }
                            return true;
                        } else {
                            spend(GameMath.gate(attackDelay(), (int)Math.ceil(enemy.cooldown()), 3*attackDelay()));
                            return true;
                        }
                    }
                }

                int oldPos = pos;
                if (target != -1 && getCloser( target )) {

                    spend( 1 / speed() );
                    return moveSprite( oldPos,  pos );

                } else {
                    spend( TICK );
                    if (!enemyInFOV) {
                        sprite.showLost();
                        state = WANDERING;
                        target = Dungeon.level.randomDestination( GnollKing.this );
                    }
                    return true;
                }
            }
        }

    }


}
