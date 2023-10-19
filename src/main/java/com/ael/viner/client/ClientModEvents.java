package com.ael.viner.client;

import com.ael.viner.network.VeinMiningPacket;
import com.ael.viner.network.VinerPacketHandler;
import com.ael.viner.util.MiningUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import static com.ael.viner.Viner.MOD_ID;
import static com.ael.viner.common.Common.SHIFT_KEY_BINDING;

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



    // Event handler for block breaking
    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {

        if (!SHIFT_KEY_BINDING.isDown())
            return;

        Level level = (Level)event.getLevel();
        BlockPos pos = event.getPos();
        BlockState targetBlockState = level.getBlockState(pos);
        Block block = targetBlockState.getBlock();

        if (targetBlockState.canHarvestBlock(level, pos, event.getPlayer()) && MiningUtils.isVineable(block)){
            List<BlockPos> connectedBlocks = MiningUtils.collectConnectedBlocks(level, pos, targetBlockState);
            VeinMiningPacket packet = new VeinMiningPacket(connectedBlocks);
            VinerPacketHandler.INSTANCE.sendToServer(packet);
        }

    }

//    @SubscribeEvent
//    public static void onBlockBroken(BlockEvent.BreakEvent event) {
//
//        // Getting world instance
//        Level world = (Level)event.getLevel();
//
//        // Exit conditions for server-side and correct event type
//        if (world.isClientSide() || !(event instanceof BlockEvent.BreakEvent))
//            return;
//
//        // Getting player and tool instances
//        Player player = event.getPlayer();
//        ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);
//
//        // Exit condition if custom key binding is not pressed
//        if (!SHIFT_KEY_BINDING.isDown())
//            return;
//
//        // Other variable initializations
//        BlockPos pos = event.getPos();
//        BlockState targetBlockState = world.getBlockState(pos);
//
//        // Exit condition if block is not veinmineable
//        if (!VinerBlockRegistry.isVeinmineable(targetBlockState.getBlock()))
//            return;
//
//        // Checking if block can be harvested
//        boolean canHarvest = targetBlockState.canHarvestBlock(world, pos, player);
//
//        if (!canHarvest)
//            return;
//
//        // Initializations for connected block collection
//        List<BlockPos> connectedBlocks = new ArrayList<>();
//        Set<BlockPos> visited = new HashSet<>();
//        collectConnectedBlocks(world, pos, targetBlockState, connectedBlocks, visited);
//
//        // Checking for Silk Touch enchantment
//        boolean hasSilkTouch = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;
//
//        // Initializing block count
//        int blockCount = 0;
//
//        // Iterating through connected blocks and breaking them
//        for (BlockPos connectedPos : connectedBlocks) {
//
//            // Exit condition if block count reaches veinmineable limit
//            if (blockCount >= VinerBlockRegistry.getVeinableLimit())
//                break;
//
//            // Getting block state of connected block
//            BlockState connectedBlockState = world.getBlockState(connectedPos);
//
//            // Checking if block is veinmineable and breaking it
//            if (VinerBlockRegistry.isVeinmineable(targetBlockState.getBlock())) {
//                if (hasSilkTouch) {
//                    Block.popResource(world, connectedPos, new ItemStack(connectedBlockState.getBlock()));
//                } else {
//                    Block.dropResources(connectedBlockState, world, connectedPos);
//                }
//
//                // Removing block from world
//                world.removeBlock(connectedPos, false);
//                blockCount++;
//            }
//        }
//
//        // Updating tool damage
//        // TODO: Should we take efficacy into account?
//        ItemStack item = player.getItemInHand(InteractionHand.MAIN_HAND);
//        item.setDamageValue(item.getDamageValue() + blockCount);
//    }
}
