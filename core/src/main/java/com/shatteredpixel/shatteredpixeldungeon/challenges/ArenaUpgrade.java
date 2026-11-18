package com.shatteredpixel.shatteredpixeldungeon.challenges;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * 每清一波怪给的三选一强化。
 *
 * <p>数值类强化可以无限叠加；功能类（顺劈、处决）拿到即锁定，
 * 之后再抽到就会被排除，保证每次三选一都是真的「有得选」。</p>
 */
public enum ArenaUpgrade {

	FURY,       // 狂怒：伤害 +30%
	TITAN,      // 巨人：最大生命 +15% 并回满
	VAMP,       // 吸血：吸血 +8%
	SWIFT,      // 疾风：攻击速度 +12%
	REAP,       // 收割：击杀回复最大生命 5%
	CLEAVE,     // 顺劈：击杀时溅射周围 2 格
	SANDS,      // 时之沙：时间 +15 秒
	EXECUTE;    // 处决：对 35% 生命以下的敌人双倍伤害

	private String key() {
		return name().toLowerCase(Locale.ROOT);
	}

	public String title() {
		return M.L(ArenaUpgrade.class, key() + "_name");
	}

	public String desc() {
		return M.L(ArenaUpgrade.class, key() + "_desc");
	}

	/** 功能类强化只能拿一次。 */
	private boolean oneTime() {
		return this == CLEAVE || this == EXECUTE;
	}

	public void apply(Hero hero) {
		if (hero == null) {
			return;
		}
		ArenaEmpower empower = ArenaRun.empower();
		if (empower == null) {
			return;
		}
		switch (this) {
			case FURY:
				empower.damage += 0.30f;
				break;
			case TITAN: {
				int add = Math.max(8, Math.round(hero.HT * 0.15f));
				hero.HT += add;
				hero.HP = hero.HT;
				break;
			}
			case VAMP:
				empower.lifesteal += 0.08f;
				break;
			case SWIFT:
				empower.attackSpeed += 0.12f;
				break;
			case REAP:
				empower.killHealPct += 5;
				break;
			case CLEAVE:
				empower.cleave = true;
				break;
			case SANDS:
				ArenaRun.addTime(15);
				break;
			case EXECUTE:
				empower.execute = true;
				break;
		}
	}

	/** 抽三个不重复的强化。 */
	public static ArenaUpgrade[] roll() {
		ArenaEmpower empower = ArenaRun.empower();
		ArrayList<ArenaUpgrade> pool = new ArrayList<>(Arrays.asList(values()));
		if (empower != null) {
			if (empower.cleave) pool.remove(CLEAVE);
			if (empower.execute) pool.remove(EXECUTE);
		}
		ArenaUpgrade[] picked = new ArenaUpgrade[3];
		for (int i = 0; i < picked.length; i++) {
			if (pool.isEmpty()) {
				// 池子被抽干的极端情况（正常不会发生），补一个可叠加的
				picked[i] = FURY;
				continue;
			}
			int index = Random.Int(pool.size());
			ArenaUpgrade upgrade = pool.remove(index);
			picked[i] = upgrade;
		}
		// 三选一里若出现重复的可叠加项，观感太差，重抽一次兜底
		if (picked[0] == picked[1] || picked[1] == picked[2] || picked[0] == picked[2]) {
			return roll();
		}
		return picked;
	}
}
