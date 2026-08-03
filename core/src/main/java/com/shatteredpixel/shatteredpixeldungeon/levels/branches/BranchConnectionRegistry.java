/*
 * Radish Pixel Dungeon
 * 分支连接注册表 - 管理所有楼梯配对关系
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.branches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 约定表：管理所有分支连接
 * 
 * 在游戏初始化时加载，提供楼梯生成的蓝图
 * 每条连接生成时自动创建成对的 pairId
 */
public class BranchConnectionRegistry {
    
    private static final List<BranchConnection> connections = new ArrayList<>();
    private static final Map<String, List<BranchConnection>> bySource = new HashMap<>();
    private static final Map<String, List<BranchConnection>> byDest = new HashMap<>();
    
    private static String key(String branch, int depth) {
        return branch + ":" + depth;
    }
    
    /**
     * 初始化约定表（游戏启动时调用）
     */
    public static void init() {
        connections.clear();
        bySource.clear();
        byDest.clear();
        
        // 主线2层 → 苔藓1层（苔藓分支入口）
        // 苔藓1层→苔藓2层 是分支内部自然楼梯，不需要约定表
        register(Branches.MAIN, 2, Branches.MOSS, 1);
        
        // 主线12层 → 采矿1层（采矿分支入口）
        register(Branches.MAIN, 12, Branches.MINING, 1);
    }
    
    /**
     * 注册一条连接（单向）
     * 源楼层创建出口，目标楼层创建入口
     */
    private static void register(String srcBranch, int srcDepth, String dstBranch, int dstDepth) {
        // 使用确定性 pairId，而非 UUID
        String pairId = srcBranch + ":" + srcDepth + "<->" + dstBranch + ":" + dstDepth;
        
        // 只添加一条记录，不双向添加
        add(new BranchConnection(srcBranch, srcDepth, dstBranch, dstDepth, pairId));
    }
    
    private static void add(BranchConnection conn) {
        connections.add(conn);
        bySource.computeIfAbsent(key(conn.sourceBranch, conn.sourceDepth), k -> new ArrayList<>()).add(conn);
        byDest.computeIfAbsent(key(conn.destBranch, conn.destDepth), k -> new ArrayList<>()).add(conn);
    }
    
    /**
     * 获取当前楼层作为源的所有连接（生成出口时使用）
     */
    public static List<BranchConnection> getFrom(String branch, int depth) {
        return bySource.getOrDefault(key(branch, depth), new ArrayList<>());
    }
    
    /**
     * 获取当前楼层作为目标的所有连接（生成入口时使用）
     */
    public static List<BranchConnection> getTo(String branch, int depth) {
        return byDest.getOrDefault(key(branch, depth), new ArrayList<>());
    }
}