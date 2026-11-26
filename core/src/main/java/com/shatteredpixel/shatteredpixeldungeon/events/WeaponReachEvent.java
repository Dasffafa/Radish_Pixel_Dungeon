package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import org.jetbrains.annotations.NotNull;

/**
 * Weapon.reachFactor() 算出攻击距离后发布；reach 分发后作为最终攻击距离返回。
 * 只读距离的地方（攻击判定、说明文字）都会走到，订阅方不能有副作用。
 */
public class WeaponReachEvent extends GameEvent {

	private final Weapon weapon;
	private final Char owner;
	private int reach;

	public WeaponReachEvent(@NotNull Weapon weapon, Char owner, int reach) {
		this.weapon = weapon;
		this.owner = owner;
		this.reach = reach;
	}

	public @NotNull Weapon getWeapon() { return weapon; }

	public Char getOwner() { return owner; }

	public int getReach() { return reach; }

	public void setReach(int reach) { this.reach = reach; }
}
