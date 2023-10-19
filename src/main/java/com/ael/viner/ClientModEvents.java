package com.ael.viner;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ael.viner.Common.SHIFT_KEY_BINDING;
import static com.ael.viner.Viner.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientModEvents {

    // Event handler for client setup
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        // Adding custom key binding to the game's key mappings
        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(
                Minecraft.getInstance().options.keyMappings,
                SHIFT_KEY_BINDING
        );
    }

    // Recursive method to collect connected blocks
    private static void collectConnectedBlocks(Level world, BlockPos pos, BlockState targetState,
                                               List<BlockPos> connectedBlocks, Set<BlockPos> visited) {

        // Exit conditions for recursion
        if (visited.contains(pos) || !targetState.getBlock().equals(world.getBlockState(pos).getBlock())
                || connectedBlocks.size() >= VinerBlockRegistry.getVeinableLimit()) {
            return;
        }

        // Marking the current block as visited and adding to the list of connected blocks
        visited.add(pos);
        connectedBlocks.add(pos);

        // Checking adjacent blocks
        for (Direction direction : Direction.values()) {
            collectConnectedBlocks(world, pos.relative(direction), targetState, connectedBlocks, visited);
        }

        // Checking diagonal blocks
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {  // Avoid checking the original position
                        collectConnectedBlocks(world, pos.offset(dx, dy, dz), targetState, connectedBlocks, visited);
                    }
                }
            }
        }
    }

    // Event handler for block breaking
    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {

        // Getting world instance
        Level world = (Level)event.getLevel();

        // Exit conditions for server-side and correct event type
        if (world.isClientSide() || !(event instanceof BlockEvent.BreakEvent))
            return;

        // Getting player and tool instances
        Player player = event.getPlayer();
        ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);

        // Exit condition if custom key binding is not pressed
        if (!SHIFT_KEY_BINDING.isDown())
            return;

        // Other variable initializations
        BlockPos pos = event.getPos();
        BlockState targetBlockState = world.getBlockState(pos);

        // Exit condition if block is not veinmineable
        if (!VinerBlockRegistry.isVeinmineable(targetBlockState.getBlock()))
            return;

        // Checking if block can be harvested
        boolean canHarvest = targetBlockState.canHarvestBlock(world, pos, player);

        if (!canHarvest)
            return;

        // Initializations for connected block collection
        List<BlockPos> connectedBlocks = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        collectConnectedBlocks(world, pos, targetBlockState, connectedBlocks, visited);

        // Checking for Silk Touch enchantment
        boolean hasSilkTouch = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

        // Initializing block count
        int blockCount = 0;

        // Iterating through connected blocks and breaking them
        for (BlockPos connectedPos : connectedBlocks) {

            // Exit condition if block count reaches veinmineable limit
            if (blockCount >= VinerBlockRegistry.getVeinableLimit())
                break;

            // Getting block state of connected block
            BlockState connectedBlockState = world.getBlockState(connectedPos);

            // Checking if block is veinmineable and breaking it
            if (VinerBlockRegistry.isVeinmineable(targetBlockState.getBlock())) {
                if (hasSilkTouch) {
                    Block.popResource(world, connectedPos, new ItemStack(connectedBlockState.getBlock()));
                } else {
                    Block.dropResources(connectedBlockState, world, connectedPos);
                }

                // Removing block from world
                world.removeBlock(connectedPos, false);
                blockCount++;
            }
        }

        // Updating tool damage
        // TODO: Should we take efficacy into account?
        ItemStack item = player.getItemInHand(InteractionHand.MAIN_HAND);
        item.setDamageValue(item.getDamageValue() + blockCount);
    }
}
