package com.ael.viner.event;

import com.ael.viner.common.VinerEntrypoint;
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

import static com.ael.viner.forge.VinerForge.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockEventHandler {
    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        if (!VinerEntrypoint.get().getPlayerRegistry().getPlayerData(player).isVineKeyPressed()) {
            return;
        }
        LevelAccessor levelAccessor = event.getLevel();
        Level level = (Level) levelAccessor;
        BlockPos pos = event.getPos();
        BlockState targetBlockState = level.getBlockState(pos);
        Block block = targetBlockState.getBlock();
        var playerConfig = VinerEntrypoint.get().getPlayerRegistry().getPlayerData(player);
        boolean isShapeVine = playerConfig.isShapeVine();
        int vineableLimit = playerConfig.getVineableLimit();
        int heightAbove = playerConfig.getHeightAbove();
        int heightBelow = playerConfig.getHeightBelow();
        int widthLeft = playerConfig.getWidthLeft();
        int widthRight = playerConfig.getWidthRight();
        int layerOffset = playerConfig.getLayerOffset();
        if (MiningUtils.isVineable(block, player) && targetBlockState.canHarvestBlock(level, pos, player)) {
            List<BlockPos> connectedBlocks = MiningUtils.collectConnectedBlocks(level, pos, targetBlockState,
                    player.getDirection().getNormal(), vineableLimit, isShapeVine, heightAbove, heightBelow, widthLeft,
                    widthRight, layerOffset);
            MiningUtils.mineBlocks(player, connectedBlocks);
            double exhaustionPerBlock = playerConfig.getExhaustionPerBlock();
            player.getFoodData().addExhaustion((float) (exhaustionPerBlock * connectedBlocks.size()));
        }
    }
} 