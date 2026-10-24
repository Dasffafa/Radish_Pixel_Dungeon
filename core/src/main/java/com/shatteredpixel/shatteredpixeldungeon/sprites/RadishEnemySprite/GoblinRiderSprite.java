package com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/** 哥布林骑手：15 帧（16x18），待机 0-5、攻击 6-8、受击 9-12、死亡 13-14。 */
public class GoblinRiderSprite extends MobSprite {

    public GoblinRiderSprite() {
        super();

        texture(Assets.Sprites.GOBLIN_RIDER);
        TextureFilm frames = new TextureFilm(texture, 16, 18);

        idle = new MovieClip.Animation(2, true);
        idle.frames(frames, 0, 1, 2, 3, 4, 5);

        run = new MovieClip.Animation(10, true);
        run.frames(frames, 0, 1, 2, 3);

        attack = new MovieClip.Animation(12, false);
        attack.frames(frames, 6, 7, 8);

        zap = new MovieClip.Animation(12, false);
        zap.frames(frames, 9, 10, 11);

        die = new MovieClip.Animation(9, false);
        die.frames(frames, 13, 14);

        play(idle);
    }

    @Override
    public int blood() {
        return 0xFF3F7F3F;
    }
}
