package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;

/** 摇杆视觉；输入由 RealtimeController 统一处理。 */
public class Joystick extends Group {

	private final Image base;
	private final Image knob;
	private float nx, ny;
	private float size;

	public Joystick(float radius, int color) {
		size = radius * 2;
		base = circle(radius * 2, color & 0xFFFFFF, 0.28f);
		knob = circle(radius, 0xFFFFFF, 0.75f);
		add(base);
		add(knob);
	}

	public void setBounds(float x, float y, float diameter) {
		size = diameter;
		base.scale.set(diameter / base.width(), diameter / base.height());
		knob.scale.set(diameter * 0.5f / knob.width(), diameter * 0.5f / knob.height());
		base.setPos(x, y);
		knob.setPos(x + diameter * 0.25f, y + diameter * 0.25f);
	}

	public void setAxes(float nx, float ny) {
		this.nx = nx;
		this.ny = ny;
		float maxOffset = size * 0.25f;
		knob.setPos(base.x + size * 0.25f + nx * maxOffset,
				base.y + size * 0.25f + ny * maxOffset);
	}

	public float getXAxis() { return nx; }
	public float getYAxis() { return ny; }

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
}
