package com.ael.viner.network.packets;

import com.ael.viner.Viner;
import com.ael.viner.network.VinerPacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

import static com.ael.viner.registry.VinerBlockRegistry.getBlocksFromConfigEntries;
import static com.ael.viner.registry.VinerBlockRegistry.getTagsFromConfigEntries;

public class ConfigSyncPacket extends AbstractPacket<ConfigSyncPacket.ConfigData> {

    public static final PacketFactory<ConfigSyncPacket> FACTORY = buf -> {
        ConfigType type = buf.readEnum(ConfigType.class);
        String configName = buf.readUtf(32767);
        Object value = switch (type) {
            case BOOLEAN -> buf.readBoolean();
            case DOUBLE -> buf.readDouble();
            case INT -> buf.readInt();
            case BLOCK_LIST -> buf.readList(FriendlyByteBuf::readUtf);
        };
        return new ConfigSyncPacket(new ConfigData(type, value, configName));
    };

    private static final Logger LOGGER = LogUtils.getLogger();

    public ConfigSyncPacket(ConfigData data) {
        super(data);
    }

    public static void encode(ConfigSyncPacket msg, FriendlyByteBuf buf) {
        ConfigData data = msg.getData();
        buf.writeEnum(data.type());
        buf.writeUtf(data.configName());
        switch (data.type()) {
            case BOOLEAN -> buf.writeBoolean((Boolean) data.value());
            case DOUBLE -> buf.writeDouble((Double) data.value());
            case INT -> buf.writeInt((Integer) data.value());
            case BLOCK_LIST -> {
                List<String> list = (List<String>) data.value();
                buf.writeCollection(list, FriendlyByteBuf::writeUtf);
            }
        }
    }

    /**
     * Syncs the config with the server.
     * Uses reflection to invoke sendToServer on the appropriate channel (SimpleChannel or PayloadChannel).
     */
    public static void syncConfigWithServer(ConfigType type, Object value, String configName) {
        ConfigSyncPacket packet = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(type, value, configName));

        // Use reflection to invoke sendToServer
        try {
            Method sendToServerMethod = VinerPacketHandler.INSTANCE.getClass().getMethod("sendToServer", Object.class);
            sendToServerMethod.invoke(VinerPacketHandler.INSTANCE, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(AbstractPacket<ConfigData> msg, @NotNull Supplier<Object> ctx) {
        try {
            Object context = ctx.get(); // Dynamically handle context

            // Dynamically detect the presence of methods/classes based on the version
            boolean usePayloadChannel = classExists("net.minecraftforge.network.PayloadChannel");

            if (usePayloadChannel) {
                // Newer version (1.21.x or later) handling using PayloadChannel
                Method enqueueWorkMethod = context.getClass().getMethod("enqueueWork", Runnable.class);
                Method setPacketHandledMethod = context.getClass().getMethod("setPacketHandled", boolean.class);

                enqueueWorkMethod.invoke(context, (Runnable) () -> {
                    ServerPlayer player = getServerPlayer(context);
                    if (player == null) return; // Single-player case

                    processConfig(msg, player);
                });

                setPacketHandledMethod.invoke(context, true);

            } else {
                // Older version (1.20.x) handling using SimpleChannel
                Method enqueueWorkMethod = context.getClass().getMethod("enqueueWork", Runnable.class);
                Method setPacketHandledMethod = context.getClass().getMethod("setPacketHandled", boolean.class);

                enqueueWorkMethod.invoke(context, (Runnable) () -> {
                    ServerPlayer player = getServerPlayer(context);
                    if (player == null) return; // Single-player case

                    processConfig(msg, player);
                });

                setPacketHandledMethod.invoke(context, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to process configuration data
     */
    private void processConfig(AbstractPacket<ConfigData> msg, ServerPlayer player) {
        // Handle various config updates based on the type and name
        ConfigData data = msg.getData();

        if ("vineAll".equals(data.configName()) && data.type() == ConfigType.BOOLEAN) {
            Viner.getInstance().getPlayerRegistry().setVineAllEnabled(player, (Boolean) data.value());
        } else if ("vineableLimit".equals(data.configName()) && data.type() == ConfigType.INT) {
            Viner.getInstance().getPlayerRegistry().setVineableLimit(player, (Integer) data.value());
        } else if ("exhaustionPerBlock".equals(data.configName()) && data.type() == ConfigType.DOUBLE) {
            Viner.getInstance().getPlayerRegistry().setExhaustionPerBlock(player, (Double) data.value());
        } else if ("heightAbove".equals(data.configName()) && data.type() == ConfigType.INT) {
            Viner.getInstance().getPlayerRegistry().setHeightAbove(player, (Integer) data.value());
        } else if ("widthLeft".equals(data.configName()) && data.type() == ConfigType.INT) {
            Viner.getInstance().getPlayerRegistry().setWidthLeft(player, (Integer) data.value());
        } else if ("vineableBlocks".equals(data.configName()) && data.type() == ConfigType.BLOCK_LIST) {
            List<String> entries = (List<String>) data.value();
            List<Block> blocks = getBlocksFromConfigEntries(entries);
            List<TagKey<Block>> tags = getTagsFromConfigEntries(entries);

            Viner.getInstance().getPlayerRegistry().setVineableBlocks(player, blocks);
            Viner.getInstance().getPlayerRegistry().setVineableTags(player, tags);
        }
    }

    /**
     * Helper method to get ServerPlayer from context (reflection)
     */
    private ServerPlayer getServerPlayer(Object context) {
        try {
            Method getSenderMethod = context.getClass().getMethod("getSender");
            return (ServerPlayer) getSenderMethod.invoke(context);
        } catch (Exception e) {
            return null; // Handle exception or return null for single-player mode
        }
    }

    /**
     * Helper method to check if a class exists
     */
    private boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public enum ConfigType {
        BOOLEAN,
        DOUBLE,
        INT,
        BLOCK_LIST
    }

    public record ConfigData(ConfigType type, Object value, String configName) {}
}
