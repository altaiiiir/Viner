package com.ael.viner.util;

import com.ael.viner.registry.VinerBlockRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MiningUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static List<BlockPos> collectConnectedBlocks(Level level, BlockPos pos, BlockState targetState) {
        List<BlockPos> connectedBlocks = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        collect(level, pos, targetState, connectedBlocks, visited);
        return connectedBlocks;
    }

    private static void collect(Level level, BlockPos pos, BlockState targetState, List<BlockPos> connectedBlocks, Set<BlockPos> visited) {

        if (visited.contains(pos) || !targetState.getBlock().equals(level.getBlockState(pos).getBlock())
                || connectedBlocks.size() >= VinerBlockRegistry.getVeinableLimit()) {
            return;
        }

        visited.add(pos);
        connectedBlocks.add(pos);

        // Checking adjacent blocks
        for (Direction direction : Direction.values()) {
            collect(level, pos.relative(direction), targetState, connectedBlocks, visited);
        }

        // Checking diagonal blocks
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    // Avoid checking the original position
                    if (dx != 0 || dy != 0 || dz != 0) {
                        collect(level, pos.offset(dx, dy, dz), targetState, connectedBlocks, visited);
                    }
                }
            }
        }
    }

    public static boolean isVeinmineable(Block block) {
        LOGGER.debug("Checking if block {} is veinmineable", block);

        if (VinerBlockRegistry.VINEABLE_BLOCKS.contains(block)) {
            LOGGER.debug("Block {} is veinmineable as a block", block);
            return true;
        }

        // Iterating through each tag to check if the block is veinmineable under any tag
        for (var tagKey : VinerBlockRegistry.VINEABLE_TAGS) {
            if (ForgeRegistries.BLOCKS.tags().getTag(tagKey).contains(block)) {
                LOGGER.debug("Block {} is veinmineable under tag {}", block, tagKey);
                return true;
            }
        }

        LOGGER.debug("Block {} is not veinmineable", block);
        return false;  // Return false if the block is not veinmineable
    }

}
