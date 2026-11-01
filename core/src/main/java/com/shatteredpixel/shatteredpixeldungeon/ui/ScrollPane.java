/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.input.ScrollEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.ScrollArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.GameMath;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;
import com.watabou.utils.Signal;

public class ScrollPane extends Component {

	protected static final int THUMB_COLOR		= 0xFF7b8073;
	protected static final float THUMB_ALPHA	= 0.5f;

	protected PointerController controller;
	protected Signal.Listener<KeyEvent> keyListener;
	protected Component content;
	protected ColorBlock thumb;

	/** 滚动条在无操作后自动隐藏。 */
	private float thumbTimer = 0f;
	private static final float THUMB_HOLD = 1.2f;

	private float keyScroll = 0;

	public ScrollPane( Component content ) {
		super();

		this.content = content;
		addToBack( content );

		width = content.width();
		height = content.height();

		content.camera = new Camera( 0, 0, 1, 1, PixelScene.defaultZoom );
		Camera.add( content.camera );

		KeyEvent.addKeyListener(keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent keyEvent) {
				GameAction action = KeyBindings.getActionForKey(keyEvent);
				if (action == SPDAction.ZOOM_IN){
					if (keyEvent.pressed){
						keyScroll += 1;
					} else {
						keyScroll -= 1;
					}
					keyScroll = GameMath.gate(-1f, keyScroll, +1f);
					return true;
				} else if (action == SPDAction.ZOOM_OUT){
					if (keyEvent.pressed){
						keyScroll -= 1;
					} else {
						keyScroll += 1;
					}
					keyScroll = GameMath.gate(-1f, keyScroll, +1f);
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void destroy() {
		super.destroy();
		Camera.remove( content.camera );
		KeyEvent.removeKeyListener(keyListener);
	}

	public void scrollTo( float x, float y ) {
		Camera c = content.camera;
		c.scroll.set( x, y );
		clampScroll();
	}

	/** 相对滚动：滚轮、拖动、缩略条都用这一条路径，保证边界与缩略条位置永远一致。 */
	public void scrollBy( float dx, float dy ) {
		Camera c = content.camera;
		c.scroll.x += dx;
		c.scroll.y += dy;
		clampScroll();
	}

	public void setScrollY( float y ) {
		content.camera.scroll.y = y;
		clampScroll();
	}

	private void clampScroll() {
		Camera c = content.camera;
		float maxX = Math.max( 0, content.width()  - width );
		float maxY = Math.max( 0, content.height() - height );
		c.scroll.x = GameMath.gate( 0, c.scroll.x, maxX );
		c.scroll.y = GameMath.gate( 0, c.scroll.y, maxY );
		updateThumb();
	}

	/**
	 * 缩略条是 ScrollPane 的子节点，坐标相对 ScrollPane 自身（从 0 到 height），
	 * 所以这里不能再叠加 this.y —— 之前叠了一次，滚到底时缩略条正好被推出窗口下沿。
	 */
	protected void updateThumb() {
		if (thumb == null || content == null) return;

		boolean show = height < content.height();
		thumb.visible = show;
		if (!show) return;

		float track = Math.max( 1, height );
		float thumbH = Math.min( track, Math.max( 12, track * track / content.height() ) );
		thumb.scale.set( 3, thumbH );
		thumb.x = width - thumb.width() - 1;

		float maxScroll = Math.max( 1, content.height() - height );
		float maxThumbY = Math.max( 0, track - thumbH );
		thumb.y = maxThumbY * ( content.camera.scroll.y / maxScroll );
	}

	@Override
	public synchronized void update() {
		super.update();

		// 窗口刚创建时 camera() 还是 null，layout() 会直接返回，导致视口尺寸/相机没被摆好，
		// 之后又没人再排一次 —— 表现就是“没有滚动条、也滑不动”。
		// 这里每帧核对一次：视口尺寸或内容相机位置跟窗口对不上就重排。
		if (content != null && content.camera != null && camera() != null) {
			Point p = camera().cameraToScreen( x, y );
			if (content.camera.width != (int)width || content.camera.height != (int)height
					|| content.camera.x != p.x || content.camera.y != p.y) {
				layout();
			}
		}

		if (keyScroll != 0){
			scrollTo(content.camera.scroll.x, content.camera.scroll.y + (keyScroll * 150 * Game.elapsed));
		}
		if (thumbTimer > 0){
			thumbTimer -= Game.elapsed;
			if (thumbTimer <= 0){
				thumbTimer = 0;
				thumb.am = 0;
			} else {
				thumb.am = THUMB_ALPHA * Math.min(1f, thumbTimer / 0.3f);
			}
		}
	}

	/** 显示滚动条，并重置自动隐藏计时。 */
	protected void showThumb(){
		thumbTimer = THUMB_HOLD;
		if (thumb != null){
			thumb.visible = height < content.height();
			thumb.am = THUMB_ALPHA;
		}
	}

	@Override
	protected void createChildren() {
		controller = new PointerController();
		add( controller );

		thumb = new ColorBlock( 1, 1, THUMB_COLOR );
		thumb.am = 0;
		add( thumb );
	}

	@Override
	protected void layout() {

		// 窗口尚未挂到场景时 camera() 为 null，调用 cameraToScreen 会 NPE
		if (camera() == null) {
			return;
		}

		content.setPos( 0, 0 );
		// 控制器是 ScrollPane 自己的子节点，坐标相对 ScrollPane 自身（0,0）。
		// 原先写的 x/y 是“相对父窗口”的坐标，ScrollPane 一旦不放在 (0,0)（比如背包的 y=TITLE_HEIGHT），
		// 控制器就会整体偏移到内容区之外，手指在格子上拖动永远收不到事件、滚动失效。
		controller.x = 0;
		controller.y = 0;
		controller.width = width;
		controller.height = height;

		Point p = camera().cameraToScreen( x, y );
		Camera cs = content.camera;
		cs.x = p.x;
		cs.y = p.y;
		cs.resize( (int)width, (int)height );

		thumb.visible = height < content.height();
		if (thumb.visible) {
			updateThumb();
			if (thumbTimer <= 0) thumb.am = 0;
		}
	}

	public Component content() {
		return content;
	}

	public void onClick( float x, float y ) {
	}

	public class PointerController extends ScrollArea {

		private float dragThreshold;

		public PointerController() {
			super( 0, 0, 0, 0 );
			dragThreshold = PixelScene.defaultZoom * 8;
			// 不阻挡点击：内容里的按钮/物品格必须能收到 DOWN/UP
			blockLevel = NEVER_BLOCK;
		}

		@Override
		protected void onScroll(ScrollEvent event) {
			// 滚轮直接按当前滚动量相对移动。之前是拿 lastPos（只有拖动时才更新）当参照，
			// 桌面端纯滚轮时 lastPos 还是 (0,0)，一滚就整页跳走。
			if (event.amount != 0) {
				scrollBy( 0, -event.amount * Math.max( 1, height ) * 0.5f );
				showThumb();
			}
		}

		@Override
		protected void onPointerDown( PointerEvent event ) {
			// 按住缩略条 = 直接拖滚动条（桌面端和手机都适用）
			if (thumb != null && thumb.visible) {
				PointF p = localPoint( event.current );
				if (p.x >= thumb.x - 2 && p.x <= thumb.x + thumb.width() + 2
						&& p.y >= thumb.y - 2 && p.y <= thumb.y + thumb.height() + 2) {
					thumbDragging = true;
					thumbStartY = p.y;
					thumbScrollStart = content.camera.scroll.y;
					showThumb();
					return;
				}
			}
			lastPos.set( event.current );
		}

		@Override
		protected void onPointerUp( PointerEvent event ) {
			if (thumbDragging) {
				thumbDragging = false;
				showThumb();
				return;
			}

			if (dragging) {

				dragging = false;
				showThumb();

			} else {

				PointF p = content.camera.screenToCamera( (int) event.current.x, (int) event.current.y );
				ScrollPane.this.onClick( p.x, p.y );

			}
		}

		private boolean dragging = false;
		private boolean thumbDragging = false;
		private float thumbStartY = 0;
		private float thumbScrollStart = 0;
		private PointF lastPos = new PointF();

		private PointF localPoint( PointF screen ){
			PointF p = camera().screenToCamera( (int)screen.x, (int)screen.y );
			return new PointF( p.x - x, p.y - y );
		}

		@Override
		protected void onDrag( PointerEvent event ) {

			if (thumbDragging) {
				float track = Math.max( 1, height );
				float thumbH = thumb.height();
				float maxScroll = Math.max( 1, content.height() - height );
				float maxThumbY = Math.max( 1, track - thumbH );
				float dy = localPoint( event.current ).y - thumbStartY;
				setScrollY( thumbScrollStart + dy * maxScroll / maxThumbY );
				showThumb();
				return;
			}

			if (dragging) {

				scroll(event.current);
				showThumb();

			} else if (PointF.distance( event.current, event.start ) > dragThreshold) {

				dragging = true;
				lastPos.set( event.current );
				thumb.am = 1;
				showThumb();

			}
		}

		private void scroll( PointF current ){

			PointF diff = PointF.diff( lastPos, current ).invScale( content.camera.zoom );
			//拖动过程中必须每帧更新参照点，否则每次都拿"按下那一瞬间"的位置去算位移，
			//手指一动位移就成倍累积，内容会直接闪到另一头
			lastPos.set( current );
			scrollBy( diff.x, diff.y );
		}
		
	}
}
