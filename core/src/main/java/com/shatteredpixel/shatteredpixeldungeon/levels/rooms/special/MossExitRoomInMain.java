/*
 * Radish Pixel Dungeon
 * 苔藓分支在主线第 3 层的出口房间（单向门）。
 *
 * 玩家从苔藓第 2 层通过传送门抵达这里，但无法从这里原路返回苔藓分支——
 * 这一侧只有落地点和一个封闭的传送门外观，踩上去不会触发任何传送。
 *
 * 注意：LevelTransition 仍然必须存在，因为 InterlevelScene 会用它来定位落点
 * （requireTransition 缺少对应 linkId 会直接抛异常），所以这里保留 transition，
 * 只把地形从 ENTRANCE 改成普通地面，使其不再可激活。
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.watabou.utils.Point;

public class MossExitRoomInMain extends SpecialRoom {

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
        return 7;
    }

    @Override
    public int maxHeight() {
        return 7;
    }

    @Override
    public boolean canMerge(Level level, Room other, Point point, int mergeTerrain) {
        return false;
    }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY);

        for (Room.Door door : connected.values()) {
            door.set(Room.Door.Type.REGULAR);
        }

        Point center = center();
        int entranceCell = level.pointToCell(center);

        //保留 transition 用于落点定位，但标记为 sealed：踩上去不会触发传送
        LevelTransition gate = LevelTransition.branchUp(level, entranceCell,
                "moss:main-3", Branches.MOSS, 2);
        gate.sealed = true;
        level.transitions.add(gate);
        Painter.set(level, entranceCell, Terrain.EMPTY);

        //封闭的传送门外观，提示这里曾经是一扇门
        MossExitRoom.MossPortalVisual vis = new MossExitRoom.MossPortalVisual();
        vis.pos(center.x, center.y);
        level.customTiles.add(vis);
    }

    @Override
    public void onLevelLoad(Level level) {
        super.onLevelLoad(level);

        for (LevelTransition transition : level.transitions) {
            if (inside(transition.center())) {
                //单向门：只保留落点功能，不再提供从主线返回苔藓的入口
                transition.linkId = "moss:main-3";
                transition.direction = LevelTransition.Direction.UP;
                transition.destBranch = Branches.MOSS;
                transition.destDepth = 2;
                transition.type = LevelTransition.Type.BRANCH_ENTRANCE;
                transition.sealed = true;
                Painter.set(level, transition.cell(), Terrain.EMPTY);
                return;
            }
        }
    }

    @Override
    public boolean connect(Room room) {
        if (room.isExit()) return false;
        return super.connect(room);
    }
}
