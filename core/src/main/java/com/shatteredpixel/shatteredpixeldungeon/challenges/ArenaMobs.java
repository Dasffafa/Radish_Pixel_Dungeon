package com.shatteredpixel.shatteredpixeldungeon.challenges;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Acidic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Albino;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RipperDemon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Slime;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Spinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Succubus;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

/**
 * 竞技场的波次出怪表：波次越高，怪越硬、越杂，但单波数量会被压住，
 * 免得手机端一口气刷 30 只卡成幻灯片。
 */
public class ArenaMobs {

	/** 第一梯队：一区小怪，用来暖手。 */
	private static final Class<?>[] TIER_1 = {
			Rat.class, Albino.class, Snake.class, Gnoll.class, Crab.class, Slime.class
	};

	/** 第二梯队：二区前后，开始有远程和自爆。 */
	private static final Class<?>[] TIER_2 = {
			Bat.class, Acidic.class, Thief.class, DM100.class, Skeleton.class, Swarm.class
	};

	/** 第三梯队：三区，肉盾与法系混编。 */
	private static final Class<?>[] TIER_3 = {
			Brute.class, Shaman.class, Guard.class, Necromancer.class, Spinner.class, Ghoul.class, Golem.class
	};

	/** 第四梯队：四区之后，专门给撑过十分钟的狠人... 好吧是三分钟。 */
	private static final Class<?>[] TIER_4 = {
			Monk.class, Warlock.class, Succubus.class, Elemental.class, Scorpio.class, RipperDemon.class
	};

	/** 第 n 波刷几只。 */
	public static int count(int wave) {
		return Math.min(2 + wave, 10);
	}

	public static void spawnWave(int wave) {
		int count = count(wave);
		for (int i = 0; i < count; i++) {
			spawnOne(wave);
		}
	}

	private static void spawnOne(int wave) {
		if (Dungeon.level == null || Dungeon.hero == null || !Dungeon.hero.isAlive()) {
			return;
		}
		Mob mob;
		try {
			mob = (Mob) Reflection.newInstance(pick(wave));
		} catch (Exception e) {
			return;
		}
		if (mob == null) {
			return;
		}

		// 血量随波次小幅成长，主要靠数量与种类变难，避免后期变成打木桩
		float scale = 1f + Math.min(1.2f, 0.08f * (wave - 1));
		mob.HT = Math.max(1, Math.round(mob.HT * scale));
		mob.HP = mob.HT;

		int cell = Dungeon.level.randomRespawnCell(mob);
		if (cell == -1) {
			cell = Dungeon.level.entrance();
		}

		mob.pos = cell;
		mob.state = mob.HUNTING;
		GameScene.add(mob);
		Dungeon.level.occupyCell(mob);
		mob.beckon(Dungeon.hero.pos);
	}

	private static Class<?> pick(int wave) {
		// 每 3 波进入下一梯队，最后一档之后一直停在第四梯队
		int tier = Math.min(4, 1 + (wave - 1) / 3);
		switch (tier) {
			case 1:  return TIER_1[Random.Int(TIER_1.length)];
			case 2:  return TIER_2[Random.Int(TIER_2.length)];
			case 3:  return TIER_3[Random.Int(TIER_3.length)];
			default: return TIER_4[Random.Int(TIER_4.length)];
		}
	}
}
