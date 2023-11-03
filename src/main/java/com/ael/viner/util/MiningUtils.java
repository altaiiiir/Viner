package com.ael.viner.util;

import com.ael.viner.registry.VinerBlockRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class MiningUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Mines a list of blocks on behalf of a player, applying the appropriate tool enchantments,
     * updating tool damage, and spawning drops at the position of the first block in the list.
     *
     * @param player The player who is mining the blocks.
     * @param blocksToMine A list of BlockPos representing the blocks to be mined.
     */
    public static void mineBlocks(ServerPlayer player, List<BlockPos> blocksToMine) {
        if (player == null)
            return;

        Level level = player.level();
        ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);

        // Check for client side, return early if true
        if (level.isClientSide())
            return;

        // Initial block position for spawning all drops
        BlockPos firstBlockPos = blocksToMine.get(0);
        int currentBlock = 1;

        for (BlockPos blockPos : blocksToMine) {

            // Stop breaking blocks if the tool is about to break or if it's the first block
            if (currentBlock++ != 1 && applyDamage(tool, 1)) break;

            spawnBlockDrops(player, (ServerLevel) level, blockPos, tool, firstBlockPos);
            spawnExp(level.getBlockState(blockPos), (ServerLevel) level, firstBlockPos, tool);

            level.removeBlock(blockPos, false);
        }
    }

    private static void spawnExp(BlockState blockState, ServerLevel level, BlockPos blockPos, ItemStack tool) {

        // Gets current tools enchantments
        int fortuneLevel = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
        int silkTouchLevel = tool.getEnchantmentLevel(Enchantments.SILK_TOUCH);

        // Gets the XP expected to drop from a block
        int exp = blockState.getExpDrop(level, level.random, blockPos, fortuneLevel, silkTouchLevel);

        // If there's any XP to drop, it drops it
        if (exp > 0) {
            blockState.getBlock().popExperience(level, blockPos, exp);
        }
    }

    private static void spawnBlockDrops(ServerPlayer player, ServerLevel level, BlockPos blockPos, ItemStack tool, BlockPos firstBlockPos) {

        // making sure we use the native getDrops method which uses loot tables and takes the tool into account.
        List<ItemStack> itemsToDrop = Block.getDrops(level.getBlockState(blockPos), level, blockPos, null, player, tool);

        // I'm sure there is a native way to do this, but i cba to look for it, this works for now.
        // All we're doing is spawning the entities on the first block.
        for (ItemStack item : itemsToDrop) {
            double d0 = (double)(level.random.nextFloat() * 0.5F) + 0.25D;
            double d1 = (double)(level.random.nextFloat() * 0.5F) + 0.25D;
            double d2 = (double)(level.random.nextFloat() * 0.5F) + 0.25D;

            ItemEntity itemEntity = new ItemEntity(level, firstBlockPos.getX() + d0, firstBlockPos.getY() + d1, firstBlockPos.getZ() + d2, item);
            level.addFreshEntity(itemEntity);
        }
    }


    /**
     * Initiates the collection of connected blocks of the same type as the specified block.
     * It creates fresh lists for connected blocks and visited positions, then calls the
     * recursive collect method to find all connected blocks.
     *
     * @param level The level where the block exists.
     * @param pos The position of the block being vein mined.
     * @param targetState The BlockState of the block being vein mined.
     * @return A list of BlockPos representing all connected blocks of the same type.
     */
    public static List<BlockPos> collectConnectedBlocks(Level level, BlockPos pos, BlockState targetState) {
        List<BlockPos> connectedBlocks = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        collect(level, pos, targetState, connectedBlocks, visited);
        return connectedBlocks;
    }


    /**
     * Recursively collects connected blocks of the same type to the specified block,
     * up to a maximum limit defined in VinerBlockRegistry. This method checks adjacent
     * and diagonal blocks for the same block type, adding them to a list if they match.
     *
     * @param level The level where the block exists.
     * @param pos The position of the current block being checked.
     * @param targetState The BlockState of the block being vein mined.
     * @param connectedBlocks A list to store positions of connected blocks of the same type.
     * @param visited A set to keep track of already visited positions, to avoid infinite recursion.
     */
    private static void collect(Level level, BlockPos pos, BlockState targetState, List<BlockPos> connectedBlocks, Set<BlockPos> visited) {
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(pos);

        while (!queue.isEmpty() && connectedBlocks.size() < VinerBlockRegistry.getVeinableLimit()) {
            BlockPos currentPos = queue.poll();

            if (visited.contains(currentPos) || !targetState.getBlock().equals(level.getBlockState(currentPos).getBlock())) {
                continue;
            }

            visited.add(currentPos);
            connectedBlocks.add(currentPos);

            // Add all adjacent blocks to the queue
            for (Direction direction : Direction.values()) {
                queue.add(currentPos.relative(direction));
            }

            // Add all diagonal blocks to the queue
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx != 0 || dy != 0 || dz != 0) {
                            queue.add(currentPos.offset(dx, dy, dz));
                        }
                    }
                }
            }
        }
    }



    /**
     * Checks if a block is vineable based on predefined criteria.
     * A block is considered vineable if:
     * 1. It does not exist in the list of unvineable blocks.
     * 2. It either exists within specified tags or in the list of vineable blocks.
     *
     * @param block The block to be checked.
     * @return true if the block is vineable, false otherwise.
     */
    public static boolean isVineable(Block block) {
        return VinerBlockRegistry.getVineAll() || (!blockExistsInUnvineableBlocks(block) && (blockExistsInTags(block) || blockExistsInVineableBlocks(block)));
    }


    /**
     * Checks if a specified block is listed as unvineable in the registry.
     *
     * @param block The block to check.
     * @return true if the block is unvineable, false otherwise.
     */
    private static boolean blockExistsInUnvineableBlocks(Block block){
        return VinerBlockRegistry.getUnvineableBlocks().contains(block);
    }

    /**
     * Checks if a specified block is listed as vineable in the registry.
     *
     * @param block The block to check.
     * @return true if the block is vineable, false otherwise.
     */
    private static boolean blockExistsInVineableBlocks(Block block){
        return VinerBlockRegistry.getVineableBlocks().contains(block);
    }

    /**
     * Checks if a specified block is listed as vineable under any tag in the registry.
     *
     * @param block The block to check.
     * @return true if the block is vineable under any tag, false otherwise.
     */
    private static boolean blockExistsInTags(Block block){
        // Iterating through each tag to check if the block is vineable under any tag
        List<TagKey<Block>> tags = VinerBlockRegistry.getVineableTags();
        for (var tagKey : tags) {
            if (tagContainsBlock(tagKey, block)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks if a specified block is contained within a given tag.
     *
     * @param tagKey The key of the tag to check.
     * @param block The block to check for within the tag.
     * @return true if the tag contains the block, false otherwise.
     */
    private static boolean tagContainsBlock(TagKey<Block> tagKey, Block block){
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.tags()).getTag(tagKey).contains(block);
    }


    /**
     * Retrieves the level of the Unbreaking enchantment on a specified tool.
     *
     * @param tool The tool whose Unbreaking enchantment level is to be checked.
     * @return The level of the Unbreaking enchantment, or 0 if not present.
     */
    public static int getUnbreakingLevel(ItemStack tool) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, tool);
    }

    /**
     * Calculates the chance of a tool taking damage based on its Unbreaking enchantment level.
     *
     * @param unbreakingLevel The level of the Unbreaking enchantment on the tool.
     * @return The chance of the tool taking damage.
     */
    public static double getDamageChance(int unbreakingLevel) {
        return 1.0 / (unbreakingLevel + 1);
    }

    /**
     * Applies damage to a specified tool. Taking Unbreaking into consideration
     *
     * @param tool The tool to be damaged.
     * @param damage The amount of damage to apply.
     *
     * @return A boolean containing whether the applyDamage function broke the weapon
     */
    public static boolean applyDamage(@NotNull ItemStack tool, int damage) {

        if (!tool.isDamageableItem())
            return false;

        int unbreakingLevel = getUnbreakingLevel(tool);
        double chance = getDamageChance(unbreakingLevel);
        double random = Math.random();

        // Only apply damage if the random value is less than the damage chance.
        if (random < chance) {
            int newDamage = tool.getDamageValue() + damage;
            int maxDamage = tool.getMaxDamage();

            // Prevent over-damaging the tool
            if (newDamage >= maxDamage) {
                tool.setDamageValue(maxDamage);
                return true;
            } else {
                tool.setDamageValue(newDamage);
            }
        }

        return false;
    }
}
