package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss.GnollKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss.GnollShamanKing;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;

public class GnollKingBossLevel extends Level {

    private static final int W = WALL;
    private static final int E = Terrain.EMPTY;
    private static final int R = Terrain.EXIT;
    private static final int H = Terrain.HIGH_GRASS;
    private static final int P = Terrain.PEDESTAL;
    private static final int X = Terrain.ENTRANCE;
    private static final int D = WALL;

    private static final int WIDTH = 19;
    private static final int HEIGHT = 19;

    @Override
    public void unseal() {
        super.unseal();

    }

    @Override
    public void occupyCell( Char ch ) {
        super.occupyCell(ch);

       // GLog.w(String.valueOf(Dungeon.hero.pos));

//        if (map[entrance] == Terrain.STATUE && map[exit] != Terrain.EXIT
//                && ch == hero && level.distance(ch.pos, entrance) >= 2) {
//            seal();
//        }
    }


    private static final int[] code_map = {
        D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,
        D,W,W,W,W,W,W,W,W,R,W,W,W,W,W,W,W,W,D,
        D,W,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,W,D,
        D,W,E,E,E,E,W,W,W,E,W,W,W,E,E,E,E,W,D,
        D,W,E,E,E,E,E,E,W,W,W,E,E,E,E,E,E,W,D,
        D,W,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,W,D,
        D,W,E,E,W,W,E,E,E,E,E,E,E,W,W,E,E,W,D,
        D,W,E,W,W,E,E,E,E,E,E,E,E,E,W,W,E,W,D,
        D,W,E,W,E,E,E,E,E,E,E,E,E,E,E,W,E,W,D,
        D,W,E,E,E,E,E,E,E,X,E,E,E,E,E,E,E,W,D,
        D,W,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,W,D,
        D,W,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,W,D,
        D,W,E,H,H,H,H,E,P,E,P,E,H,H,H,H,E,W,D,
        D,W,E,H,E,E,H,E,E,E,E,E,H,E,E,H,E,W,D,
        D,W,E,H,H,H,H,E,H,H,H,E,H,H,H,H,E,W,D,
        D,W,E,E,E,E,E,E,H,E,H,E,E,E,E,E,E,W,D,
        D,W,E,E,E,E,E,E,H,H,H,E,E,E,E,E,E,W,D,
        D,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,D,
        D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,
    };


    {
        color1 = 5459774;
        color2 = 12179041;
    }

    protected boolean build() {
        setSize(WIDTH, HEIGHT);
        map = code_map.clone();

        int entrance = 180;
        int exit = 28;

        LevelTransition ecne = new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE);
        transitions.add(ecne);

        map[exit] = Terrain.LOCKED_EXIT;

        return true;
    }


    protected void createItems() {
    }

    @Override
    protected void createMobs() {
        GnollKing gk = new GnollKing();
        gk.pos = 236;
        mobs.add(gk);

        GnollShamanKing gsk = new GnollShamanKing();
        gsk.pos = 238;
        mobs.add(gsk);
    }

    public Actor respawner() {
        return null;
    }

    public String tilesTex() {
        return Assets.Environment.TILES_MOSS;
    }

    public String waterTex() {
        return Assets.Environment.WATER_MOSS;
    }

}

