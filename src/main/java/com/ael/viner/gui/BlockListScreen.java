package com.ael.viner.gui;

import com.ael.viner.registry.VinerBlockRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockListScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Screen parent;
    private final List<? extends String> blockList;
    private final Consumer<List<String>> configUpdater;
    private int maxWidth = 0;

    public BlockListScreen(Screen parent, String screenTitle, List<? extends String> blockList, Consumer<List<String>> configUpdater) {
        super(Component.literal(screenTitle));
        this.parent = parent;
        this.blockList = blockList;
        this.configUpdater = configUpdater; // Store the consumer that will update the configuration
    }

    @Override
    protected void init() {
        super.init();
        addBackButton();
        addRemoveButtons();
    }

    private void addBackButton() {
        this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
                    Minecraft.getInstance().setScreen(parent);
                })
                .pos(this.width / 2 - 100, this.height - 30)
                .width(200)
                .build());
    }

    private void addRemoveButtons() {
        maxWidth = blockList.stream().mapToInt(block -> this.font.width(block)).max().orElse(0);
        int yStart = 40;
        int boxHeight = this.font.lineHeight + 3 * 2;
        int uniformBoxWidth = maxWidth + 8;

        for (int i = 0; i < blockList.size(); i++) {
            final int index = i;
            int buttonX = (this.width / 2) + (uniformBoxWidth / 2) + 4;

            this.addRenderableWidget(Button.builder(Component.literal("X"), button -> {
                        List<String> tempList = new ArrayList<>(blockList);
                        tempList.remove(index);
                        applyConfigChanges(tempList);
                    })
                    .pos(buttonX, yStart)
                    .width(20)
                    .build());
            yStart += boxHeight + 2;
        }
    }

    private void applyConfigChanges(List<String> updatedList) {
        try {
            configUpdater.accept(updatedList); // Use the provided consumer to update the config
            VinerBlockRegistry.setup();

            // Reload the screen with the updated list
            Minecraft.getInstance().setScreen(new BlockListScreen(parent, this.title.getString(), updatedList, configUpdater));
        } catch (Exception e) { // Catch a more generic exception if the error might not always be a NumberFormatException
            LOGGER.error("Error applying config changes: {}", e.getMessage());
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics); // Renders the background
        super.render(graphics, mouseX, mouseY, partialTicks); // Renders any children components

        int boxPadding = 4;
        int uniformBoxWidth = maxWidth + (boxPadding * 2); // Width based on the largest text

        int titleWidth = this.font.width(this.title);
        graphics.drawString(this.font, this.title, (this.width - titleWidth) / 2, 15, 0xFFFFFF);

        int yStart = 40;
        for (String block : blockList) {
            int textWidth = this.font.width(block);
            int boxHeight = this.font.lineHeight + (boxPadding * 2);
            int boxX = (this.width / 2) - (uniformBoxWidth / 2); // Center the box

            // Draw the box
            graphics.fill(boxX, yStart, boxX + uniformBoxWidth, yStart + boxHeight, 0xFF555555);

            // Align text to the left inside the box
            graphics.drawString(this.font, block, boxX + boxPadding, yStart + boxPadding, 0xFFFFFF);

            yStart += boxHeight + 2; // Increment for the next item, with a small gap
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
