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

package com.watabou.noosa;

import com.watabou.input.PointerEvent;
import com.watabou.utils.Signal;

public class PointerArea extends Visual implements Signal.Listener<PointerEvent> {
	
	// Its target can be pointerarea itself
	public Visual target;
	
	protected PointerEvent curEvent = null;
	protected boolean hovered = false;

	//按下点（屏幕坐标），用于区分「点击」和「拖动」
	private float downX = 0, downY = 0;

	public int blockLevel = BLOCK_WHEN_ACTIVE;
	public static final int ALWAYS_BLOCK = 0;       //Always block input to overlapping elements
	public static final int BLOCK_WHEN_ACTIVE = 1;  //Only block when active (default)
	public static final int NEVER_BLOCK = 2;        //Never block (handy for buttons in scroll areas)
	
	public PointerArea( Visual target ) {
		super( 0, 0, 0, 0 );
		this.target = target;
		
		PointerEvent.addPointerListener( this );
	}
	
	public PointerArea( float x, float y, float width, float height ) {
		super( x, y, width, height );
		this.target = this;
		
		visible = false;
		
		PointerEvent.addPointerListener( this );
	}
	
	@Override
	public boolean onSignal( PointerEvent event ) {

		boolean hit = event != null && target.overlapsScreenPoint( (int)event.current.x, (int)event.current.y );
		
		if (!isActive()) {
			return (hit && blockLevel == ALWAYS_BLOCK);
		}
		
		if (hit) {
			
			boolean returnValue = (event.type == PointerEvent.Type.DOWN || event == curEvent);
			
			if (event.type == PointerEvent.Type.DOWN) {
				
				if (curEvent == null) {
					curEvent = event;
					//记下按下点：抬起时如果已经移动了一段距离，说明是在拖动（滚动列表/背包），
					//不当成点击，否则滑动物品格会误触发长按/打开/关闭等操作
					downX = event.current.x;
					downY = event.current.y;
				}
				onPointerDown( event );
				
			} else if (event.type == PointerEvent.Type.UP) {
				
				onPointerUp( event );
				
				if (curEvent == event) {
					curEvent = null;
					//拖动阈值按相机缩放换算：16 个逻辑像素，缩放越大对应的屏幕像素越多，
					//这样高 DPI / 放大状态下“手抖”也不会被误判成点击。
					float dragLimit = 16f * (Camera.main != null ? Math.max( 1f, Camera.main.zoom ) : 1f);
					boolean dragged = Math.abs( event.current.x - downX ) > dragLimit
							|| Math.abs( event.current.y - downY ) > dragLimit;
					if (!dragged) {
						onClick( event );
					}
				}
				
			} else if (event.type == PointerEvent.Type.HOVER) {
				if (event.handled && hovered){
					hovered = false;
					onHoverEnd(event);
				} else if (!event.handled && !hovered){
					hovered = true;
					onHoverStart(event);
				}
				event.handle();
			}
			
			return returnValue && blockLevel != NEVER_BLOCK;
			
		} else {
			
			if (event == null && curEvent != null) {
				onDrag(curEvent);

			} else if (curEvent != null && event.type == PointerEvent.Type.UP) {
				onPointerUp( event );
				curEvent = null;

			} else if (event != null && event.type == PointerEvent.Type.HOVER && hovered){
				hovered = false;
				onHoverEnd(event);
			}
			
			return false;
			
		}
	}
	
	protected void onPointerDown( PointerEvent event ) { }
	
	protected void onPointerUp( PointerEvent event) { }
	
	protected void onClick( PointerEvent event ) { }
	
	protected void onDrag( PointerEvent event ) { }

	protected void onHoverStart( PointerEvent event ) { }

	protected void onHoverEnd( PointerEvent event ) { }
	
	public void reset() {
		curEvent = null;
	}

	//moves this pointer area to the front of the pointer event order
	public void givePointerPriority(){
		PointerEvent.removePointerListener( this );
		PointerEvent.addPointerListener( this );
	}
	
	@Override
	public void destroy() {
		PointerEvent.removePointerListener( this );
		super.destroy();
	}
}
