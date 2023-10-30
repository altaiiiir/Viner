package com.ael.viner;

import java.util.UUID;

public class VinerPlayerData {

    private final UUID playerId;
    private boolean vineKeyPressed = false;

    public VinerPlayerData(UUID playerId){
        this.playerId = playerId;
    }

    public boolean isVineKeyPressed() {
        return vineKeyPressed;
    }

    public void setVineKeyPressed(boolean pressed) {
        this.vineKeyPressed = pressed;
    }
}
