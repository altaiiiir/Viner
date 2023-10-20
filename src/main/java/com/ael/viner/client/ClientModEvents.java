package com.ael.viner.client;

import com.ael.viner.network.VeinMiningPacket;
import com.ael.viner.network.VinerPacketHandler;
import com.ael.viner.util.MiningUtils;
import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

import java.util.List;

import static com.ael.viner.Viner.MOD_ID;
import static com.ael.viner.common.Common.SHIFT_KEY_BINDING;

/**
 * This class handles the vein mining feature when a block is broken.
 */
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientModEvents {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Event handler for client setup
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        // Adding custom key binding to the game's key mappings
        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(
                Minecraft.getInstance().options.keyMappings,
                SHIFT_KEY_BINDING
        );
    }

    /**
     * This method is triggered whenever a block is broken.
     * It checks if the SHIFT (or whatever is configured) key is held down, and if so,
     * it performs vein mining on vineable blocks.
     *
     * @param event The Block Break Event.
     */
    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {

        // Return early if the SHIFT key is not held down
        if (!SHIFT_KEY_BINDING.isDown())
            return;

        // Get the level and position of the broken block
        Level level = (Level)event.getLevel();
        BlockPos pos = event.getPos();

        // Get the state and type of the broken block
        BlockState targetBlockState = level.getBlockState(pos);
        Block block = targetBlockState.getBlock();

        // Check if the block can be harvested and is vineable, then perform vein mining
        if (targetBlockState.canHarvestBlock(level, pos, event.getPlayer()) && MiningUtils.isVineable(block)){
            // Collect all connected blocks of the same type
            List<BlockPos> connectedBlocks = MiningUtils.collectConnectedBlocks(level, pos, targetBlockState);

            // Create and send a packet to the server to perform vein mining
            VeinMiningPacket packet = new VeinMiningPacket(connectedBlocks);
            VinerPacketHandler.INSTANCE.sendToServer(packet);
        }

    }

}
