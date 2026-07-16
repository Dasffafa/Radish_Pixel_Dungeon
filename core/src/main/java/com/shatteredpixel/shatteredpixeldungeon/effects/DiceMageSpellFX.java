package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PoisonParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;

/**
 * Slice&Dice-inspired color/effect palette for Dice Mage spells.
 *
 * This intentionally uses Radish/Shattered's existing noosa effect primitives instead of
 * directly copying Slice&Dice GLSL runtime code, while keeping the visual language:
 * dark dice palette, sharp cut flashes, burn wipes, acid/poison haze, and purple surgery magic.
 */
public class DiceMageSpellFX {

    public enum Type {
        CUT(0xAD1F1F),
        HEAL(0x388044),
        SOOTHE(0xF1E5B5),
        LIQUOR(0xC45E16),
        OPERATE(0x6A4484),
        MIASMA(0x388044),
        CRUSH(0xB59E09),
        BLAZE(0xEE7722);

        public final int color;

        Type(int color) {
            this.color = color;
        }
    }

    public static void impact(Char target, Type type) {
        if (target == null || target.pos < 0) return;
        if (Dungeon.level == null || !Dungeon.level.heroFOV[target.pos]) return;

        DiceMageAudio.cast(type);

        switch (type) {
            case CUT:
                CellEmitter.center(target.pos).burst(BlastParticle.FACTORY, 6);
                CellEmitter.center(target.pos).burst(Speck.factory(Speck.RED_LIGHT), 2);
                break;
            case HEAL:
            case SOOTHE:
                CellEmitter.center(target.pos).start(Speck.factory(Speck.HEALING), 0.12f, 3);
                break;
            case LIQUOR:
                CellEmitter.center(target.pos).burst(SparkParticle.FACTORY, 5);
                break;
            case OPERATE:
                CellEmitter.center(target.pos).burst(PurpleParticle.BURST, 8);
                break;
            case MIASMA:
                CellEmitter.center(target.pos).burst(PoisonParticle.SPLASH, 8);
                break;
            case CRUSH:
                CellEmitter.center(target.pos).burst(BlastParticle.FACTORY, 10);
                CellEmitter.center(target.pos).start(Speck.factory(Speck.ROCK), 0.08f, 4);
                break;
            case BLAZE:
                CellEmitter.center(target.pos).burst(FlameParticle.FACTORY, 10);
                break;
        }
    }

    public static void damage(Char target, int damage, Object source, Type type) {
        if (target == null) return;

        boolean usesKillFX = target instanceof Mob && target.alignment == Char.Alignment.ENEMY;
        impact(target, type);
        if (usesKillFX) {
            target.suppressNextDeathAnimation();
        }
        target.damage(damage, source);
        if (!usesKillFX) {
            return;
        } else if (target.isAlive()) {
            target.clearDeathAnimationSuppression();
        } else {
            kill(target, type);
        }
    }

    public static void kill(Char target, Type type) {
        if (target == null || target.sprite == null) return;

        DiceMageAudio.kill(type);

        impact(target, type);
        if (target.sprite.parent != null) {
            new Flare(8, 24).color(type.color, true).show(target.sprite, 0.35f);
        }
        if (Dungeon.level != null && target.pos >= 0 && Dungeon.level.heroFOV[target.pos]) {
            CellEmitter.center(target.pos).burst(RainbowParticle.BURST, 4);
            if (type == Type.OPERATE) {
                CellEmitter.center(target.pos).start(ShadowParticle.UP, 0.04f, 8);
            }
        }
        target.sprite.killAndErase();
    }
}
