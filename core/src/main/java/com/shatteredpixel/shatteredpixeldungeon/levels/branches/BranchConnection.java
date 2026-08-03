/*
 * Radish Pixel Dungeon
 * 分支连接 - 定义楼梯配对关系
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.branches;

import com.watabou.utils.Bundle;

/**
 * 分支连接：定义两个楼层之间的配对关系
 * 
 * 同一 pairId 的所有楼梯形成循环组
 * 一对一时：A → B → A → B...
 * 一对多时：A → B → C → A → B → C...
 */
public class BranchConnection {
    
    public String sourceBranch;
    public int sourceDepth;
    public String destBranch;
    public int destDepth;
    public String pairId;
    
    private static final String SOURCE_BRANCH = "source_branch";
    private static final String SOURCE_DEPTH = "source_depth";
    private static final String DEST_BRANCH = "dest_branch";
    private static final String DEST_DEPTH = "dest_depth";
    private static final String PAIR_ID = "pair_id";
    
    public BranchConnection() {}
    
    public BranchConnection(String sourceBranch, int sourceDepth,
                            String destBranch, int destDepth, String pairId) {
        this.sourceBranch = sourceBranch;
        this.sourceDepth = sourceDepth;
        this.destBranch = destBranch;
        this.destDepth = destDepth;
        this.pairId = pairId;
    }
    
    public void storeInBundle(Bundle bundle) {
        bundle.put(SOURCE_BRANCH, sourceBranch);
        bundle.put(SOURCE_DEPTH, sourceDepth);
        bundle.put(DEST_BRANCH, destBranch);
        bundle.put(DEST_DEPTH, destDepth);
        bundle.put(PAIR_ID, pairId);
    }
    
    public void restoreFromBundle(Bundle bundle) {
        sourceBranch = bundle.getString(SOURCE_BRANCH);
        sourceDepth = bundle.getInt(SOURCE_DEPTH);
        destBranch = bundle.getString(DEST_BRANCH);
        destDepth = bundle.getInt(DEST_DEPTH);
        pairId = bundle.getString(PAIR_ID);
    }
}