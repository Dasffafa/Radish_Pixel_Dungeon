package com.shatteredpixel.shatteredpixeldungeon.damage;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

/** Single entry point for damage application during the damage-system migration. */
public final class DamagePipeline {
	private DamagePipeline() {}
	private static final ThreadLocal<DamageInfo> ACTIVE = new ThreadLocal<>();

	public static DamageInfo activeInfo() { return ACTIVE.get(); }

	public static DamageResult apply(Char target, DamageInfo info) {
		if (target == null) throw new IllegalArgumentException("target cannot be null");
		if (info == null) throw new IllegalArgumentException("info cannot be null");
		int before = Math.max(0, target.HP);
		int shieldBefore = target.shielding();
		int modified = info.getDamage();
		DamageInfo previous = ACTIVE.get();
		ACTIVE.set(info);
		try {
			target.applyDamageLegacy(info);
		} finally {
			if (previous == null) {
				ACTIVE.remove();
			} else {
				ACTIVE.set(previous);
			}
		}
		int hpDamage = Math.max(0, before - Math.max(0, target.HP));
		int shieldBlocked = Math.max(0, shieldBefore - target.shielding());
		return new DamageResult(info.getBaseDamage(), modified, shieldBlocked, hpDamage, modified == 0 && hpDamage == 0);
	}
}
