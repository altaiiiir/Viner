package com.ael.viner.forge.event;

import static com.ael.viner.forge.VinerForge.MOD_ID;

import com.ael.viner.forge.network.VinerPacketHandler;
import com.ael.viner.forge.network.packets.ConfigSyncPacket;
import com.ael.viner.forge.registry.VinerBlockRegistry;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {
  @SubscribeEvent
  public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
      return;
    }
    List<String> vineableBlocks =
        VinerBlockRegistry.getVineableBlocks().stream()
            .map(block -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString())
            .collect(Collectors.toList());
    ConfigSyncPacket vineableBlocksPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.BLOCK_LIST, vineableBlocks, "vineableBlocks"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), vineableBlocksPacket);

    List<String> unvineableBlocks =
        VinerBlockRegistry.getUnvineableBlocks().stream()
            .map(block -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString())
            .collect(Collectors.toList());
    ConfigSyncPacket unvineableBlocksPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.BLOCK_LIST, unvineableBlocks, "unvineableBlocks"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), unvineableBlocksPacket);

    boolean vineAllEnabled = VinerBlockRegistry.isVineAll();
    ConfigSyncPacket vineAllPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.BOOLEAN, vineAllEnabled, "vineAll"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), vineAllPacket);

    double exhaustionPerBlock = VinerBlockRegistry.getExhaustionPerBlock();
    ConfigSyncPacket exhaustionPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.DOUBLE, exhaustionPerBlock, "exhaustionPerBlock"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), exhaustionPacket);

    int vineableLimit = VinerBlockRegistry.getVineableLimit();
    ConfigSyncPacket vineableLimitPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, vineableLimit, "vineableLimit"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), vineableLimitPacket);

    int heightAbove = VinerBlockRegistry.getHeightAbove();
    ConfigSyncPacket heightAbovePacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, heightAbove, "heightAbove"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), heightAbovePacket);

    int heightBelow = VinerBlockRegistry.getHeightBelow();
    ConfigSyncPacket heightBelowPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, heightBelow, "heightBelow"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), heightBelowPacket);

    int widthLeft = VinerBlockRegistry.getWidthLeft();
    ConfigSyncPacket widthLeftPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, widthLeft, "widthLeft"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), widthLeftPacket);

    int widthRight = VinerBlockRegistry.getWidthRight();
    ConfigSyncPacket widthRightPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, widthRight, "widthRight"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), widthRightPacket);

    int layerOffset = VinerBlockRegistry.getLayerOffset();
    ConfigSyncPacket layerOffsetPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, layerOffset, "layerOffset"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), layerOffsetPacket);

    Boolean shapeVine = VinerBlockRegistry.isShapeVine();
    ConfigSyncPacket shapeVinePacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.BOOLEAN, shapeVine, "shapeVine"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), shapeVinePacket);
  }
}
