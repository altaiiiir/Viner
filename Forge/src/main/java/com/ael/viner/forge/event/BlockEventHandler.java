package com.ael.viner.forge.event;

import static com.ael.viner.forge.VinerForge.MOD_ID;

import com.ael.viner.common.IPlayerData;
import com.ael.viner.common.VinerCore;
import com.ael.viner.forge.util.MiningUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for block-related events in the Viner mod. Specifically handles the block broken
 * event to implement vein mining functionality.
 */
@Mod.EventBusSubscriber(modid = MOD_ID)
public class BlockEventHandler {
  @SubscribeEvent
  public static void onBlockBroken(BlockEvent.BreakEvent event) {
    Player player = event.getPlayer();
    BlockState blockState = event.getState();
    LevelAccessor levelAccessor = event.getLevel();
    BlockPos pos = event.getPos();

    if (!(player instanceof ServerPlayer serverPlayer
        && levelAccessor instanceof ServerLevel serverLevel)) {
      return;
    }

    // Log player config for debugging
    if (Math.random() < 0.1) { // Only log 10% of the time to avoid spam
      MiningUtils.logPlayerConfig(player);
    }

    IPlayerData playerConfig = VinerCore.getPlayerRegistry().getPlayerData(player);

    if (!playerConfig.isVineKeyPressed()) {
      return;
    }

    if (MiningUtils.isVineable(blockState.getBlock(), player)) {
      // Get the player's look position for pattern mining
      Vec3i lookPos =
          new Vec3i(
              (int) Math.round(player.getLookAngle().x),
              (int) Math.round(player.getLookAngle().y),
              (int) Math.round(player.getLookAngle().z));

      // Collect the connected blocks based on player's config
      List<BlockPos> blocksToMine =
          MiningUtils.collectConnectedBlocks(
              serverLevel,
              pos,
              blockState,
              lookPos,
              playerConfig.getVineableLimit(),
              playerConfig.isShapeVine(),
              playerConfig.getHeightAbove(),
              playerConfig.getHeightBelow(),
              playerConfig.getWidthLeft(),
              playerConfig.getWidthRight(),
              playerConfig.getLayerOffset());

      // Mine the collected blocks
      MiningUtils.mineBlocks(serverPlayer, blocksToMine);
    }
  }
}
