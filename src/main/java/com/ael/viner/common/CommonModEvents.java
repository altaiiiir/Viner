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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.ael.viner.Viner.MOD_ID;
import static com.ael.viner.client.ClientModEvents.VINE_KEY_BINDING;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CommonModEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // server logic only
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Send configuration packets to the player using reflection
        sendConfigPacketToPlayer(new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.BLOCK_LIST, getVineableBlocks(), "vineableBlocks")), serverPlayer);
        sendConfigPacketToPlayer(new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.BLOCK_LIST, getUnvineableBlocks(), "unvineableBlocks")), serverPlayer);
        sendConfigPacketToPlayer(new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.BOOLEAN, VinerBlockRegistry.isVineAll(), "vineAll")), serverPlayer);
        sendConfigPacketToPlayer(new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.DOUBLE, VinerBlockRegistry.getExhaustionPerBlock(), "exhaustionPerBlock")), serverPlayer);
        sendConfigPacketToPlayer(new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, VinerBlockRegistry.getVineableLimit(), "vineableLimit")), serverPlayer);
        sendConfigPacketToPlayer(new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, VinerBlockRegistry.getHeightAbove(), "heightAbove")), serverPlayer);
        sendConfigPacketToPlayer(new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, VinerBlockRegistry.getHeightBelow(), "heightBelow")), serverPlayer);
        sendConfigPacketToPlayer(new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, VinerBlockRegistry.getWidthLeft(), "widthLeft")), serverPlayer);
        sendConfigPacketToPlayer(new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, VinerBlockRegistry.getWidthRight(), "widthRight")), serverPlayer);
        sendConfigPacketToPlayer(new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.INT, VinerBlockRegistry.getLayerOffset(), "layerOffset")), serverPlayer);
        sendConfigPacketToPlayer(new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(ConfigSyncPacket.ConfigType.BOOLEAN, VinerBlockRegistry.isShapeVine(), "shapeVine")), serverPlayer);
    }

    private static void sendConfigPacketToPlayer(Object packet, ServerPlayer player) {
        try {
            if (isVersion21OrLater()) {
                // Directly use player for PacketDistributor.PLAYER.with
                Method sendMethod = VinerPacketHandler.INSTANCE.getClass().getMethod("send", Object.class, Object.class);

                // Pass the player directly, not a Supplier
                Object packetTarget = PacketDistributor.PLAYER.with(player);

                // Invoke the send method
                sendMethod.invoke(VinerPacketHandler.INSTANCE, packetTarget, packet);
            } else {
                // Handle for 1.20.x
                sendConfigPacketToPlayerForOldVersion(packet, player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isVersion21OrLater() {
        try {
            // This checks for a method introduced in 1.21.x
            Class<?> packetDistributorClass = Class.forName("net.minecraftforge.network.PacketDistributor");
            Method withMethod = packetDistributorClass.getMethod("with", ServerPlayer.class);
            return true; // If method exists, it's 1.21.x or later
        } catch (Exception e) {
            return false; // If not, it's 1.20.x or earlier
        }
    }

    private static void sendConfigPacketToPlayerForOldVersion(Object packet, ServerPlayer player) {
        try {
            // Fallback mechanism for 1.20.x
            Class<?> packetDistributorClass = Class.forName("net.minecraftforge.network.PacketDistributor");
            Object playerDistributor = packetDistributorClass.getDeclaredField("PLAYER").get(null);

            // Reflectively invoke the "send" method in VinerPacketHandler using reflection
            Method sendMethod = VinerPacketHandler.INSTANCE.getClass().getMethod("send", Object.class, Object.class);
            sendMethod.invoke(VinerPacketHandler.INSTANCE, playerDistributor, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> getVineableBlocks() {
        return VinerBlockRegistry.getVineableBlocks().stream()
                .map(block -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString())
                .collect(Collectors.toList());
    }

    private static List<String> getUnvineableBlocks() {
        return VinerBlockRegistry.getUnvineableBlocks().stream()
                .map(block -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString())
                .collect(Collectors.toList());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null && VINE_KEY_BINDING.isDown()) {
                mc.setScreen(new ConfigScreen()); // Open GUI
            }
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
    public static void onBlockBroken(BlockEvent.BreakEvent event) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
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

        boolean isShapeVine = playerConfig.isShapeVine();
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
            double exhaustionPerBlock = playerConfig.getExhaustionPerBlock();
            player.getFoodData().addExhaustion((float) (exhaustionPerBlock * connectedBlocks.size()));
        }
    }
}
