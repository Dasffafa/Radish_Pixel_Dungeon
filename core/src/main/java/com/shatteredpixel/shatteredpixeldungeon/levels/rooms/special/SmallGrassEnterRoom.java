package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Point;

public class SmallGrassEnterRoom extends SpecialRoom {

    @Override
    public int minWidth() {
        return 5;
    }

    @Override
    public int minHeight() {
        return 5;
    }

    @Override
    public int maxWidth() {
        return 5;
    }

    @Override
    public int maxHeight() {
        return 5;
    }

    @Override
    public void paint(Level level) {

        Painter.fill( level, this, Terrain.WALL );
        Painter.fill( level, this, 1, Terrain.EMPTY );
        Painter.fillDiamond( level, this, 2, Terrain.WATER);

        Point c = center();
        int cx = c.x;
        int cy = c.y;

        int DragonPos = cx + cy * level.width();;

        DreamcatcherMaker vis = new DreamcatcherMaker();
        vis.pos(c.x, c.y);
        level.customTiles.add(vis);

        level.transitions.add(new LevelTransition(level,
                DragonPos,
                LevelTransition.Type.BRANCH_EXIT,
                2,
                Dungeon.branch+1,
                LevelTransition.Type.BRANCH_ENTRANCE));
        Painter.set(level, DragonPos, Terrain.EXIT);

        Door door = entrance();
        door.set(Door.Type.REGULAR);
    }

    @Override
    public boolean connect(Room room) {
        //cannot connect to entrance, otherwise works normally
        if (room.isExit())  return false;
        else                return super.connect(room);
    }

    public static class DreamcatcherMaker extends CustomTilemap {

        {
            texture = Assets.Environment.MOSS_ENTER;
            tileW = tileH = 1;

        }

        final int TEX_WIDTH = 16;

        @Override
        public Tilemap create() {
            Tilemap v = super.create();
            v.map(mapSimpleImage(0, 0, TEX_WIDTH), 1);
            return v;
        }

        @Override
        public String name(int tileX, int tileY) {
            return Messages.get(this, "name");
        }

        @Override
        public String desc(int tileX, int tileY) {
            return Messages.get(this, "desc");
        }
    }

    @Override
    public boolean canPlaceTrap(Point p) {
        return false;
    }
}

