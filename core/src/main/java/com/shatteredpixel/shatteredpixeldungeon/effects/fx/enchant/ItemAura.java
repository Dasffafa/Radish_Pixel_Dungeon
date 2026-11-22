package com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;

/** 附魔物品的光环：地上的附魔装备会冒同色小点，角色持附魔武器时脚边也有。 */
public final class ItemAura {

	private ItemAura(){}

	private static final float SIZE = DungeonTilemap.SIZE;
	private static final float GAP = 0.45f;
	private static float in;
	public static int spawned;

	public static void reset(){
		in = 0f;
	}

	public static void update( float dt ){
		if (dt <= 0f || Dungeon.level == null) return;
		if ((in -= dt) > 0f) return;
		in = GAP;

		FxEmitter air = Fx.air();
		if (air == null) return;

		// 地上的附魔物品
		int shown = 0;
		for (Heap heap : Dungeon.level.heaps.valueList()){
			if (shown >= 6) break;
			if (!heap.seen || !Dungeon.level.heroFOV[heap.pos]) continue;
			Item it = heap.peek();
			if (!(it instanceof Weapon)) continue;
			Weapon.Enchantment e = ((Weapon) it).enchantment;
			if (e == null) continue;
			float x = (heap.pos % Dungeon.level.width()) * SIZE + FxRandom.Float( 2f, 14f );
			float y = (heap.pos / Dungeon.level.width()) * SIZE + FxRandom.Float( 2f, 14f );
			if (!FxBudget.keep( FxBudget.P3, x, y )) continue;
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( x, y )
					.look( FxFrames.STAR4_5 )
					.paint( EnchantFx.colorOf( e ) )
					.vel( 0, -6f )
					.life( FxRandom.Float( 0.5f, 0.9f ) )
					.alpha( 0.7f, 0f, 0.3f )
					.priority( FxBudget.P3 );
			spawned++;
			shown++;
		}

		// 角色手持附魔武器
		if (Dungeon.hero != null && Dungeon.hero.sprite != null
				&& Dungeon.hero.belongings.weapon() instanceof Weapon){
			Weapon.Enchantment e = ((Weapon) Dungeon.hero.belongings.weapon()).enchantment;
			if (e != null){
				float x = Dungeon.hero.sprite.x + FxRandom.Float( 0f, Dungeon.hero.sprite.width() );
				float y = Dungeon.hero.sprite.y + Dungeon.hero.sprite.height() - FxRandom.Float( 0f, 4f );
				if (FxBudget.keep( FxBudget.P3, x, y )){
					((FxParticle) air.recycle( FxParticle.class ))
							.reset( x, y )
							.look( FxFrames.MOTE_1 )
							.paint( EnchantFx.colorOf( e ) )
							.vel( FxRandom.Float( -4f, 4f ), -10f )
							.life( FxRandom.Float( 0.4f, 0.8f ) )
							.alpha( 0.6f, 0f, 0.3f )
							.priority( FxBudget.P3 );
					spawned++;
				}
			}
		}
	}
}
