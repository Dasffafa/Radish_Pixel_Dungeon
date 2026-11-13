package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import org.jetbrains.annotations.NotNull;

/**
 * Wand.wandUsed() 结算一次施法、算施法耗时之前发布；time 分发后再交给天赋等后续修正。
 * 每次真正施放发布一次，订阅方可以在这里按施放次数扣耐久。
 */
public class WandZapTimeEvent extends GameEvent {

	private final Wand wand;
	private final Hero user;
	private float time;

	public WandZapTimeEvent(@NotNull Wand wand, @NotNull Hero user, float time) {
		this.wand = wand;
		this.user = user;
		this.time = time;
	}

	public @NotNull Wand getWand() { return wand; }

	public @NotNull Hero getUser() { return user; }

	public float getTime() { return time; }

	public void setTime(float time) { this.time = time; }
}
