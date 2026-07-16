package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;

/**
 * Slice&Dice-inspired audio layer for Dice Mage.
 *
 * Imported Slice&Dice assets intentionally live under dedicated asset subfolders:
 * - sounds/snd/*
 * - music/snd/*
 * so they are not mixed with Radish/Shattered's original audio files.
 */
public class DiceMageAudio {

    public static boolean active() {
        return Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClass.DICE_MAGE;
    }

    public static void load() {
        Sample.INSTANCE.load(Assets.Sounds.sndAll);
    }

    public static boolean playLevelMusic() {
        if (!active()) return false;
        Music.INSTANCE.play(Assets.Music.SND_DICE_MAGE, true);
        return true;
    }

    public static void hit(float pitch) {
        play(Assets.Sounds.SND_ATTACK_HIT, 1f, pitch);
    }

    public static void miss() {
        play(Assets.Sounds.SND_ATTACK_MISS, 0.8f, 1f);
    }

    public static void death() {
        play(Assets.Sounds.SND_DEATH, 1f, 1f);
    }

    public static void cast(DiceMageSpellFX.Type type) {
        play(soundFor(type), 1f, 1f);
    }

    public static void kill(DiceMageSpellFX.Type type) {
        death();
    }

    private static void play(String sound, float volume, float pitch) {
        if (active()) {
            Sample.INSTANCE.play(sound, volume, pitch);
        }
    }

    private static String soundFor(DiceMageSpellFX.Type type) {
        switch (type) {
            case CUT:
                return Assets.Sounds.SND_SPELL_CUT;
            case HEAL:
                return Assets.Sounds.SND_SPELL_HEAL;
            case SOOTHE:
                return Assets.Sounds.SND_SPELL_SOOTHE;
            case LIQUOR:
                return Assets.Sounds.SND_SPELL_LIQUOR;
            case OPERATE:
                return Assets.Sounds.SND_SPELL_OPERATE;
            case MIASMA:
                return Assets.Sounds.SND_SPELL_MIASMA;
            case CRUSH:
                return Assets.Sounds.SND_SPELL_CRUSH;
            case BLAZE:
                return Assets.Sounds.SND_SPELL_BLAZE;
            default:
                return Assets.Sounds.SND_SPELL_CUT;
        }
    }
}
