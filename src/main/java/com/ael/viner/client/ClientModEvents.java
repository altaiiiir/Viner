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

}
