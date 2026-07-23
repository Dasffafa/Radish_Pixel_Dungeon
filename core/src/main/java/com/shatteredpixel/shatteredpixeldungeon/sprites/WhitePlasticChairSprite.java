package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class WhitePlasticChairSprite extends MobSprite {
    public WhitePlasticChairSprite() {
        super();
        texture(Assets.Sprites.POWAH_CHAIR);

        TextureFilm frames = new TextureFilm(texture, 16, 16);

        idle = new Animation(1, true);
        idle.frames(frames, 0);

        run = new Animation(1, true);
        run.frames(frames, 0);

        attack = new Animation(1, false);
        attack.frames(frames, 0);

        die = new Animation(1, false);
        die.frames(frames, 0);

        play(idle);
    }
}
