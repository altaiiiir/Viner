package com.ael.viner.forge.network.packets;

import com.ael.viner.common.VinerEntrypoint;
import com.ael.viner.forge.network.VinerPacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.ael.viner.forge.registry.VinerBlockRegistry.getBlocksFromConfigEntries;
import static com.ael.viner.forge.registry.VinerBlockRegistry.getTagsFromConfigEntries;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
                Object value = data.value();
                List<String> list;
                if (value instanceof List<?> rawList) {
                    list = rawList.stream().allMatch(e -> e instanceof String)
                        ? rawList.stream().map(e -> (String) e).collect(Collectors.toList())
                        : List.of();
                } else {
                    list = List.of();
                }
                buf.writeCollection(list, FriendlyByteBuf::writeUtf);
            }
        }
    }

    public static void syncConfigWithServer(ConfigType type, Object value, String configName) {
        ConfigSyncPacket packet = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(type, value, configName));
        VinerPacketHandler.INSTANCE.sendToServer(packet);
    }

    @Override
    public void handle(AbstractPacket<ConfigData> msg, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return; // for single player

        if ("vineAll".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.BOOLEAN) {
           VinerEntrypoint.get().getPlayerRegistry().setVineAllEnabled(player, (Boolean) msg.getData().value());
        } else if("vineableLimit".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
            VinerEntrypoint.get().getPlayerRegistry().setVineableLimit(player, (Integer) msg.getData().value());
        } else if("exhaustionPerBlock".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.DOUBLE) {
            VinerEntrypoint.get().getPlayerRegistry().setExhaustionPerBlock(player, (Double) msg.getData().value());
        } else if("heightAbove".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
           VinerEntrypoint.get().getPlayerRegistry().setHeightAbove(player, (Integer) msg.getData().value());
        } else if("heightBelow".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
            VinerEntrypoint.get().getPlayerRegistry().setHeightBelow(player, (Integer) msg.getData().value());
        } else if("widthLeft".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
            VinerEntrypoint.get().getPlayerRegistry().setWidthLeft(player, (Integer) msg.getData().value());
        } else if("widthRight".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
            VinerEntrypoint.get().getPlayerRegistry().setWidthRight(player, (Integer) msg.getData().value());
        } else if("layerOffset".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
            VinerEntrypoint.get().getPlayerRegistry().setLayerOffset(player, (Integer) msg.getData().value());
        } else if ("shapeVine".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.BOOLEAN) {
            VinerEntrypoint.get().getPlayerRegistry().setShapeVine(player, (Boolean) msg.getData().value());
        } else if ("vineableBlocks".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.BLOCK_LIST) {
            Object value = msg.getData().value();
            List<String> entries;
            if (value instanceof List<?> list) {
                entries = list.stream().allMatch(e -> e instanceof String)
                    ? list.stream().map(e -> (String) e).collect(Collectors.toList())
                    : List.of();
            } else {
                entries = List.of();
            }
            ((com.ael.viner.forge.registry.VinerPlayerRegistry) VinerEntrypoint.get().getPlayerRegistry()).setVineableBlocks(player, entries);
            ((com.ael.viner.forge.registry.VinerPlayerRegistry) VinerEntrypoint.get().getPlayerRegistry()).setVineableTags(player, entries);
        } else if ("unvineableBlocks".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.BLOCK_LIST) {
            Object value = msg.getData().value();
            List<String> entries;
            if (value instanceof List<?> list) {
                entries = list.stream().allMatch(e -> e instanceof String)
                    ? list.stream().map(e -> (String) e).collect(Collectors.toList())
                    : List.of();
            } else {
                entries = List.of();
            }

            ((com.ael.viner.forge.registry.VinerPlayerRegistry) VinerEntrypoint.get().getPlayerRegistry()).setUnvineableBlocks(player, entries);
            ((com.ael.viner.forge.registry.VinerPlayerRegistry) VinerEntrypoint.get().getPlayerRegistry()).setUnvineableTags(player, entries);
        }

        ctx.get().setPacketHandled(true);
    }

    public enum ConfigType {
        BOOLEAN,
        DOUBLE,
        INT,
        BLOCK_LIST
    }

    public record ConfigData(ConfigType type, Object value, String configName) {}

}
