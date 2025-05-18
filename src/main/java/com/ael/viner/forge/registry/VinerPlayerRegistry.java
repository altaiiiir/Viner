package com.ael.viner.forge.registry;

import com.ael.viner.common.IPlayerRegistry;
import com.ael.viner.common.IPlayerData;
import com.ael.viner.forge.player.VinerPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ael.viner.forge.registry.VinerBlockRegistry.getBlocksFromConfigEntries;
import static com.ael.viner.forge.registry.VinerBlockRegistry.getTagsFromConfigEntries;

public class VinerPlayerRegistry implements IPlayerRegistry {

    private final Map<UUID, VinerPlayerData> players;

    public VinerPlayerRegistry() {
        players = new HashMap<>();
    }

    public static VinerPlayerRegistry create() {
        return new VinerPlayerRegistry();
    }

    @Override
    public IPlayerData getPlayerData(Object player) {
        if (player instanceof Player) {
            return players.computeIfAbsent(((Player) player).getUUID(), VinerPlayerData::new);
        }
        throw new IllegalArgumentException("Player must be a net.minecraft.world.entity.player.Player");
    }

    public VinerPlayerData getPlayerData(Player player) {
        return players.computeIfAbsent(player.getUUID(), VinerPlayerData::new);
    }

    // Loader-agnostic setter implementations
    @Override
    public void setVineAllEnabled(Object player, boolean enabled) {
        if (player instanceof ServerPlayer) {
            getPlayerData((ServerPlayer) player).setVineAllEnabled(enabled);
        }
    }

    @Override
    public void setExhaustionPerBlock(Object player, double exhaustionPerBlock) {
        if (player instanceof ServerPlayer) {
            getPlayerData((ServerPlayer) player).setExhaustionPerBlock(exhaustionPerBlock);
        }
    }

    @Override
    public void setVineableLimit(Object player, int vineableLimit) {
        if (player instanceof ServerPlayer) {
            getPlayerData((ServerPlayer) player).setVineableLimit(vineableLimit);
        }
    }

    @Override
    public void setHeightAbove(Object player, int heightAbove) {
        if (player instanceof ServerPlayer) {
            getPlayerData((ServerPlayer) player).setHeightAbove(heightAbove);
        }
    }

    @Override
    public void setHeightBelow(Object player, int heightBelow) {
        if (player instanceof ServerPlayer) {
            getPlayerData((ServerPlayer) player).setHeightBelow(heightBelow);
        }
    }

    @Override
    public void setWidthLeft(Object player, int widthLeft) {
        if (player instanceof ServerPlayer) {
            getPlayerData((ServerPlayer) player).setWidthLeft(widthLeft);
        }
    }

    @Override
    public void setWidthRight(Object player, int widthRight) {
        if (player instanceof ServerPlayer) {
            getPlayerData((ServerPlayer) player).setWidthRight(widthRight);
        }
    }

    @Override
    public void setLayerOffset(Object player, int layerOffset) {
        if (player instanceof ServerPlayer) {
            getPlayerData((ServerPlayer) player).setLayerOffset(layerOffset);
        }
    }

    @Override
    public void setShapeVine(Object player, boolean shapeVine) {
        if (player instanceof ServerPlayer) {
            getPlayerData((ServerPlayer) player).setShapeVine(shapeVine);
        }
    }

    @Override
    public void setVineableBlocks(Object player, List<String> vineableBlockIds) {
        if (player instanceof ServerPlayer) {
            List<Block> blocks = getBlocksFromConfigEntries(vineableBlockIds);
            getPlayerData((ServerPlayer) player).setVineableBlocks(blocks);
        }
    }

    @Override
    public void setUnvineableBlocks(Object player, List<String> unvineableBlockIds) {
        if (player instanceof ServerPlayer) {
            List<Block> blocks = getBlocksFromConfigEntries(unvineableBlockIds);
            getPlayerData((ServerPlayer) player).setUnvineableBlocks(blocks);
        }
    }

    @Override
    public void setVineableTags(Object player, List<String> vineableTagNames) {
        if (player instanceof ServerPlayer) {
            List<TagKey<Block>> tags = getTagsFromConfigEntries(vineableTagNames);
            getPlayerData((ServerPlayer) player).setVineableTags(tags);
        }
    }

    @Override
    public void setUnvineableTags(Object player, List<String> unvineableTagNames) {
        if (player instanceof ServerPlayer) {
            List<TagKey<Block>> tags = getTagsFromConfigEntries(unvineableTagNames);
            getPlayerData((ServerPlayer) player).setUnvineableTags(tags);
        }
    }

    @Override
    public void setVineKeyPressed(Object player, boolean pressed) {
        if (player instanceof ServerPlayer serverPlayer) {
            getPlayerData(serverPlayer).setVineKeyPressed(pressed);
        }
    }
}

