/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 */
package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

public class LevelTransition extends Rect implements Bundlable {
	public enum Type {
		SURFACE, REGULAR_ENTRANCE, REGULAR_EXIT, BRANCH_ENTRANCE, BRANCH_EXIT
	}

	public enum Direction {
		UP, DOWN, SURFACE;

		public Direction opposite() {
			if (this == UP) return DOWN;
			if (this == DOWN) return UP;
			return SURFACE;
		}
	}

	public String linkId;
	public Direction direction;
	public String destBranch;
	public int destDepth;
	public int centerCell;
	public Type type;

	/** 单向门：保留 transition 用于落点定位，但玩家不能从这里激活 */
	public boolean sealed = false;

	/** 逻辑门类型 */
	public GateType gate = GateType.TWO_WAY;
	/** 炸药门是否已被炸开 */
	public boolean gateOpened = false;
	/** 条件门要求的层数 */
	public int gateDepth = 1;
	/** 循环门当前序号 */
	public int gateIndex = 0;
	/** 条件门需要等待的事件类名 */
	public String gateEvent = "";

	/** 循环门的三段目的地 linkId */
	public String[] gateLinks = new String[0];

	/** 这扇门当前能否被英雄激活 */
	public boolean canUse(Hero hero) {
		if (sealed) {
			return false;
		}
		switch (gate) {
			case SINGLE_USE:
				return !Dungeon.usedGates.contains(linkId);
			case BOMB_ONLY:
				return gateOpened;
			case CONDITIONAL:
				return gateOpened;
			case ONE_WAY:
			case TWO_WAY:
			case CYCLE_ABC:
			default:
				return true;
		}
	}

	/** 成功通行后的状态变更 */
	public void onUse() {
		if (gate == GateType.SINGLE_USE) {
			Dungeon.usedGates.add(linkId);
		} else if (gate == GateType.CYCLE_ABC && gateLinks != null && gateLinks.length > 0) {
			gateIndex = (gateIndex + 1) % gateLinks.length;
			linkId = gateLinks[gateIndex];
		}
	}

	/** 不能通行时给玩家的提示 */
	public String blockedMessage() {
		if (sealed) {
			return Messages.get(LevelTransition.class, "blocked_one_way");
		}
		switch (gate) {
			case SINGLE_USE:
				return Messages.get(LevelTransition.class, "blocked_single_use");
			case BOMB_ONLY:
				return Messages.get(LevelTransition.class, "blocked_bomb");
			case CONDITIONAL:
				return Messages.get(LevelTransition.class, "blocked_condition");
			default:
				return Messages.get(LevelTransition.class, "blocked_one_way");
		}
	}

	// for bundling
	public LevelTransition() {
		super();
	}

	public LevelTransition(Level level, int cell, Type type) {
		this(type == Type.REGULAR_EXIT
				? regularExit(level, cell)
				: type == Type.REGULAR_ENTRANCE
				? regularEntrance(level, cell)
				: surface(level, cell));
		this.type = type;
	}

	private LevelTransition(LevelTransition source) {
		set(source.left, source.top, source.right, source.bottom);
		centerCell = source.centerCell;
		linkId = source.linkId;
		direction = source.direction;
		destBranch = source.destBranch;
		destDepth = source.destDepth;
		type = source.type;
	}

	private LevelTransition(Level level, int cell, String linkId, Direction direction,
							String destBranch, int destDepth) {
		if (linkId == null || linkId.isEmpty()) throw new IllegalArgumentException("Missing transition linkId");
		if (!Branches.exists(destBranch)) throw new IllegalArgumentException("Unknown destination branch: " + destBranch);
		if (direction != Direction.SURFACE
				&& (destDepth < 1 || destDepth > Branches.get(destBranch).maxDepth)) {
			throw new IllegalArgumentException("Invalid destination floor: " + destBranch + ":" + destDepth);
		}
		this.centerCell = cell;
		Point p = level.cellToPoint(cell);
		set(p.x, p.y, p.x, p.y);
		this.linkId = linkId;
		this.direction = direction;
		this.destBranch = destBranch;
		this.destDepth = destDepth;
		this.type = direction == Direction.DOWN ? Type.REGULAR_EXIT
				: direction == Direction.UP ? Type.REGULAR_ENTRANCE : Type.SURFACE;
	}

	public static LevelTransition up(Level level, int cell, String linkId,
								 String destBranch, int destDepth) {
		return new LevelTransition(level, cell, linkId, Direction.UP, destBranch, destDepth);
	}

	public static LevelTransition down(Level level, int cell, String linkId,
								   String destBranch, int destDepth) {
		return new LevelTransition(level, cell, linkId, Direction.DOWN, destBranch, destDepth);
	}

