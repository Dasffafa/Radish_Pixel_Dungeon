/*
 * Radish Pixel Dungeon
 * Copyright (C) 2026
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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

/**
 * 游商精灵。
 *
 * 素材来自桌面 ai 文件夹那张 256x512 的 NPC 图集，按行拆成 7 张独立贴图
 * （npc_peddler0~6.png，每张 176x16 = 11 帧 x 16x16）。
 * 每个游商只取自己那一行的 idle 帧，走位/攻击直接复用 idle，
 * 因为他们站着不动、也不会打人。
 */
public class PeddlerSprite extends MobSprite {

	/** 贴图表：下标即 Peddler 的 variant。 */
	private static final String[] SHEETS = {
			Assets.Sprites.NPC_PEDDLER0,
			Assets.Sprites.NPC_PEDDLER1,
			Assets.Sprites.NPC_PEDDLER2,
			Assets.Sprites.NPC_PEDDLER3,
			Assets.Sprites.NPC_PEDDLER4,
			Assets.Sprites.NPC_PEDDLER5,
			Assets.Sprites.NPC_PEDDLER6,
	};

	/** 每行有效帧数（原图每行 11 格，个别行尾部是空的，多填几帧无害）。 */
	private static final int FRAMES = 11;

	public PeddlerSprite(int variant) {
		super();

		if (variant < 0 || variant >= SHEETS.length) {
			variant = 0;
		}
		texture(SHEETS[variant]);

		TextureFilm film = new TextureFilm(texture, 16, 16);

		idle = new Animation(6, true);
		idle.frames(film, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

		run = idle.clone();
		attack = idle.clone();
		die = idle.clone();

		idle();
	}

	/** 供 catalog 出错误时的兜底，避免一异常就整张图消失。 */
	public PeddlerSprite() {
		this(0);
	}
}
