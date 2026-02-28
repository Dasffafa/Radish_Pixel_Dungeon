package com.shatteredpixel.shatteredpixeldungeon.levels;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.depth;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Random;

public class SmallGrassMiniLevel extends SewerLevel{

    @Override
    public void playLevelMusic(){
        Music.INSTANCE.play(Assets.Music.SEWERS_TENSE, true);
    }

    @Override
    protected int standardRooms(boolean forceMax) {
        if (forceMax) return 2;
        return 2+ Random.chances(new float[]{1, 3, 1});
    }

    @Override
    protected int specialRooms(boolean forceMax) {
        if (forceMax) return 1;
        return 1+Random.chances(new float[]{1, 2});
    }

    public String tilesTex() {
        return Assets.Environment.TILES_MOSS;
    }

    public String waterTex() {
        return Assets.Environment.WATER_MOSS;
    }

    @Override
    public boolean activateTransition(Hero hero, LevelTransition transition) {
        if (Dungeon.branch != 0) {
            if (transition.type == LevelTransition.Type.REGULAR_EXIT) {
                if(Dungeon.branch == 2){
                    TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
                    if (timeFreeze != null) timeFreeze.disarmPresses();
                    Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
                    if (timeBubble != null) timeBubble.disarmPresses();
                    InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
                    InterlevelScene.curTransition = new LevelTransition();
                    InterlevelScene.curTransition.destDepth = depth;
                    InterlevelScene.curTransition.destType = LevelTransition.Type.BRANCH_EXIT;
                    InterlevelScene.curTransition.destBranch = 0;
                    InterlevelScene.curTransition.type = LevelTransition.Type.BRANCH_EXIT;
                    InterlevelScene.curTransition.centerCell = -1;
                    Game.switchScene(InterlevelScene.class);
                } else {
                    TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
                    if (timeFreeze != null) timeFreeze.disarmPresses();
                    Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
                    if (timeBubble != null) timeBubble.disarmPresses();
                    InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
                    InterlevelScene.curTransition = new LevelTransition();
                    InterlevelScene.curTransition.destDepth = depth;
                    InterlevelScene.curTransition.destType = LevelTransition.Type.BRANCH_EXIT;
                    InterlevelScene.curTransition.destBranch = Dungeon.branch+1;
                    InterlevelScene.curTransition.type = LevelTransition.Type.BRANCH_EXIT;
                    InterlevelScene.curTransition.centerCell = -1;
                    Game.switchScene(InterlevelScene.class);
                }
                return false;
            } else if(transition.type == LevelTransition.Type.REGULAR_ENTRANCE && Dungeon.branch > 1) {
                TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
                if (timeFreeze != null) timeFreeze.disarmPresses();
                Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
                if (timeBubble != null) timeBubble.disarmPresses();
                InterlevelScene.mode = InterlevelScene.Mode.ASCEND;
                InterlevelScene.curTransition = new LevelTransition();
                InterlevelScene.curTransition.destDepth = depth;
                InterlevelScene.curTransition.destType = LevelTransition.Type.BRANCH_EXIT;
                InterlevelScene.curTransition.destBranch = Dungeon.branch+1;
                InterlevelScene.curTransition.type = LevelTransition.Type.BRANCH_EXIT;
                InterlevelScene.curTransition.centerCell = -1;
                Game.switchScene(InterlevelScene.class);
                return false;
            } else {
                TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
                if (timeFreeze != null) timeFreeze.disarmPresses();
                Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
                if (timeBubble != null) timeBubble.disarmPresses();
                InterlevelScene.mode = InterlevelScene.Mode.ASCEND;
                InterlevelScene.curTransition = new LevelTransition();
                InterlevelScene.curTransition.destDepth = depth;
                InterlevelScene.curTransition.destType = LevelTransition.Type.BRANCH_EXIT;
                InterlevelScene.curTransition.destBranch = 0;
                InterlevelScene.curTransition.type = LevelTransition.Type.BRANCH_EXIT;
                InterlevelScene.curTransition.centerCell = -1;
                Game.switchScene(InterlevelScene.class);
                return false;
            }
        } else {
            return super.activateTransition(hero,transition);
        }
    }

}
