package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;

/**
 * 循环门的外观：楼梯周围一圈光圈。
 */
public class CycleStairVisual extends CustomTilemap {

	{
		texture = Assets.Environment.CYCLE_STAIR;
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