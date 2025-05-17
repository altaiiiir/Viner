package com.ael.viner.network.packets;

import com.ael.viner.forge.VinerForge;
import com.ael.viner.network.VinerPacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

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

    public static void syncConfigWithServer(ConfigType type, Object value, String configName) {
        ConfigSyncPacket packet = new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(type, value, configName));
        VinerPacketHandler.INSTANCE.sendToServer(packet);
    }

    @Override
    public void handle(AbstractPacket<ConfigData> msg, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return; // for single player

        if ("vineAll".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.BOOLEAN) {
           VinerForge.getInstance().getPlayerRegistry().setVineAllEnabled(player, (Boolean) msg.getData().value());
        } else if("vineableLimit".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
            VinerForge.getInstance().getPlayerRegistry().setVineableLimit(player, (Integer) msg.getData().value());
        } else if("exhaustionPerBlock".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.DOUBLE) {
            VinerForge.getInstance().getPlayerRegistry().setExhaustionPerBlock(player, (Double) msg.getData().value());
        } else if("heightAbove".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
           VinerForge.getInstance().getPlayerRegistry().setHeightAbove(player, (Integer) msg.getData().value());
        } else if("heightBelow".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
            VinerForge.getInstance().getPlayerRegistry().setHeightBelow(player, (Integer) msg.getData().value());
        } else if("widthLeft".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
            VinerForge.getInstance().getPlayerRegistry().setWidthLeft(player, (Integer) msg.getData().value());
        } else if("widthRight".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
            VinerForge.getInstance().getPlayerRegistry().setWidthRight(player, (Integer) msg.getData().value());
        } else if("layerOffset".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.INT) {
            VinerForge.getInstance().getPlayerRegistry().setLayerOffset(player, (Integer) msg.getData().value());
        } else if ("shapeVine".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.BOOLEAN) {
            VinerForge.getInstance().getPlayerRegistry().setShapeVine(player, (Boolean) msg.getData().value());
        } else if ("vineableBlocks".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.BLOCK_LIST) {
            List<String> entries = (List<String>) msg.getData().value();
            List<Block> blocks = getBlocksFromConfigEntries(entries);
            List<TagKey<Block>> tags = getTagsFromConfigEntries(entries);

            VinerForge.getInstance().getPlayerRegistry().setVineableBlocks(player, blocks);
            VinerForge.getInstance().getPlayerRegistry().setVineableTags(player, tags);
        } else if ("unvineableBlocks".equals(msg.getData().configName()) && msg.getData().type() == ConfigType.BLOCK_LIST) {
            List<String> entries = (List<String>) msg.getData().value();
            List<Block> blocks = getBlocksFromConfigEntries(entries);
            List<TagKey<Block>> tags = getTagsFromConfigEntries(entries);

            VinerForge.getInstance().getPlayerRegistry().setUnvineableBlocks(player, blocks);
            VinerForge.getInstance().getPlayerRegistry().setUnvineableTags(player, tags);
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