	public static LevelTransition regularEntrance(Level level, int cell) {
		if (Dungeon.depth == 1 && Branches.MAIN.equals(Dungeon.branchId)) return surface(level, cell);
		return up(level, cell, regularLinkId(Dungeon.branchId, Dungeon.depth - 1),
				Dungeon.branchId, Dungeon.depth - 1);
	}

	public static LevelTransition regularExit(Level level, int cell) {
		return down(level, cell, regularLinkId(Dungeon.branchId, Dungeon.depth),
				Dungeon.branchId, Dungeon.depth + 1);
	}

	public static LevelTransition surface(Level level, int cell) {
		return new LevelTransition(level, cell, "surface", Direction.SURFACE, Branches.MAIN, 0);
	}

	public static LevelTransition branchUp(Level level, int cell, String linkId,
									   String destBranch, int destDepth) {
		LevelTransition result = up(level, cell, linkId, destBranch, destDepth);
		result.type = Type.BRANCH_ENTRANCE;
		return result;
	}

	public static LevelTransition branchDown(Level level, int cell, String linkId,
										 String destBranch, int destDepth) {
		LevelTransition result = down(level, cell, linkId, destBranch, destDepth);
		result.type = Type.BRANCH_EXIT;
		return result;
	}

	public static String regularLinkId(String branch, int upperDepth) {
		return branch + ":" + upperDepth + "-" + (upperDepth + 1);
	}

	public int cell() {
		return centerCell;
	}

	@Override
	public int width() {
		return super.width() + 1;
	}

	@Override
	public int height() {
		return super.height() + 1;
	}

	@Override
	public boolean inside(Point p) {
		return p.x >= left && p.x <= right && p.y >= top && p.y <= bottom;
	}

	public boolean inside(int cell) {
		return inside(new Point(Dungeon.level.cellToPoint(cell)));
	}

	public Point center() {
		return new Point(
				(left + right) / 2 + (((right - left) % 2) == 1 ? Random.Int(2) : 0),
				(top + bottom) / 2 + (((bottom - top) % 2) == 1 ? Random.Int(2) : 0));
	}

	private static final String LINK_ID = "link_id";
	private static final String DIRECTION = "direction";
	private static final String DEST_BRANCH = "dest_branch";
	private static final String SEALED	= "sealed";
	private static final String GATE	= "gate";
	private static final String GATE_OPENED	= "gate_opened";
	private static final String GATE_DEPTH	= "gate_depth";
	private static final String GATE_INDEX	= "gate_index";
	private static final String GATE_LINKS	= "gate_links";
	private static final String GATE_EVENT	= "gate_event";
	private static final String DEST_DEPTH = "dest_depth";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put("left", left);
		bundle.put("top", top);
		bundle.put("right", right);
		bundle.put("bottom", bottom);
		bundle.put("center", centerCell);
		bundle.put(LINK_ID, linkId);
		bundle.put("type", type);
		bundle.put(DIRECTION, direction);
		bundle.put(DEST_BRANCH, destBranch);
		bundle.put(DEST_DEPTH, destDepth);
		bundle.put(SEALED, sealed);
		bundle.put(GATE, gate.name());
		bundle.put(GATE_OPENED, gateOpened);
		bundle.put(GATE_DEPTH, gateDepth);
		bundle.put(GATE_INDEX, gateIndex);
		bundle.put(GATE_LINKS, gateLinks);
		bundle.put(GATE_EVENT, gateEvent);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		left = bundle.getInt("left");
		top = bundle.getInt("top");
		right = bundle.getInt("right");
		bottom = bundle.getInt("bottom");
		centerCell = bundle.getInt("center");
		linkId = bundle.getString(LINK_ID);
		type = bundle.getEnum("type", Type.class);
		direction = bundle.getEnum(DIRECTION, Direction.class);
		destBranch = bundle.getString(DEST_BRANCH);
		destDepth = bundle.getInt(DEST_DEPTH);
		sealed = bundle.getBoolean(SEALED);
		String gateName = bundle.getString(GATE);
		//旧存档没有这个字段，不能直接 valueOf(null)
		gate = GateType.TWO_WAY;
		if (gateName != null) {
			try {
				gate = GateType.valueOf(gateName);
			} catch (IllegalArgumentException ignored) {
				gate = GateType.TWO_WAY;
			}
		}
		gateOpened = bundle.getBoolean(GATE_OPENED);
		gateDepth = bundle.getInt(GATE_DEPTH);
		gateIndex = bundle.getInt(GATE_INDEX);
		gateLinks = bundle.getStringArray(GATE_LINKS);
		gateEvent = bundle.getString(GATE_EVENT);
		if (gateEvent == null) gateEvent = "";
		if (gateLinks == null) gateLinks = new String[0];
		if (linkId == null || linkId.isEmpty() || type == null || direction == null || !Branches.exists(destBranch)) {
			throw new IllegalStateException("Invalid level transition in save data");
		}
	}
}
