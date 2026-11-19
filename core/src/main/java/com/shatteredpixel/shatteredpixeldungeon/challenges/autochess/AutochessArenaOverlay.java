package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.watabou.noosa.ui.Component;

/**
 * 棋盘装饰层（当前为空）。
 *
 * <p>金边和外围网格都被反馈「不好看」撤掉了，敌我区分交给地砖颜色；
 * 这里保留一个空组件，方便以后加「选中高亮」之类的临时效果。</p>
 */
public class AutochessArenaOverlay extends Component {

	public AutochessArenaOverlay() {
		super();
	}
}