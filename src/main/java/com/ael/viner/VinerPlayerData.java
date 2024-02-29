package com.ael.viner;

import java.util.UUID;

public class VinerPlayerData {

    private final UUID playerId;
    public static boolean vineKeyPressed = false;

    public VinerPlayerData(UUID playerId){
        this.playerId = playerId;
    }

    public boolean isVineKeyPressed() {
        return vineKeyPressed;
    }

    public void setVineKeyPressed(boolean pressed) {
        vineKeyPressed = pressed;
    }
}
