/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

public class LevelTransition extends Rect implements Bundlable {

	public enum Type {
			SURFACE,
			REGULAR_ENTRANCE,
			REGULAR_EXIT,
			BRANCH_ENTRANCE,
			BRANCH_EXIT;
		}

		public Type type;
		public String branchId;      // 此楼梯所属的分支（用于精确配对）
		public int destDepth;
		public String destBranchId;  // 目标分支的字符串标识（如 "main", "moss", "mining"）
		public Type destType;

		public int centerCell;

	//for bundling
	public LevelTransition(){
		super();
	}

	// 完整构造函数：手动指定所有参数
		public LevelTransition(Level level, int cell, Type type, int destDepth, String destBranchId, Type destType){
			centerCell = cell;
			Point p = level.cellToPoint(cell);
			set(p.x, p.y, p.x, p.y);
			this.type = type;
			this.branchId = Dungeon.branchId;  // 默认为当前分支
			this.destDepth = destDepth;
			this.destBranchId = destBranchId;
			this.destType = destType;
		}

		//gives default values for common transition types
		public LevelTransition(Level level, int cell, Type type){
			centerCell = cell;
			Point p = level.cellToPoint(cell);
			set(p.x, p.y, p.x, p.y);
			this.type = type;
			this.branchId = Dungeon.branchId;  // 默认为当前分支
			switch (type){
				case REGULAR_ENTRANCE: default:
					destDepth = Dungeon.depth-1;
					destBranchId = Dungeon.branchId;
					destType = Type.REGULAR_EXIT;
					break;
				case REGULAR_EXIT:
					destDepth = Dungeon.depth+1;
					destBranchId = Dungeon.branchId;
					destType = Type.REGULAR_ENTRANCE;
					break;
				case SURFACE:
					destDepth = 1;
					destBranchId = Branches.MAIN;
					destType = null;
					break;
				case BRANCH_ENTRANCE:
					// BRANCH_ENTRANCE: 支线入口，返回主线
					// 需要手动设置 destDepth, destBranchId, destType
					break;
				case BRANCH_EXIT:
					// BRANCH_EXIT: 通往支线
					// 需要手动设置 destDepth, destBranchId, destType
					break;
			}
		}

	//note that the center cell isn't always the actual center.
	// It is important when game logic needs to pick a specific cell for some action
	// e.g. where to place the hero
	public int cell(){
		return centerCell;
	}

	//Transitions are inclusive to their right and bottom sides
	@Override
	public int width() {
		return super.width()+1;
	}

	@Override
	public int height() {
		return super.height()+1;
	}

	@Override
	public boolean inside(Point p) {
		return p.x >= left && p.x <= right && p.y >= top && p.y <= bottom;
	}

	public boolean inside(int cell){
		return inside(new Point(Dungeon.level.cellToPoint(cell)));
	}

	public Point center() {
		return new Point(
				(left + right) / 2 + (((right - left) % 2) == 1 ? Random.Int( 2 ) : 0),
				(top + bottom) / 2 + (((bottom - top) % 2) == 1 ? Random.Int( 2 ) : 0) );
	}

	public static final String TYPE = "type";
		public static final String BRANCH_ID = "branch_id";
		public static final String DEST_DEPTH = "dest_depth";
		public static final String DEST_BRANCH_ID = "dest_branch_id";
		public static final String DEST_TYPE = "dest_type";

		@Override
		public void storeInBundle(Bundle bundle) {
			bundle.put( "left", left );
			bundle.put( "top", top );
			bundle.put( "right", right );
			bundle.put( "bottom", bottom );

			bundle.put( "center", centerCell );

			bundle.put(TYPE, type);
			bundle.put(BRANCH_ID, branchId);
			bundle.put(DEST_DEPTH, destDepth);
			bundle.put(DEST_BRANCH_ID, destBranchId);
			bundle.put(DEST_TYPE, destType);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			left = bundle.getInt( "left" );
			top = bundle.getInt( "top" );
			right = bundle.getInt( "right" );
			bottom = bundle.getInt( "bottom" );

			centerCell = bundle.getInt( "center" );

			type = bundle.getEnum(TYPE, Type.class);
		
			// branchId 兼容旧存档
			if (bundle.contains(BRANCH_ID)) {
				branchId = bundle.getString(BRANCH_ID);
			} else {
				branchId = Dungeon.branchId;  // 默认为当前分支
			}
		
			destDepth = bundle.getInt(DEST_DEPTH);
		
			// destBranchId 兼容旧存档
			if (bundle.contains(DEST_BRANCH_ID)) {
				destBranchId = bundle.getString(DEST_BRANCH_ID);
			} else if (bundle.contains("dest_branch")) {
				// 旧格式迁移
				int oldBranch = bundle.getInt("dest_branch");
				destBranchId = oldBranch == 0 ? Branches.MAIN : (oldBranch == 1 ? Branches.MOSS : Branches.MINING);
			} else {
				destBranchId = Branches.MAIN;
			}
		
			destType = bundle.getEnum(DEST_TYPE, Type.class);
		}
	}
