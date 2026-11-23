package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.watabou.noosa.ui.Component;

/**
 * 棋子标记组件（已停用）。
 *
 * <p>以前会在地面上画「己方」小箭头，和星级、血条、法力条挤在一起显得很乱。
 * 现在标记全部由 {@link AutochessHealthBars}（星/血/蓝）和
 * {@link AutochessSelectionHighlight}（选中框）负责，这个组件保留空实现，
 * 只是为了让 GameScene 的添加语句不用改。</p>
 */
public class AutochessUnitMarks extends Component {
}
