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

        for (int blockIndex = 0; blockIndex < blocksToMine.size(); blockIndex++) {
            BlockPos blockPos = blocksToMine.get(blockIndex);

            // Obtain block state of connected block
            BlockState blockState = level.getBlockState(blockPos);

            // Check for Silk Touch enchantment
            boolean hasSilkTouch = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

            // Get Fortune enchantment level
            int fortuneLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);

            if (hasSilkTouch) {
                // Drop block item if Silk Touch is present
                Block.popResource(level, firstBlockPos, new ItemStack(blockState.getBlock()));
            } else {
                // Determine extra drops based on Fortune level
                Random rand = new Random();
                int drops = 1;  // Default drops
                if (fortuneLevel > 0) {
                    int maxExtraDrops = fortuneLevel * 2;  // Max extra drops is twice the Fortune level
                    int extraDrops = rand.nextInt(maxExtraDrops + 1);
                    drops += extraDrops;
                }
                for (int i = 0; i < drops; i++) {
                    // Spawn resources for each drop
                    Block.dropResources(blockState, level, firstBlockPos);
                }
            }

            // Remove block from world
            level.removeBlock(blockPos, false);

            // Update tool damage
            int unbreakingLevel = MiningUtils.getUnbreakingLevel(tool);
            double chance = MiningUtils.getDamageChance(unbreakingLevel);
            double random = Math.random();

            // Apply damage to tool based on chance
            // skips applying damage from first block - minecraft already accounts for this
            if (blockIndex != 0 && random < chance) {
                MiningUtils.applyDamage(tool, 1);  // assuming 1 damage per block
            }
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

        // Return if the position has been visited, or the block type doesn't match, or the limit has been reached
        if (visited.contains(pos) || !targetState.getBlock().equals(level.getBlockState(pos).getBlock())
                || connectedBlocks.size() >= VinerBlockRegistry.getVeinableLimit()) {
            return;
        }

        // Mark the position as visited and add it to the connected blocks list
        visited.add(pos);
        connectedBlocks.add(pos);

        // Check all adjacent blocks
        for (Direction direction : Direction.values()) {
            collect(level, pos.relative(direction), targetState, connectedBlocks, visited);
        }

        // Check all diagonal blocks
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
        return !blockExistsInUnvineableBlocks(block) && (blockExistsInTags(block) || blockExistsInVineableBlocks(block));
    }


    /**
     * Checks if a specified block is listed as unvineable in the registry.
     *
     * @param block The block to check.
     * @return true if the block is unvineable, false otherwise.
     */
    private static boolean blockExistsInUnvineableBlocks(Block block){
        return VinerBlockRegistry.UNVINEABLE_BLOCKS.contains(block);
    }

    /**
     * Checks if a specified block is listed as vineable in the registry.
     *
     * @param block The block to check.
     * @return true if the block is vineable, false otherwise.
     */
    private static boolean blockExistsInVineableBlocks(Block block){
        return VinerBlockRegistry.VINEABLE_BLOCKS.contains(block);
    }

    /**
     * Checks if a specified block is listed as vineable under any tag in the registry.
     *
     * @param block The block to check.
     * @return true if the block is vineable under any tag, false otherwise.
     */
    private static boolean blockExistsInTags(Block block){
        // Iterating through each tag to check if the block is vineable under any tag
        for (var tagKey : VinerBlockRegistry.VINEABLE_TAGS) {
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
        return EnchantmentHelper.getTagEnchantmentLevel(Enchantments.UNBREAKING, tool);
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
     * Applies damage to a specified tool.
     *
     * @param tool The tool to be damaged.
     * @param damage The amount of damage to apply.
     */
    public static void applyDamage(@NotNull ItemStack tool, int damage) {
        tool.setDamageValue(tool.getDamageValue() + damage);
    }


    /*
    public static List<ItemStack> getDrops(BlockState blockState, Level level, BlockPos blockPos, ItemStack tool) {
        return blockState.getDrops(new LootParams.Builder((ServerLevel) level)
                .withRandom(level.random)
                .withParameter(LootContextParams.TOOL, tool)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(blockPos)));
    }

    public static void spawnDrops(List<ItemStack> drops, Level level, BlockPos pos) {
        for (ItemStack drop : drops) {
            Block.popResource(level, pos, drop);
        }
    }
    */
}
