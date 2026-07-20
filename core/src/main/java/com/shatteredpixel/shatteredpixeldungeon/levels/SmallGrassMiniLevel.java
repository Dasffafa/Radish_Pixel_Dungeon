package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.DiceMageAudio;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.watabou.noosa.audio.Music;

import java.util.ArrayList;

public class SmallGrassMiniLevel extends SewerLevel {

    @Override
    public void playLevelMusic(){
        if (DiceMageAudio.playLevelMusic()) return;
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
}