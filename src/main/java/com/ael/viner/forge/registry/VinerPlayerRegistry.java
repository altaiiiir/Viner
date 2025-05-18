package com.ael.viner.forge.registry;

import com.ael.viner.VinerPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VinerPlayerRegistry {

    private final Map<UUID, VinerPlayerData> players;

    public VinerPlayerRegistry() {
        players = new HashMap<>();
    }

    public static VinerPlayerRegistry create() {
        return new VinerPlayerRegistry();
    }

    public VinerPlayerData getPlayerData(Player player) {
        return players.computeIfAbsent(player.getUUID(), VinerPlayerData::new);
    }

    public void setVineableBlocks(ServerPlayer player, List<Block> vineableBlocks) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setVineableBlocks(vineableBlocks);
    }

    public void setUnvineableBlocks(ServerPlayer player, List<Block> unvineableBlocks) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setUnvineableBlocks(unvineableBlocks);
    }

    public void setVineableTags(ServerPlayer player, List<TagKey<Block>> vineableTags) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setVineableTags(vineableTags);
    }

    public void setUnvineableTags(ServerPlayer player, List<TagKey<Block>> unvineableTags) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setUnvineableTags(unvineableTags);
    }

    public void setVineKeyPressed(ServerPlayer player, boolean vineKeyPressed) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setVineKeyPressed(vineKeyPressed);
    }

    public void setVineAllEnabled(ServerPlayer player, boolean vineAllEnabled) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setVineAllEnabled(vineAllEnabled);
    }

    public void setExhaustionPerBlock(ServerPlayer player, double exhaustionPerBlock) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setExhaustionPerBlock(exhaustionPerBlock);
    }

    public void setVineableLimit(ServerPlayer player, int vineableLimit) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setVineableLimit(vineableLimit);
    }

    public void setHeightAbove(ServerPlayer player, int heightAbove) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setHeightAbove(heightAbove);
    }

    public void setHeightBelow(ServerPlayer player, int heightBelow) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setHeightBelow(heightBelow);
    }

    public void setWidthLeft(ServerPlayer player, int widthLeft) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setWidthLeft(widthLeft);
    }

    public void setWidthRight(ServerPlayer player, int widthRight) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setWidthRight(widthRight);
    }

    public void setLayerOffset(ServerPlayer player, int layerOffset) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setLayerOffset(layerOffset);
    }

    public void setShapeVine(ServerPlayer player, Boolean shapeVine) {
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setShapeVine(shapeVine);
    }
}

