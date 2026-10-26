package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branch;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.Game;

import java.util.ArrayList;

/**
 * 测试模式楼层生成器：可跳转到任意分支的任意层（含主线与副本），或重生成当前层。
 *
 * 「跳转」输入格式：<分支ID> <层数>，例如 `main 8`、`convict 1`、`mining 1`、`moss 2`。
 * 当前分支与层数会作为默认值填入。
 */
public class FloorTester extends TestItem {

	private static final String AC_GOTO  = "goto";
	private static final String AC_REGEN = "regen";
	private static final String AC_ARENA = "arena";

	{
		image = ItemSpriteSheet.SCROLL_KAUNAN;
		defaultAction = AC_GOTO;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_GOTO );
		actions.add( AC_REGEN );
		actions.add( AC_ARENA );
		return actions;
	}

	@Override
	public String name() {
		return "楼层跳转器";
	}

	@Override
	public String desc() {
		return "跳转到任意分支/楼层进行测试，也可以直接进入一张空白的测试场。\n\n"
				+ "跳转：直接输入层数就停在本分支，输入「分支ID 层数」可跨分支，例如 main 8、moss 2。\n"
				+ "测试场：一间没有怪物、没有掉落的空房，用来安全地试武器和技能。";
	}

	@Override
	public String actionName( String action, Hero hero ) {
		switch (action) {
			case AC_GOTO:  return "跳转";
			case AC_REGEN: return "重生成当前层";
			case AC_ARENA: return "测试场";
			default:       return super.actionName( action, hero );
		}
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if (action.equals( AC_GOTO )) {
			GameScene.show( new WndTextInput(
					"跳转到楼层",
					"直接输入层数（停留在当前分支），例如：8\n"
							+ "也可以输入「分支 层数」跨分支跳转，例如：main 8 / moss 2\n"
							+ "当前分支：" + Dungeon.branchId
							+ "\n可用分支：" + branchList(),
					String.valueOf( Dungeon.depth ),
					32, false, "前往", "取消" ) {
				@Override
				public void onSelect( boolean positive, String text ) {
					if (positive) {
						parseAndGo( text );
					}
				}
			} );
		} else if (action.equals( AC_REGEN )) {
			InterlevelScene.mode = InterlevelScene.Mode.RESET;
			Game.switchScene( InterlevelScene.class );
		} else if (action.equals( AC_ARENA )) {
			// 生成一张空白测试场（Dungeon.newLevel 会读取该标记）
			TestArenaLevel.requested = true;
			InterlevelScene.mode = InterlevelScene.Mode.RESET;
			Game.switchScene( InterlevelScene.class );
		}
	}

	private static String branchList() {
		StringBuilder sb = new StringBuilder();
		for (String id : Branches.getAllIds()) {
			if (sb.length() > 0) sb.append( ", " );
			sb.append( id );
		}
		return sb.toString();
	}

	private void parseAndGo( String text ) {
		if (text == null || text.trim().isEmpty()) {
			return;
		}
		String[] parts = text.trim().split( "[\\s,，:]+" );
		try {
			// 只输入数字：停留在当前分支，跳到该层
			if (parts.length == 1 && parts[0].matches( "\\d+" )) {
				goTo( Dungeon.branchId, Integer.parseInt( parts[0] ) );
				return;
			}
			String branchId = parts[0];
			int depth = parts.length > 1 ? Integer.parseInt( parts[1] ) : 1;
			goTo( branchId, depth );
		} catch (Exception e) {
			GLog.n( "格式错误，应为：层数 或 分支ID 层数" );
		}
	}

	private void goTo( String branchId, int depth ) {
		if (!Branches.exists( branchId )) {
			GLog.n( "未知分支：" + branchId );
			return;
		}
		Branch branch = Branches.get( branchId );
		if (depth < 1 || depth > branch.maxDepth) {
			GLog.n( "层数超出范围（1~" + branch.maxDepth + "）：" + depth );
			return;
		}

		TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff( TimekeepersHourglass.timeFreeze.class );
		if (timeFreeze != null) timeFreeze.disarmPresses();
		Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff( Swiftthistle.TimeBubble.class );
		if (timeBubble != null) timeBubble.disarmPresses();

		InterlevelScene.mode = InterlevelScene.Mode.RETURN;
		InterlevelScene.returnBranchId = branchId;
		InterlevelScene.returnDepth = depth;
		InterlevelScene.returnPos = -1;
		Game.switchScene( InterlevelScene.class );
	}
}
