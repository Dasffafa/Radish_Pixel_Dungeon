package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.TransitionContract;
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

        int DragonPos = cx + cy * level.width();

        DreamcatcherMaker vis = new DreamcatcherMaker();
        vis.pos(c.x, c.y);
        level.customTiles.add(vis);

        // ====== 精确过渡系统：创建带 ID 的楼梯 ======
        // 生成楼梯 ID
        String sourceBranch = Dungeon.currentBranchId();
        int sourceDepth = Dungeon.depth;
        String destBranch = "moss";
        int destDepth = 1;

        String stairId = sourceBranch + "_" + sourceDepth + "_to_" + destBranch + "_" + destDepth;
        String destStairId = destBranch + "_" + destDepth + "_to_" + sourceBranch + "_" + sourceDepth;

        LevelTransition transition = new LevelTransition(level, DragonPos, LevelTransition.Type.BRANCH_EXIT);
        transition.id = stairId;
        transition.destDepth = destDepth;
        transition.destBranch = 1;  // 兼容旧系统
        transition.destBranchId = destBranch;
        transition.destId = destStairId;
        transition.destType = LevelTransition.Type.BRANCH_ENTRANCE;

        level.transitions.add(transition);
        Painter.set(level, DragonPos, Terrain.EXIT);

        // 注册约定，供目标楼层生成时使用
        TransitionContract contract = new TransitionContract(
            stairId,
            sourceDepth,
            sourceBranch,
            destDepth,
            destBranch,
            destStairId
        );
        Dungeon.registerTransitionContract(contract);

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