package com.ael.viner.registry;

import com.ael.viner.VinerPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VinerPlayerRegistry {

    private final Map<UUID, VinerPlayerData> players;

    public VinerPlayerRegistry(){
        players = new HashMap<>();
    }

    public static VinerPlayerRegistry create(){
        return new VinerPlayerRegistry();
    }

    public VinerPlayerData getPlayerData(Player player){
        return players.computeIfAbsent(player.getUUID(), VinerPlayerData::new);
    }

    public void setVineKeyPressed(ServerPlayer player, boolean vineKeyPressed){
        VinerPlayerData playerData = getPlayerData(player);
        playerData.setVineKeyPressed(vineKeyPressed);
    }

}
