package com.shatteredpixel.shatteredpixeldungeon.effects.shaders;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.watabou.glwrap.Vertexbuffer;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoor;
import com.watabou.glscripts.Script;
import com.watabou.glwrap.Uniform;
import com.watabou.noosa.NoosaScript;

public class SpecialDoorShader extends NoosaScript {

	private final Uniform marker = uniform("uMarker");
	private boolean walls;

	public void walls(boolean walls) {
		this.walls = walls;
	}

	public static SpecialDoorShader get() {
		return Script.use(SpecialDoorShader.class);
	}

	@Override
	public void drawQuadSet(Vertexbuffer buffer, int length, int offset) {
		int end = offset + length;
		while (offset < end) {
			SpecialDoor next = null;
			int stop = end;
			for (SpecialDoor door : Dungeon.level.specialDoors.values()) {
				if (!DungeonTileSheet.doorTile(Dungeon.level.map[door.cell])) continue;
				int cell = walls ? door.cell - Dungeon.level.width() : door.cell;
				if (walls && cell < offset && door.cell + Dungeon.level.width() < Dungeon.level.length()
						&& DungeonTileSheet.wallStitcheable(Dungeon.level.map[door.cell + Dungeon.level.width()])) {
					cell = door.cell;
				}
				if (cell >= offset && cell < stop) {
					next = door;
					stop = cell;
				}
			}
			mark(null);
			super.drawQuadSet(buffer, stop - offset, offset);
			if (next == null) break;
			mark(next);
			super.drawQuadSet(buffer, 1, stop);
			offset = stop + 1;
		}
		mark(null);
	}

	public void mark(SpecialDoor door) {
		float r = 1f, g = 1f, b = 1f, strength = 0f;
		if (door != null) {
			strength = 1f;
			if (door.sealed) {
				r = 0.75f; g = 0.18f; b = 0.18f;
			} else if (door.locked()) {
				if ("explosion".equals(door.triggerId)) {
					r = 1f; g = 0.48f; b = 0.12f;
				} else if (door.triggerId.startsWith("signal:combat:")) {
					r = 1f; g = 0.24f; b = 0.24f;
				} else {
					r = 0.68f; g = 0.42f; b = 1f;
				}
			} else if (door.oneWay) {
				r = 0.25f; g = 0.75f; b = 1f;
			} else if (door.oneShot) {
				r = 1f; g = 0.82f; b = 0.25f;
			} else if (door.toggle || !door.triggerId.isEmpty()) {
				r = 0.35f; g = 1f; b = 0.55f;
			} else {
				strength = 0f;
			}
		}
		marker.value4f(r, g, b, strength);
	}

	@Override
	protected String shader() {
		return super.shader().replace("uniform vec4 uColorA;", "uniform vec4 uColorA;\nuniform vec4 uMarker;")
				.replace("  gl_FragColor = texture2D( uTex, vUV ) * uColorM + uColorA;",
						"  vec4 tex = texture2D(uTex, vUV);\n"
						+ "  float gray = dot(tex.rgb, vec3(0.299, 0.587, 0.114));\n"
						+ "  tex.rgb = mix(tex.rgb, gray * uMarker.rgb, uMarker.a);\n"
						+ "  gl_FragColor = tex * uColorM + uColorA;");
	}
}
