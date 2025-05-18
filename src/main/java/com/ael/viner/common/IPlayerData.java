package com.ael.viner.common;

public interface IPlayerData {
    boolean isVineKeyPressed();
    boolean isVineAllEnabled();
    double getExhaustionPerBlock();
    int getVineableLimit();
    int getHeightAbove();
    int getHeightBelow();
    int getWidthLeft();
    int getWidthRight();
    int getLayerOffset();
    boolean isShapeVine();
}