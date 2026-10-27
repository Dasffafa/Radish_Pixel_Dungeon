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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.utils.Callback;

/**
 * 游商对话窗口。
 *
 * 对照上游 _upstream/darkest .../windows/WndDialogue.kt 的布局：
 * 顶部图标标题栏 → 分隔线 → 正文问候语 → 底部一排动作按钮。
 *
 * 与上游的差别只在于语法层面（Kotlin lambda → Java 数组 + Callback），
 * 视觉结构保持一致，这样"对话界面"才是暗黑地牢那个样子而不是三个按钮的 WndOptions。
 */
public class WndDialogue extends Window {

	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_MAX = 220;
	private static final int GAP = 2;

	private static final int BTN_HEIGHT = 18;

	public WndDialogue( Image icon, String title, String message, String[] actions, Callback[] callbacks ) {

		super();

		IconTitle titlebar = new IconTitle( icon, title );
		titlebar.setRect( 0, 0, WIDTH_MAX, 0 );
		add( titlebar );

		RenderedTextBlock tfmessage = PixelScene.renderTextBlock( message, 6 );
		tfmessage.maxWidth( WIDTH_MAX );
		tfmessage.setPos( titlebar.left(), titlebar.bottom() + GAP );
		add( tfmessage );

		float pos = tfmessage.bottom() + GAP + 2;

		// 动作按钮竖排，宽度撑满，和上游 items.forEach 竖排一致
		int count = actions == null ? 0 : actions.length;
		for (int i = 0; i < count; i++) {
			final Callback cb = callbacks != null && i < callbacks.length ? callbacks[i] : null;
			final boolean hasCb = cb != null;

			RedButton btn = new RedButton( actions[i] ) {
				@Override
				protected void onClick() {
					hide();
					if (hasCb) cb.call();
				}
			};
			btn.setRect( 0, pos, WIDTH_MAX, BTN_HEIGHT );
			add( btn );
			pos += btn.height() + GAP;
		}

		// 至少给一个"算了"，不然没按钮用户出不去
		if (count == 0) {
			RedButton btn = new RedButton( "..." ) {
				@Override
				protected void onClick() {
					hide();
				}
			};
			btn.setRect( 0, pos, WIDTH_MAX, BTN_HEIGHT );
			add( btn );
			pos += btn.height() + GAP;
		}

		float width = WIDTH_MAX;
		if (titlebar.width() + 2 > width) width = titlebar.width() + 2;
		if (tfmessage.width() + 2 > width) width = tfmessage.width() + 2;
		if (width < WIDTH_MIN) width = WIDTH_MIN;

		resize( (int) width, (int) (pos - GAP) );
	}
}
