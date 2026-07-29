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

    // SND 音乐列表 - 从 assets/music/snd/ 子文件夹中收集
    private static final String[] SND_MUSIC_TRACKS = {
            "music/snd/aleksander/Defense Ready.ogg",
            "music/snd/aleksander/Dicing Opponents.ogg",
            "music/snd/aleksander/Into The Depths.ogg",
            "music/snd/aleksander/Next Battle Awaits.ogg",
            "music/snd/andrew goodwin/Black Castle.ogg",
            "music/snd/andrew goodwin/No Turning Back.ogg",
            "music/snd/andrew goodwin/The Witches Castle.ogg",
            "music/snd/andrew goodwin/What The Smoke Conceals.ogg",
            "music/snd/andrew goodwin/Withering Thoughts.ogg",
            "music/snd/cold sanctum/CONJURING SINISTER WIZARDRY.ogg",
            "music/snd/cold sanctum/LINGERING DESOLATE GLOOM.ogg",
            "music/snd/louigi verona/Ancient Books of Magic.ogg",
            "music/snd/louigi verona/Bounty Hunters.ogg",
            "music/snd/louigi verona/Dark Enchantments.ogg",
            "music/snd/louigi verona/Deadly Encounter.ogg",
            "music/snd/louigi verona/Spellcasters Galore.ogg",
            "music/snd/louigi verona/War Machines.ogg",
            "music/snd/roho/Swiftsoles v3.ogg",
            "music/snd/ziggurath/Assassins_ Dirge.ogg",
            "music/snd/ziggurath/Gemstones and Stratagems.ogg",
            "music/snd/ziggurath/Steel Wins Battles.ogg",
            "music/snd/ziggurath/Veteran of 1000 Rolls.ogg"
    };

    // 所有曲目概率相同
    private static final float[] SND_MUSIC_CHANCES;
    static {
        SND_MUSIC_CHANCES = new float[SND_MUSIC_TRACKS.length];
        for (int i = 0; i < SND_MUSIC_CHANCES.length; i++) {
            SND_MUSIC_CHANCES[i] = 1.0f;
        }
    }

    public static boolean active() {
        return Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClass.DICE_MAGE;
    }

    public static void load() {
        Sample.INSTANCE.load(Assets.Sounds.sndAll);
    }

    public static boolean playLevelMusic() {
        if (!active()) return false;
        // 使用 playTracks 播放随机音乐列表，启用 shuffle 打乱顺序
        Music.INSTANCE.playTracks(SND_MUSIC_TRACKS, SND_MUSIC_CHANCES, true);
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

    public static void cast(String sound) {
        play(sound, 1f, 1f);
    }

    public static void kill() {
        death();
    }

    private static void play(String sound, float volume, float pitch) {
        if (active()) {
            Sample.INSTANCE.play(sound, volume, pitch);
        }
    }
}
