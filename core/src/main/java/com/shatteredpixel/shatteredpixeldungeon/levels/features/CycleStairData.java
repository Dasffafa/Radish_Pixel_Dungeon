package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;

/**
 * 循环楼梯的数据：同一条链上的三扇楼梯 A/B/C，各自记录目的格。
 */
public class CycleStairData implements Bundlable {

	public int cell = -1;
	public String chain = "";
	public int slot = 0;      // 0=A 1=B 2=C
	public int destCell = -1; // 目的格（同层）
	public String destBranch = ""; // 非空则跨层
	public int destDepth = 0;

	public CycleStairData() {
	}

	public CycleStairData(int cell, String chain, int slot, int destCell) {
		this.cell = cell;
		this.chain = chain;
		this.slot = slot;
		this.destCell = destCell;
	}

	public CycleStairData(int cell, String chain, int slot, String destBranch, int destDepth) {
		this.cell = cell;
		this.chain = chain;
		this.slot = slot;
		this.destBranch = destBranch;
		this.destDepth = destDepth;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put("cell", cell);
		bundle.put("chain", chain);
		bundle.put("slot", slot);
		bundle.put("dest_cell", destCell);
		bundle.put("dest_branch", destBranch);
		bundle.put("dest_depth", destDepth);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		cell = bundle.getInt("cell");
		chain = bundle.getString("chain");
		if (chain == null) chain = "";
		slot = bundle.getInt("slot");
		destCell = bundle.getInt("dest_cell");
		destBranch = bundle.getString("dest_branch");
		if (destBranch == null) destBranch = "";
		destDepth = bundle.getInt("dest_depth");
	}
}