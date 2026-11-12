/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.DimensionalRipper;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Visual;
import com.watabou.utils.Callback;

/**
 * 空间裂隙窗口：次元撕裂者自己造成传送后，3 回合内可瞬移到被攻击的敌人身旁并追击一次。
 *
 * 按文档规格：按回合记账（act 里 spend(TICK)）、按钮带剩余回合数、
 * 落点用 appear（无视距离）、必中追击、出入场都有裂隙粒子、武器离手/换层/目标死亡即关窗。
 * 锁定的目标是 Char 引用，刻意不实现存档，读档后首次 act 自行关闭。
 */
public class RiftWindow extends Buff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	public static final int WINDOW_TURNS = 3;

	private Char target;
	private int openedDepth = -1;
	private int turnsLeft = WINDOW_TURNS;
	// 开窗后的第一次结算只用来对齐回合，不计入 WINDOW_TURNS，原因见 act()
	private boolean fresh = false;

	/** 打开（或刷新）窗口，锁定这次被攻击的敌人。 */
	public void open( Char target ){
		this.target = target;
		this.openedDepth = Dungeon.depth;
		this.turnsLeft = WINDOW_TURNS;
		this.fresh = true;
		// 刷新已有窗口时把下一次结算拉回当前时刻，和新开的窗口一样从这一拍重新数
		timeToNow();
		ActionIndicator.setAction( this );
		BuffIndicator.refreshHero();
		GLog.p( Messages.get( DimensionalRipper.class, "window_open" ) );
	}

	private boolean stillValid(){
		Hero hero = Dungeon.hero;
		return hero != null
				&& target != null && target.isAlive()
				&& Dungeon.depth == openedDepth
				&& DimensionalRipper.equipped( hero ) != null;
	}

	@Override
	public boolean attachTo( Char target ) {
		// affect() 发生在 open() 之前，此刻还没锁定目标；这里只把「武器仍在手上」的关。
		return super.attachTo( target ) && DimensionalRipper.equipped( Dungeon.hero ) != null;
	}

	@Override
	public String icon() {
		return BuffIndicator.LEVITATION;
	}

	@Override
	public String actionName() {
		return Messages.get( DimensionalRipper.class, "space_tear" );
	}

	/** 借用现成的传送信标帧，不必为按钮单独画图。 */
	@Override
	public String actionIcon() {
		return HeroIcon.WARP_BEACON;
	}

	/** 按钮右下角显示剩余回合数。 */
	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText( PixelScene.pixelFont );
		txt.text( Integer.toString( Math.max( 0, turnsLeft ) ) );
		txt.hardlight( CharSprite.POSITIVE );
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return 0xAA55FF;
	}

	@Override
	public void doAction() {
		Hero hero = Dungeon.hero;
		if (!stillValid()){
			detach();
			return;
		}
		int cell = freeCellNear( target.pos );
		if (cell < 0){
			GLog.w( Messages.get( DimensionalRipper.class, "no_space" ) );
			return;
		}

		hero.busy();
		int from = hero.pos;
		ScrollOfTeleportation.appear( hero, cell );
		// 出入场粒子：appear 只管非英雄角色的起点光点，英雄传送两端都得自己补。
		DimensionalRipper.teleportFx( from, hero.pos );
		// appear 只挪位置和贴图，不刷新视野；和原版传送信标一样由调用方补上，
		// 否则这一拍用的还是落点之前的视野
		Dungeon.observe();
		GameScene.updateFog();
		hero.checkVisibleMobs();
		AttackIndicator.target( target );

		final Char victim = target;
		hero.sprite.attack( victim.pos, new Callback() {
			@Override
			public void call() {
				if (victim.isAlive()){
					hero.attack( victim, 1f, 0f, Char.INFINITE_ACCURACY );
				}
				hero.spendAndNext( hero.attackDelay() );
			}
		});
		detach();
	}

	private int freeCellNear( int pos ){
		int w = Dungeon.level.width();
		int[] dirs = { -w-1, -w, -w+1, -1, 1, w-1, w, w+1 };
		for (int d : dirs){
			int c = pos + d;
			if (c >= 0 && c < Dungeon.level.passable.length
					&& Dungeon.level.passable[c] && Actor.findChar(c) == null){
				return c;
			}
		}
		return -1;
	}

	@Override
	public boolean act() {
		// 刚挂上的 buff 会在当前时刻立刻结算一次（Actor.add 把它排在 now），
		// 这一拍在英雄下一次行动之前。它如果也扣回合，写着 3 回合的窗口实际只有 2 回合可用，
		// 按钮上也永远看不到 3。
		if (fresh){
			fresh = false;
		} else {
			turnsLeft--;
		}
		if (!stillValid() || turnsLeft <= 0){
			detach();
			return true;
		}
		// 每回合重占动作槽，避免被其它动作按钮顶掉后按钮消失。
		ActionIndicator.setAction( this );
		spend( TICK );
		return true;
	}

	@Override
	public void detach() {
		ActionIndicator.clearAction( this );
		target = null;
		super.detach();
	}

	@Override
	public String toString() {
		return Messages.get( this, "name" );
	}

	@Override
	public String desc() {
		return Messages.get( this, "desc" );
	}
}
