package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.custom.breakball.BreakBallFlight;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.BreakBall;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BreakBallSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class BreakBallEntity extends Mob {

	private BreakBall sourceItem;
	private boolean brokenAfterFlight;

	{
		spriteClass = BreakBallSprite.Variant1.class;

		HP = HT = 20;
		defenseSkill = 0;
		EXP = 0;

		alignment = Alignment.NEUTRAL;
		state = PASSIVE;

		properties.add( Property.IMMOVABLE );
		properties.add( Property.INORGANIC );
	}

	public BreakBallEntity() {
		super();
	}

	public BreakBallEntity( BreakBall source ) {
		super();
		setSourceItem( source );
	}

	public void setSourceItem( BreakBall source ) {
		this.sourceItem = source;
		applySpriteVariant();
	}

	public BreakBall getSourceItem() {
		return sourceItem;
	}

	public float mass() {
		return sourceItem != null ? sourceItem.mass() : com.shatteredpixel.shatteredpixeldungeon.custom.breakball.BreakBallBallistics.REF_MASS;
	}

	// 按球体体积公式 V ∝ r³ 反推半径缩放：scale = (m / m0)^(1/3)
	public float visualScale() {
		return (float) Math.cbrt( mass() / com.shatteredpixel.shatteredpixeldungeon.custom.breakball.BreakBallBallistics.REF_MASS );
	}

	public float ballRadius() {
		return com.shatteredpixel.shatteredpixeldungeon.custom.breakball.BreakBallChargeOverlay.BALL_RADIUS * visualScale();
	}

	private void applySpriteVariant() {
		if ( sourceItem == null ) {
			spriteClass = BreakBallSprite.Variant1.class;
			return;
		}
		switch ( sourceItem.variant ) {
			case BreakBall.VARIANT_2:
				spriteClass = BreakBallSprite.Variant2.class;
				break;
			case BreakBall.VARIANT_EGG:
				spriteClass = BreakBallSprite.Egg.class;
				break;
			case BreakBall.VARIANT_1:
			default:
				spriteClass = BreakBallSprite.Variant1.class;
				break;
		}
	}

	public void consumeDurability() {
		if ( sourceItem != null && sourceItem.consumeLaunchDurability() ) {
			brokenAfterFlight = true;
		}
	}

	public boolean isBrokenAfterFlight() {
		return brokenAfterFlight;
	}

	public void shatter() {
		if ( pos >= 0 && pos < Dungeon.level.length() ) {
			CellEmitter.get( pos ).burst( Speck.factory( Speck.WOOL ), 8 );
		}
		Sample.INSTANCE.play( Assets.Sounds.SHATTER );
		destroy();
		if ( sprite != null ) {
			sprite.killAndErase();
		}
		Dungeon.level.mobs.remove( this );
	}

	public void collectToHero() {
		if ( !isAlive() ) return;

		CellEmitter.get( Dungeon.hero.pos ).burst( Speck.factory( Speck.WOOL ), 8 );
		Sample.INSTANCE.play( Assets.Sounds.ITEM );
		GLog.i( Messages.get( BreakBall.class, "recalled" ) );

		destroy();
		if ( sprite != null ) {
			sprite.killAndErase();
		}
		Dungeon.level.mobs.remove( this );

		if ( sourceItem != null ) {
			BreakBall item = sourceItem;
			sourceItem = null;
			if ( !item.collect( Dungeon.hero.belongings.backpack ) ) {
				Dungeon.level.drop( item, Dungeon.hero.pos ).sprite.drop();
			}
		}
	}

	@Override
	protected boolean act() {
		spend( TICK );
		return true;
	}

	// 英雄换位/点击交互改为收回背包
	@Override
	public boolean interact( Char c ) {
		if ( c == Dungeon.hero && !BreakBallFlight.isFlying() ) {
			collectToHero();
			Dungeon.hero.spendAndNext( Actor.TICK );
			return true;
		}
		return true;
	}

	// 英雄攻击改为收回背包
	@Override
	public void damage( DamageInfo info ) {
		if ( info != null && (info.getAttacker() == Dungeon.hero || info.getSource() == Dungeon.hero) ) {
			if ( !BreakBallFlight.isFlying() ) {
				collectToHero();
			}
			return;
		}
		super.damage( info );
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public void beckon( int cell ) {
	}

	@Override
	public float spawningWeight() {
		return 0f;
	}

	@Override
	public int damageRoll() {
		return 0;
	}

	@Override
	public int attackSkill( Char target ) {
		return 0;
	}

	private static final String SOURCE_ITEM = "source_item";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( SOURCE_ITEM, sourceItem );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		sourceItem = (BreakBall) bundle.get( SOURCE_ITEM );
		applySpriteVariant();
	}
}
