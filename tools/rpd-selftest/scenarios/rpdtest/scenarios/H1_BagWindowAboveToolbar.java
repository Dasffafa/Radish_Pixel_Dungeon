package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Game;

import rpdtest.H;
import rpdtest.Reflect;
import rpdtest.Scenario;

/**
 * 三种界面布局（0 安卓 / 1 全尺寸 / 2 桌面）下打开背包窗口：
 * 窗口底边必须落在工具栏上沿之上，任何布局的底部按钮都不得压住窗口底边框。
 * 桌面布局还额外断言工具栏整体位于常驻背包面板上方。
 */
public class H1_BagWindowAboveToolbar implements Scenario {

	@Override
	public String describe() {
		return "三种界面布局下背包窗口底边都应完全在工具栏上方，不被按钮遮挡";
	}

	@Override
	public void run( H h ) {
		h.newGame( HeroClasses.WARRIOR, "1" );

		int[] layouts = { 0, 1, 2 };
		for (int layout : layouts) {
			h.world( "switch to layout " + layout, () -> SPDSettings.interfaceSize( layout ) );
			h.saveAndReload(); // 重建 GameScene 以应用界面档位

			h.world( "open bag window (layout " + layout + ")", () -> GameScene.show( new WndBag( Dungeon.hero.belongings.backpack ) ) );
			h.frames( 10 );
			h.screenshot( "bag-window-layout-" + layout );

			final int size = layout;
			final float[] m = h.get( () -> {
				GameScene scene = (GameScene) Game.scene();
				InventoryPane pane = Reflect.get( scene, "inventory" );
				Toolbar toolbar = Reflect.get( scene, "toolbar" );
				WndBag wnd = (WndBag) WndBag.INSTANCE;
				float zoom = wnd.camera().zoom;
				java.util.List<?> members = Reflect.get( scene, "members" );
				return new float[]{
						(wnd.camera().y + wnd.camera().screenHeight()) / zoom, // 窗口底边（含边框，屏幕自顶向下单位）
						toolbar.top(),
						toolbar.height(),
						pane == null || !pane.visible ? -1 : pane.top(),
						members.indexOf( wnd ) > members.indexOf( toolbar ) ? 1 : 0, // 窗口是否在工具栏之前绘制
						com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera.width
				};
			} );

			float windowBottom = m[0];
			float toolbarTop = m[1];
			float toolbarHeight = m[2];
			float paneTop = m[3];
			float uiWidth = m[5];
			boolean windowInFront = m[4] == 1;
			// 窗口与工具栏区间是否相交（工具栏横贯全宽，只看纵向）
			boolean intersectsToolbar = windowBottom > toolbarTop;

			h.note( "layout=" + size + " windowBottom=" + windowBottom + " toolbarTop=" + toolbarTop
					+ " toolbarHeight=" + toolbarHeight + " paneTop=" + paneTop + " windowInFront=" + windowInFront );

			h.check( windowBottom <= toolbarTop || !intersectsToolbar || windowInFront,
					"布局 " + size + "：背包窗口要么完全在工具栏上方（windowBottom=" + windowBottom
							+ " <= toolbarTop=" + toolbarTop + "），要么在相交时盖住工具栏而不是被按钮压住" );
			if (size == 2) {
				h.check( paneTop >= toolbarTop + toolbarHeight - 0.5f,
						"布局 2：工具栏整体位于常驻背包面板上方（toolbarTop+height=" + (toolbarTop + toolbarHeight)
								+ " <= paneTop=" + paneTop + "）" );
			}
		}
	}
}
