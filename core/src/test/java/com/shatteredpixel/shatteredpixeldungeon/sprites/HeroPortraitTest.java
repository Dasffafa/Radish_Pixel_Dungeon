package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HeroPortraitTest {
    @Test
    public void jumbleUsesWholeIndependentFrameRegardlessOfArmorRow() {
        GdxNativesLoader.load();
        SmartTexture texture = new SmartTexture(new Pixmap(336, 192, Pixmap.Format.RGBA8888)) {
            @Override public void delete() { bitmap.dispose(); bitmap = null; }
        };
        TextureCache.put(Assets.Sprites.JUMBLE, texture);
        try {
            Image avatar = HeroSprite.avatar(HeroClasses.JUMBLE, 5, 0);
            Image body = HeroSprite.body(HeroClasses.JUMBLE, 5, 0);
            for (Image image : new Image[]{avatar, body}) {
                assertEquals(12, image.width(), 0.00001f);
                assertEquals(16, image.height(), 0.00001f);
                assertEquals(0, image.frame().left, 0);
                assertEquals(0, image.frame().top, 0);
                assertEquals(12f / 336, image.frame().right, 0.000001f);
                assertEquals(16f / 192, image.frame().bottom, 0.000001f);
                image.destroy();
            }
        } finally {
            TextureCache.remove(Assets.Sprites.JUMBLE);
        }
    }
}
