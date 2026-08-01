package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MossExitRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.EntranceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.ExitRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.noosa.audio.Music;

import java.util.ArrayList;

public class SmallGrassMiniLevel extends SewerLevel {

    @Override
    public void playLevelMusic(){
        // 默认音乐
        Music.INSTANCE.play(Assets.Music.SEWERS_1, true);
        Music.INSTANCE.play(Assets.Music.SEWERS_TENSE, true);
    }

    @Override
    protected int standardRooms(boolean forceMax) {
        if (forceMax) return 2;
        return 2;
    }

    @Override
    protected int specialRooms(boolean forceMax) {
        if (forceMax) return 1;
        return 1;
    }

    public String tilesTex() {
        return Assets.Environment.TILES_MOSS;
    }

    public String waterTex() {
        return Assets.Environment.WATER_MOSS;
    }

    /**
     * 覆盖：在房间中找一个合适的位置放置入口楼梯
     */
    @Override
    protected int findBranchEntranceCell() {
        // 优先在普通房间中找
        for (Room room : rooms) {
            if (room.isExit() || room.isEntrance()) continue;
            for (int i = 0; i < 10; i++) {
                int cell = pointToCell(room.random());
                if (map[cell] == Terrain.EMPTY || map[cell] == Terrain.EMPTY_DECO) {
                    return cell;
                }
            }
        }
        // 兜底：调用父类方法
        return super.findBranchEntranceCell();
    }

    /**
     * 覆盖：苔藓分支第2层使用特殊出口房间
     */
    @Override
    protected ArrayList<Room> initRooms() {
        ArrayList<Room> initRooms = new ArrayList<>();
        initRooms.add(roomEntrance = EntranceRoom.createEntrance());

        // 苔藓分支第2层使用 MossExitRoom（返回主线）
        if (Dungeon.depth == 2) {
            initRooms.add(roomExit = new MossExitRoom());
        } else {
            // 第1层正常生成出口
            initRooms.add(roomExit = ExitRoom.createExit());
        }

        // 标准房间
        int standards = standardRooms(false);
        for (int i = 0; i < standards; i++) {
            initRooms.add(StandardRoom.createRoom());
        }

        // 特殊房间
        int specials = specialRooms(false);
        for (int i = 0; i < specials; i++) {
            initRooms.add(SpecialRoom.createRoom());
        }

        return initRooms;
    }
}