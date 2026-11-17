package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.input.PointerEvent;

/** 简单双摇杆：返回归一化方向，支持 360° 任意角度。 */
public class Joystick extends Group {

	private final Image base;
	private final Image knob;
	private final PointerArea area;

	private float nx, ny;
	private boolean active;
	private float size;

	public Joystick(float radius, int color) {
		size = radius * 2;
		base = circle(radius * 2, color & 0xFFFFFF, 0.28f);
		knob = circle(radius, 0xFFFFFF, 0.75f);
		add(base);
		add(knob);

		area = new PointerArea(0, 0, size, size) {
			@Override
			protected void onPointerDown(PointerEvent event) {
				active = true;
				updateStick(event);
			}

			@Override
			protected void onDrag(PointerEvent event) {
				if (active) updateStick(event);
			}

			@Override
			protected void onPointerUp(PointerEvent event) {
				active = false;
				nx = ny = 0;
				knob.setPos((size - knob.width()) / 2f, (size - knob.height()) / 2f);
			}
		};
		area.blockLevel = PointerArea.ALWAYS_BLOCK;
		add(area);
	}

	private void updateStick(PointerEvent event) {
		float dx = event.current.x - event.start.x;
		float dy = event.current.y - event.start.y;
		float max = size * 0.42f;
		float len = (float) Math.sqrt(dx * dx + dy * dy);
		if (len > max) {
			dx = dx / len * max;
			dy = dy / len * max;
		}
		nx = dx / max;
		ny = dy / max;
		knob.setPos((size - knob.width()) / 2f + dx, (size - knob.height()) / 2f + dy);
	}

	private Image circle(float diameter, int color, float alpha) {
		int d = Math.max(8, (int) diameter);
		Pixmap p = new Pixmap(d, d, Pixmap.Format.RGBA8888);
		p.setColor((color << 8) | 0xFF);
		p.fillCircle(d / 2, d / 2, d / 2 - 1);
		SmartTexture t = new SmartTexture(p, Texture.LINEAR, Texture.CLAMP, false);
		Image image = new Image(t);
		image.alpha(alpha);
		return image;
	}

	public void setBounds(float x, float y, float diameter) {
		size = diameter;
		base.scale.set(diameter / base.width(), diameter / base.height());
		knob.scale.set(diameter * 0.5f / knob.width(), diameter * 0.5f / knob.height());
		base.setPos(x, y);
		knob.setPos(x + diameter * 0.25f, y + diameter * 0.25f);
		area.x = x;
		area.y = y;
		area.width = diameter;
		area.height = diameter;
	}

	public float getXAxis() { return nx; }
	public float getYAxis() { return ny; }

	public boolean isActive() { return active; }
}
