package com.ael.viner.common;

import com.ael.viner.Viner;
import com.ael.viner.client.ClientModEvents;
import com.ael.viner.gui.ConfigScreen;
import com.ael.viner.network.VinerPacketHandler;
import com.ael.viner.network.packets.ConfigSyncPacket;
import com.ael.viner.registry.VinerBlockRegistry;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ael.viner.Viner.MOD_ID;
import static com.ael.viner.client.ClientModEvents.VINE_KEY_BINDING;



@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonModEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // server logic only
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        List<String> vineableBlocks = VinerBlockRegistry.getVineableBlocks().stream()
                .map(block -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString())
                .collect(Collectors.toList());
        ConfigSyncPacket vineableBlocksPacket = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.BLOCK_LIST, vineableBlocks, "vineableBlocks"));
        VinerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), vineableBlocksPacket);

        List<String> unvineableBlocks = VinerBlockRegistry.getUnvineableBlocks().stream()
                .map(block -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString())
                .collect(Collectors.toList());
        ConfigSyncPacket unvineableBlocksPacket = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.BLOCK_LIST, unvineableBlocks, "unvineableBlocks"));
        VinerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), unvineableBlocksPacket);

        boolean vineAllEnabled = VinerBlockRegistry.isVineAll();
        ConfigSyncPacket vineAllPacket = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.BOOLEAN, vineAllEnabled, "vineAll"));
        VinerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), vineAllPacket);

        double exhaustionPerBlock = VinerBlockRegistry.getExhaustionPerBlock();
        ConfigSyncPacket exhaustionPacket = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.DOUBLE, exhaustionPerBlock, "exhaustionPerBlock"));
        VinerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), exhaustionPacket);

        int vineableLimit = VinerBlockRegistry.getVineableLimit();
        ConfigSyncPacket vineableLimitPacket = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, vineableLimit, "vineableLimit"));
        VinerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), vineableLimitPacket);

        int heightAbove = VinerBlockRegistry.getHeightAbove();
        ConfigSyncPacket heightAbovePacket = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, heightAbove, "heightAbove"));
        VinerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), heightAbovePacket);

        int heightBelow = VinerBlockRegistry.getHeightBelow();
        ConfigSyncPacket heightBelowPacket = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, heightBelow, "heightBelow"));
        VinerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), heightBelowPacket);

        int widthLeft = VinerBlockRegistry.getWidthLeft();
        ConfigSyncPacket widthLeftPacket = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, widthLeft, "widthLeft"));
        VinerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), widthLeftPacket);

        int widthRight = VinerBlockRegistry.getWidthRight();
        ConfigSyncPacket widthRightPacket = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, widthRight, "widthRight"));
        VinerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), widthRightPacket);

        int layerOffset = VinerBlockRegistry.getLayerOffset();
        ConfigSyncPacket layerOffsetPacket = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, layerOffset, "layerOffset"));
        VinerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), layerOffsetPacket);

        Boolean shapeVine = VinerBlockRegistry.isShapeVine();
        ConfigSyncPacket shapeVinePacket = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.BOOLEAN, shapeVine, "shapeVine"));
        VinerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), shapeVinePacket);
    }


    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null && ClientModEvents.VINER_CONFIG_KEY_BINDING.isDown()) {
                mc.setScreen(new ConfigScreen()); // Open GUI
            }
        }
    }

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
            //VinerPacketHandler.INSTANCE.sendToServer(new MouseScrollPacket(scrollDelta));
            //event.setCanceled(true);

            // not used anymore
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

        var playerConfig = Viner.getInstance().getPlayerRegistry().getPlayerData(player);

        Boolean isShapeVine = playerConfig.isShapeVine();
        int vineableLimit = playerConfig.getVineableLimit();
        int heightAbove = playerConfig.getHeightAbove();
        int heightBelow = playerConfig.getHeightBelow();
        int widthLeft = playerConfig.getWidthLeft();
        int widthRight = playerConfig.getWidthRight();
        int layerOffset = playerConfig.getLayerOffset();

        // Check if the block can be harvested and is vineable, then perform vein mining
        if (MiningUtils.isVineable(block, player) && targetBlockState.canHarvestBlock(level, pos, player)) {
            // Collect all connected blocks of the same type
            List<BlockPos> connectedBlocks = MiningUtils.collectConnectedBlocks(level, pos, targetBlockState,
                    player.getDirection().getNormal(), vineableLimit, isShapeVine, heightAbove, heightBelow, widthLeft,
                    widthRight, layerOffset);

            MiningUtils.mineBlocks(player, connectedBlocks);

            // Increase player exhaustion
            double exhaustionPerBlock = Viner.getInstance().getPlayerRegistry().getPlayerData(player).getExhaustionPerBlock();
            player.getFoodData().addExhaustion((float) (exhaustionPerBlock * connectedBlocks.size()));

        }

    }

}
