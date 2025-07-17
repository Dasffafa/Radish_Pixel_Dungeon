package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.watabou.utils.Bundle;

public class VitaeBuff extends Buff{
    private int vitae;
    public int maxVitae;

    public int getVitae(){
        return vitae;
    }

    public void setVitae(int vt){
        this.vitae = vt;
        this.maxVitae = vt;
    }

    public int absorbDamage( int dmg ){
        if (vitae >= dmg){
            vitae -= dmg;
            dmg = 0;
        } else {
            vitae = 0;
        }
        if (vitae == 0){
            detach();
        }
        return dmg;
    }

    private static final String VITAE = "vitae";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( VITAE, vitae);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        vitae = bundle.getInt( VITAE );
    }
}
