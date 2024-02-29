package com.ael.viner.common;

import com.ael.viner.Viner;
import com.ael.viner.network.VinerPacketHandler;
import com.ael.viner.network.packets.MouseScrollPacket;
import com.ael.viner.util.MiningUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.List;

import static com.ael.viner.Viner.MOD_ID;
import static com.ael.viner.client.ClientModEvents.VINE_KEY_BINDING;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonModEvents {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean isShapeVine = false;

    /**
     * This method is triggered whenever mouse input is detected.
     * It checks if the configured key is pressed, and if so,
     * it toggles a boolean state when the mouse is scrolled.
     *
     * @param event The Mouse Scrolled Event.
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return; // Only proceed if in-game

        double scrollDelta = event.getScrollDelta();
        if (scrollDelta != 0 && VINE_KEY_BINDING.isDown()) {
            VinerPacketHandler.INSTANCE.sendToServer(new MouseScrollPacket(scrollDelta));
            event.setCanceled(true);
        }
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
        ServerPlayer player = (ServerPlayer) event.getPlayer();

        if (!Viner.getInstance().getPlayerRegistry().getPlayerData(player).isVineKeyPressed()) {
            return;
        }

        // Get the level and position of the broken block
        LevelAccessor levelAccessor = event.getLevel();
        Level level = (Level) levelAccessor;
        BlockPos pos = event.getPos();

        // Get the state and type of the broken block
        BlockState targetBlockState = level.getBlockState(pos);
        Block block = targetBlockState.getBlock();

        // Check if the block can be harvested and is vineable, then perform vein mining
        if (MiningUtils.isVineable(block) && targetBlockState.canHarvestBlock(level, pos, player)) {
            // Collect all connected blocks of the same type
            List<BlockPos> connectedBlocks = MiningUtils.collectConnectedBlocks(level, pos, targetBlockState,
                    player.getDirection().getNormal(), isShapeVine);
            MiningUtils.mineBlocks(player, connectedBlocks);
        }

    }

}
