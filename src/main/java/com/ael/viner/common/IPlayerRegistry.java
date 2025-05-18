package com.ael.viner.common;

import java.util.List;

public interface IPlayerRegistry {
    IPlayerData getPlayerData(Object player);
    void setVineAllEnabled(Object player, boolean enabled);
    void setExhaustionPerBlock(Object player, double exhaustionPerBlock);
    void setVineableLimit(Object player, int vineableLimit);
    void setHeightAbove(Object player, int heightAbove);
    void setHeightBelow(Object player, int heightBelow);
    void setWidthLeft(Object player, int widthLeft);
    void setWidthRight(Object player, int widthRight);
    void setLayerOffset(Object player, int layerOffset);
    void setShapeVine(Object player, boolean shapeVine);
    void setVineableBlocks(Object player, List<String> vineableBlockIds);
    void setUnvineableBlocks(Object player, List<String> unvineableBlockIds);
    void setVineableTags(Object player, List<String> vineableTagNames);
    void setUnvineableTags(Object player, List<String> unvineableTagNames);
    void setVineKeyPressed(Object player, boolean pressed);
} 