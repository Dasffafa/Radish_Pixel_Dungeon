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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.RectF;

public class TalentIcon extends Image {

	private static TextureFilm film;
	private static final int SIZE = 16;

	public TalentIcon(Talent talent){
		this(talent.icon());
	}

	public TalentIcon(int icon){
		super( Assets.Interfaces.TALENT_ICONS );

		if (film == null) film = new TextureFilm(texture, SIZE, SIZE);

		RectF frame = film.get(icon);
		if (frame == null) {
			// 找不到图标时使用默认的"开发中"占位图标
			GLog.w("天赋图标 %d 未找到，使用开发中占位图标", icon);
			frame = film.get(0);
			// 如果连索引0都不存在，创建一个空的默认帧防止崩溃
			if (frame == null) {
				GLog.w("天赋图标资源加载失败，请检查 talent_icons.png");
				frame = new RectF(0f, 0f, 1f, 1f);
			}
		}
		frame(frame);
	}

}
