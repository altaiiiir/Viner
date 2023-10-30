package com.ael.viner.common;

import com.ael.viner.Viner;
import com.ael.viner.util.MiningUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static com.ael.viner.Viner.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonModEvents {

    /**
     * This method is triggered whenever a block is broken.
     * It checks if the SHIFT (or whatever is configured) key is held down, and if so,
     * it performs vein mining on vineable blocks.
     *
     * @param event The Block Break Event.
     */
    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        ServerPlayer player = (ServerPlayer)event.getPlayer();

        if (!Viner.getInstance().getPlayerRegistry().getPlayerData(player).isVineKeyPressed()){
            return;
        }

        // Get the level and position of the broken block
        LevelAccessor levelAccessor = event.getLevel();
        Level level = (Level)levelAccessor;
        BlockPos pos = event.getPos();

        // Get the state and type of the broken block
        BlockState targetBlockState = level.getBlockState(pos);
        Block block = targetBlockState.getBlock();

        // Check if the block can be harvested and is vineable, then perform vein mining
        if (MiningUtils.isVineable(block) &&
            targetBlockState.canHarvestBlock(level, pos, event.getPlayer())){
            // Collect all connected blocks of the same type
            List<BlockPos> connectedBlocks = MiningUtils.collectConnectedBlocks(level, pos, targetBlockState);

            MiningUtils.mineBlocks(player, connectedBlocks);

//            // Create and send a packet to the server to perform vein mining
//            VeinMiningPacket packet = new VeinMiningPacket(connectedBlocks);
//            VinerPacketHandler.INSTANCE.sen(packet);
        }

    }

}
