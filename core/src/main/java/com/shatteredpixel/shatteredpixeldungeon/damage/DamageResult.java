package com.shatteredpixel.shatteredpixeldungeon.damage;

/** Immutable summary of one damage pipeline application. */
public final class DamageResult {
	public final int baseDamage;
	public final int modifiedDamage;
	public final int shieldBlocked;
	public final int hpDamage;
	public final boolean immune;

	public DamageResult(int baseDamage, int modifiedDamage, int hpDamage, boolean immune) {
		this(baseDamage, modifiedDamage, 0, hpDamage, immune);
	}

	public DamageResult(int baseDamage, int modifiedDamage, int shieldBlocked, int hpDamage, boolean immune) {
		this.baseDamage = baseDamage;
		this.modifiedDamage = modifiedDamage;
		this.shieldBlocked = shieldBlocked;
		this.hpDamage = hpDamage;
		this.immune = immune;
	}
}
