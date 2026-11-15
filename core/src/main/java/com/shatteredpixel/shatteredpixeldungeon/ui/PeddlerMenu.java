package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.watabou.input.PointerEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;

public class PeddlerMenu extends Component {

    private static final int ICON_SIZE = 12;
    private static final int PADDING = 3;
    private static final int GAP = 2;
    private final ColorBlock background;
    private final ColorBlock[] border = new ColorBlock[4];
    private final PointerArea blocker;
    private final IconButton[] buttons;

    public PeddlerMenu(Image[] icons, Runnable[] actions) {
        background = new ColorBlock(1, 1, 0xFFFFFFFF);
        add(background);
        blocker = new PointerArea(0, 0, 0, 0) {
            @Override
            public boolean onSignal(PointerEvent event) {
                if (event != null && event.type == PointerEvent.Type.DOWN
                        && !background.overlapsScreenPoint((int) event.current.x, (int) event.current.y)) {
                    PeddlerMenu.this.killAndErase();
                }
                return false;
            }
        };
        addToBack(blocker);
        for (int i = 0; i < border.length; i++) {
            border[i] = new ColorBlock(1, 1, 0xFF000000);
            add(border[i]);
        }
        buttons = new IconButton[icons.length];
        for (int i = 0; i < icons.length; i++) {
            final Runnable action = actions[i];
            buttons[i] = new IconButton(icons[i]) {
                @Override
                protected void onClick() {
                    PeddlerMenu.this.killAndErase();
                    action.run();
                }
            };
            add(buttons[i]);
        }
        width = PADDING * 2 + icons.length * ICON_SIZE + (icons.length - 1) * GAP;
        height = PADDING * 2 + ICON_SIZE;
        layout();
    }

    public void placeAbove(float centerX, float topY) {
        float left = centerX - width / 2f;
        float top = topY - height - 2;
        left = Math.max(0, Math.min(left, camera.width - width));
        if (top < 0) top = topY + 18;
        top = Math.max(0, Math.min(top, camera.height - height));
        setPos(left, top);
        layout();
    }

    @Override
    protected void layout() {
        super.layout();
        background.x = x;
        background.y = y;
        background.size(width, height);
        border[0].size(width, 1);
        border[0].setPos(x, y);
        border[1].size(width, 1);
        border[1].setPos(x, y + height - 1);
        border[2].size(1, height);
        border[2].setPos(x, y);
        border[3].size(1, height);
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setRect(x + PADDING + i * (ICON_SIZE + GAP), y + PADDING, ICON_SIZE, ICON_SIZE);
        }
    }
}
