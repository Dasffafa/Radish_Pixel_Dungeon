package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Image;

public class PeddlerActionIcon extends Image {

    public static final int TALK = 0;
    public static final int SWAP = 1;

    public PeddlerActionIcon(int type) {
        super(Icons.get(type == TALK ? Icons.INFO : Icons.REPEAT).texture);
        frame(Icons.get(type == TALK ? Icons.INFO : Icons.REPEAT).frame());
    }
}
