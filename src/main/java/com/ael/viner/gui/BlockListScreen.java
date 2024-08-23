package com.ael.viner.gui;

import com.ael.viner.registry.VinerBlockRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockListScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String minecraftTagsLink = "https://mcreator.net/wiki/minecraft-block-and-item-list-registry-and-code-names";
    private int linkX, linkY, linkWidth, linkHeight;
    private final Screen parent;
    private final List<? extends String> blockList;
    private final Consumer<List<String>> configUpdater;
    private EditBox blockTagInputField;
    private int maxWidth = 0;

    public BlockListScreen(Screen parent, String screenTitle, List<? extends String> blockList, Consumer<List<String>> configUpdater) {
        super(Component.literal(screenTitle));
        this.parent = parent;
        this.blockList = blockList;
        this.configUpdater = configUpdater;
    }

    @Override
    protected void init() {
        super.init();
        setupUIElements();
    }

    // Setup UI elements like the input field and buttons
    private void setupUIElements() {
        setupInputField();
        setupControlButtons();
        addRemoveButtons();
    }

    // Initialize and add the input field for block/tag names
    private void setupInputField() {
        int fieldWidth = 200; // Width of the input field
        int fieldX = this.width / 2 - fieldWidth / 2; // Center the input field horizontally
        int fieldY = this.height - 70; // Position the input field towards the bottom

        this.blockTagInputField = new EditBox(this.font, fieldX, fieldY, fieldWidth, 20, Component.literal(""));
        this.blockTagInputField.setMaxLength(50);
        this.addRenderableWidget(blockTagInputField);
    }

    // Setup control buttons (Back and Add)
    private void setupControlButtons() {
        int fieldWidth = 200;
        int fieldX = this.width / 2 - fieldWidth / 2;
        int buttonY = this.blockTagInputField.getY() + 25; // Position buttons directly below the input field

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> Minecraft.getInstance().setScreen(parent))
                .pos(fieldX, buttonY).width(fieldWidth / 2 - 2).build());

        // Add button
        this.addRenderableWidget(Button.builder(Component.literal("Add"), button -> {
            String input = blockTagInputField.getValue();
            if (validateInput(input)) {
                List<String> tempList = new ArrayList<>(blockList);
                tempList.add(input);
                applyConfigChanges(tempList);
                blockTagInputField.setValue(""); // Clear the input field after adding
            } else {
                LOGGER.error("Invalid block/tag format: {}", input);
            }
        }).pos(fieldX + fieldWidth / 2 + 2, buttonY).width(fieldWidth / 2 - 2).build());
    }

    // Validate the input against the expected pattern
    private boolean validateInput(String input) {
        return input.matches("^#?[a-z_]+:[a-z_]+$");
    }

    // Add buttons for removing blocks/tags from the list
    private void addRemoveButtons() {
        calculateMaxWidth();
        setupRemoveButtonForEachBlock();
    }

    // Calculate the maximum width among all blocks/tags to align remove buttons properly
    private void calculateMaxWidth() {
        maxWidth = blockList.stream().mapToInt(block -> this.font.width(block)).max().orElse(0);
    }

    // Setup a remove button next to each block/tag
    private void setupRemoveButtonForEachBlock() {
        int yStart = 40; // Starting position for the first block/tag
        for (int i = 0; i < blockList.size(); i++) {
            final int index = i;
            int buttonX = (this.width / 2) + (maxWidth + 8) / 2 + 4;
            this.addRenderableWidget(Button.builder(Component.literal("X"), button -> {
                        List<String> tempList = new ArrayList<>(blockList);
                        tempList.remove(index);
                        applyConfigChanges(tempList);
                    })
                    .pos(buttonX, yStart)
                    .width(20)
                    .build());
            yStart += this.font.lineHeight + 10; // Move down for the next block/tag
        }
    }

    // Apply configuration changes and refresh the screen
    private void applyConfigChanges(List<String> updatedList) {
        try {
            configUpdater.accept(updatedList);
            VinerBlockRegistry.setup();
            Minecraft.getInstance().setScreen(new BlockListScreen(parent, this.title.getString(), updatedList, configUpdater));
        } catch (Exception e) {
            LOGGER.error("Error applying config changes: {}", e.getMessage());
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics); // Renders the background
        super.render(graphics, mouseX, mouseY, partialTicks); // Renders any children components like buttons and input fields

        int yStart = 40;

        // Add instructional text above the input field
        renderInstructionalText(graphics, yStart);

        yStart+=5;

        // Render each block/tag with its remove button
        renderBlocksAndTags(graphics, yStart);
    }

    private void renderInstructionalText(@NotNull GuiGraphics graphics, int yStart) {
        String instructionalText = "Enter block names or tags here, for example:";

        int textY = yStart - 30;
        graphics.drawString(this.font, Component.literal(instructionalText), this.width / 2 - this.font.width(instructionalText) / 2, textY, 0xFFFFFF);

        linkX = this.width / 2 - this.font.width(minecraftTagsLink) / 2;
        linkY = textY + 10;
        linkWidth = this.font.width(minecraftTagsLink);
        linkHeight = this.font.lineHeight;

        graphics.drawString(this.font, Component.literal(minecraftTagsLink), linkX, linkY, 0x55FF55);
    }


    // Render blocks/tags and their respective remove buttons
    private void renderBlocksAndTags(@NotNull GuiGraphics graphics, int yStart) {
        for (String block : blockList) {
            int boxX = (this.width / 2) - (maxWidth + 8) / 2; // Center the box horizontally
            graphics.fill(boxX, yStart, boxX + maxWidth + 8, yStart + this.font.lineHeight + 8, 0xFF555555);
            graphics.drawString(this.font, block, boxX + 4, yStart + 4, 0xFFFFFF); // Align text to the left inside the box
            yStart += this.font.lineHeight + 10; // Move down for the next block/tag
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
