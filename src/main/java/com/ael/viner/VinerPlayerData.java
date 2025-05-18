package com.ael.viner;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ael.viner.forge.registry.VinerBlockRegistry;

public class VinerPlayerData {

    private final UUID playerId;
    private List<Block> vineableBlocks;
    private List<Block> unvineableBlocks;
    private List<TagKey<Block>> vineableTags;
    private List<TagKey<Block>> unvineableTags;
    private boolean vineKeyPressed = false;
    private boolean vineAllEnabled = false;
    private double exhaustionPerBlock;
    private int vineableLimit;
    private int heightAbove;
    private int heightBelow;
    private int widthLeft;
    private int widthRight;
    private int layerOffset;
    private boolean isShapeVine;

    public VinerPlayerData(UUID playerId) {
        this.playerId = playerId;
        initializeConfig();
    }

    private void initializeConfig() {
        vineAllEnabled = VinerBlockRegistry.isVineAll();
        exhaustionPerBlock = VinerBlockRegistry.getExhaustionPerBlock();
        vineableLimit = VinerBlockRegistry.getVineableLimit();
        heightAbove = VinerBlockRegistry.getHeightAbove();
        heightBelow = VinerBlockRegistry.getHeightBelow();
        widthLeft = VinerBlockRegistry.getWidthLeft();
        widthRight = VinerBlockRegistry.getWidthRight();
        layerOffset = VinerBlockRegistry.getLayerOffset();
        vineableBlocks = VinerBlockRegistry.getVineableBlocks();
        unvineableBlocks = VinerBlockRegistry.getUnvineableBlocks();
        vineableTags = VinerBlockRegistry.getVineableTags();
        unvineableTags = VinerBlockRegistry.getUnvineableTags();
        isShapeVine = VinerBlockRegistry.isShapeVine();
    }

    public List<Block> getVineableBlocks() {
        return vineableBlocks;
    }

    public void setVineableBlocks(List<Block> vineableBlocks) {
        this.vineableBlocks = new ArrayList<>(vineableBlocks);
    }

    public List<Block> getUnvineableBlocks() {
        return unvineableBlocks;
    }

    public void setUnvineableBlocks(List<Block> unvineableBlocks) {
        this.unvineableBlocks = new ArrayList<>(unvineableBlocks);
    }

    public List<TagKey<Block>> getVineableTags() {
        return vineableTags;
    }

    public void setVineableTags(List<TagKey<Block>> vineableTags) {
        this.vineableTags = vineableTags;
    }

    public List<TagKey<Block>> getUnvineableTags() {
        return unvineableTags;
    }

    public void setUnvineableTags(List<TagKey<Block>> unvineableTags) {
        this.unvineableTags = unvineableTags;
    }
    public boolean isVineKeyPressed() {
        return vineKeyPressed;
    }

    public void setVineKeyPressed(boolean pressed) {
        this.vineKeyPressed = pressed;
    }

    public boolean isVineAllEnabled() {
        return vineAllEnabled;
    }

    public void setVineAllEnabled(boolean enabled) {
        this.vineAllEnabled = enabled;
    }

    public double getExhaustionPerBlock() {
        return exhaustionPerBlock;
    }

    public void setExhaustionPerBlock(double exhaustionPerBlock) {
        this.exhaustionPerBlock = exhaustionPerBlock;
    }

    public int getVineableLimit() {
        return vineableLimit;
    }

    public void setVineableLimit(int vineableLimit) {
        this.vineableLimit = vineableLimit;
    }

    public int getHeightAbove() {
        return heightAbove;
    }

    public void setHeightAbove(int heightAbove) {
        this.heightAbove = heightAbove;
    }

    public int getHeightBelow() {
        return heightBelow;
    }

    public void setHeightBelow(int heightBelow) {
        this.heightBelow = heightBelow;
    }

    public int getWidthLeft() {
        return widthLeft;
    }

    public void setWidthLeft(int widthLeft) {
        this.widthLeft = widthLeft;
    }

    public int getWidthRight() {
        return widthRight;
    }

    public void setWidthRight(int widthRight) {
        this.widthRight = widthRight;
    }

    public int getLayerOffset() {
        return layerOffset;
    }

    public void setLayerOffset(int layerOffset) {
        this.layerOffset = layerOffset;
    }

    public boolean isShapeVine() { return isShapeVine; }
    public void setShapeVine(boolean isShapeVine) { this.isShapeVine = isShapeVine; }
}
