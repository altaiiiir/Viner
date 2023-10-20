package com.ael.viner.util;

import com.ael.viner.registry.VinerBlockRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.*;

public class MiningUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void mineBlocks(ServerPlayer player, List<BlockPos> blocksToMine) {
        if (player == null)
            return;

        Level level = player.level();
        ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);

        // I don't think this is possible, but can't be sure.
        if (level.isClientSide())
            return;

        // We ideally want all the drops to spawn at the first blockPos
        BlockPos firstBlockPos = blocksToMine.get(0);

        for (BlockPos blockPos: blocksToMine) {

            // Getting block state of connected block
            BlockState blockState = level.getBlockState(blockPos);

            // Checking for Silk Touch enchantment
            boolean hasSilkTouch = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

            if (hasSilkTouch) {
                Block.popResource(level, firstBlockPos, new ItemStack(blockState.getBlock()));
            } else {
                // FIXME: We need to implement logic for Fortune
                Block.dropResources(blockState, level, firstBlockPos);
            }

            // Removing block from world
            level.removeBlock(blockPos, false);

            // Updating tool damage
            int unbreakingLevel = MiningUtils.getUnbreakingLevel(tool);
            double chance = MiningUtils.getDamageChance(unbreakingLevel);

            if (Math.random() < chance) {
                MiningUtils.applyDamage(tool, blocksToMine.size());  // assuming 1 damage per block
            }
        }
    }


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

    public static boolean isVineable(Block block) {
        return !blockExistsInUnvineableBlocks(block) && (blockExistsInTags(block) || blockExistsInVineableBlocks(block));
    }

    private static boolean blockExistsInUnvineableBlocks(Block block){
        return VinerBlockRegistry.UNVINEABLE_BLOCKS.contains(block);
    }

    private static boolean blockExistsInVineableBlocks(Block block){
        return VinerBlockRegistry.VINEABLE_BLOCKS.contains(block);
    }

    private static boolean blockExistsInTags(Block block){
        // Iterating through each tag to check if the block is vineable under any tag
        for (var tagKey : VinerBlockRegistry.VINEABLE_TAGS) {
            if (tagContainsBlock(tagKey, block)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tagContainsBlock(TagKey<Block> tagKey, Block block){
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.tags()).getTag(tagKey).contains(block);
    }

    public static int getUnbreakingLevel(ItemStack tool) {
        return EnchantmentHelper.getTagEnchantmentLevel(Enchantments.UNBREAKING, tool);
    }

    public static double getDamageChance(int unbreakingLevel) {
        return 1.0 / (unbreakingLevel + 1);
    }

    public static void applyDamage(ItemStack tool, int damage) {
        tool.setDamageValue(tool.getDamageValue() + damage);
    }

//    public static int getFortuneLevel(ItemStack tool) {
//        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
//    }
//
//    public static List<ItemStack> getDrops(BlockState blockState, Level level, BlockPos blockPos, ItemStack tool) {
//        return blockState.getDrops(new LootParams.Builder((ServerLevel) level)
//                .withRandom(level.random)
//                .withParameter(LootContextParams.TOOL, tool)
//                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(blockPos)));
//    }
//
//    public static void spawnDrops(List<ItemStack> drops, Level level, BlockPos pos) {
//        for (ItemStack drop : drops) {
//            Block.popResource(level, pos, drop);
//        }
//    }
}
