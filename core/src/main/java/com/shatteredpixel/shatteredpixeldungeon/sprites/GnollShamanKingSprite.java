package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss.GnollShamanKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class GnollShamanKingSprite extends MobSprite {
    public GnollShamanKingSprite() {
        super();

        texture( Assets.Sprites.GNOLL_SHAMAN_KING );

        TextureFilm frames = new TextureFilm( texture, 12, 16 );

        idle = new MovieClip.Animation( 2, true );
        idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );

        run = new MovieClip.Animation( 12, true );
        run.frames( frames, 4, 5, 6, 7 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 2, 3, 0 );

        die = new MovieClip.Animation( 12, false );
        die.frames( frames, 8, 9, 10 );

        play( idle );
    }


    public void zap( int cell ) {

        super.zap( cell );

        MagicMissile.boltFromChar( parent,
                MagicMissile.SHAMAN_BLUE,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((GnollShamanKing)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
    }

}
