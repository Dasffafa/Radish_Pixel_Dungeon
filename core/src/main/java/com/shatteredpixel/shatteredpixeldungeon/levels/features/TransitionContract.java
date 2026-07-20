/*
 * Radish Pixel Dungeon
 * 支线系统 - 过渡约定类
 * 记录楼梯的配对信息，用于楼层生成时创建对应的入口/出口
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.watabou.utils.Bundle;

/**
 * 过渡约定：记录楼梯的配对信息
 * 
 * 当楼层 A 创建一个通往楼层 B 的楼梯时，会记录约定。
 * 当楼层 B 生成时，会查询约定表并创建对应的楼梯。
 */
public class TransitionContract {

    // 楼梯 ID：{来源分支}_{来源层数}_to_{目标分支}_{目标层数}
    public String id;
    
    // 来源信息
    public int sourceDepth;
    public String sourceBranch;
    
    // 目标信息
    public int destDepth;
    public String destBranch;
    
    // 目标楼梯 ID
    public String destId;

    public TransitionContract() {
        // 用于序列化
    }

    public TransitionContract(String id, int sourceDepth, String sourceBranch, 
                              int destDepth, String destBranch, String destId) {
        this.id = id;
        this.sourceDepth = sourceDepth;
        this.sourceBranch = sourceBranch;
        this.destDepth = destDepth;
        this.destBranch = destBranch;
        this.destId = destId;
    }

    /**
     * 从 LevelTransition 创建约定
     */
    public static TransitionContract fromTransition(LevelTransition t, int sourceDepth, String sourceBranch) {
        return new TransitionContract(
            t.id,
            sourceDepth,
            sourceBranch,
            t.destDepth,
            t.destBranchId != null ? t.destBranchId : "main",
            t.destId
        );
    }

    // ====== 序列化 ======

    private static final String ID = "id";
    private static final String SOURCE_DEPTH = "source_depth";
    private static final String SOURCE_BRANCH = "source_branch";
    private static final String DEST_DEPTH = "dest_depth";
    private static final String DEST_BRANCH = "dest_branch";
    private static final String DEST_ID = "dest_id";

    public void storeInBundle(Bundle bundle) {
        bundle.put(ID, id);
        bundle.put(SOURCE_DEPTH, sourceDepth);
        bundle.put(SOURCE_BRANCH, sourceBranch);
        bundle.put(DEST_DEPTH, destDepth);
        bundle.put(DEST_BRANCH, destBranch);
        bundle.put(DEST_ID, destId);
    }

    public void restoreFromBundle(Bundle bundle) {
        id = bundle.getString(ID);
        sourceDepth = bundle.getInt(SOURCE_DEPTH);
        sourceBranch = bundle.getString(SOURCE_BRANCH);
        destDepth = bundle.getInt(DEST_DEPTH);
        destBranch = bundle.getString(DEST_BRANCH);
        destId = bundle.getString(DEST_ID);
    }
}