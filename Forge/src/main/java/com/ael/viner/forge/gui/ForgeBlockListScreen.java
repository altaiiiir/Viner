package com.ael.viner.forge.gui;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

/**
 * Forge-specific block list screen. Contains all UI logic in Forge module to avoid cross-module
 * classloader issues in 1.21.8.
 */
@OnlyIn(Dist.CLIENT)
public class ForgeBlockListScreen extends Screen {
  private static final Logger LOGGER = LogUtils.getLogger();
  private final String minecraftTagsLink =
      "https://mcreator.net/wiki/minecraft-block-and-item-list-registry-and-code-names";
  private int linkX, linkY;
  private final Screen parent;
  private final List<? extends String> blockList;
  private final Consumer<List<String>> configUpdater;
  private EditBox blockTagInputField;
  private int maxWidth = 0;

  public ForgeBlockListScreen(
      Screen parent,
      String screenTitle,
      List<? extends String> blockList,
      Consumer<List<String>> configUpdater) {
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
    int fieldWidth = 200;
    int fieldX = this.width / 2 - fieldWidth / 2; // Center the input field horizontally
    int fieldY = this.height - 70; // Position the input field towards the bottom

    this.blockTagInputField =
        new EditBox(this.font, fieldX, fieldY, fieldWidth, 20, Component.literal(""));
    this.blockTagInputField.setMaxLength(50);
    this.addRenderableWidget(blockTagInputField);
  }

  // Setup control buttons (Back and Add)
  private void setupControlButtons() {
    int fieldWidth = 200;
    int fieldX = this.width / 2 - fieldWidth / 2;
    int buttonY =
        this.blockTagInputField.getY() + 25; // Position buttons directly below the input field

    // Back button
    this.addRenderableWidget(
        Button.builder(
                Component.literal("Back"), button -> Minecraft.getInstance().setScreen(parent))
            .pos(fieldX, buttonY)
            .width(fieldWidth / 2 - 2)
            .build());

    // Add button
    this.addRenderableWidget(
        Button.builder(
                Component.literal("Add"),
                button -> {
                  String input = blockTagInputField.getValue();
                  if (validateInput(input)) {
                    List<String> tempList = new ArrayList<>(blockList);
                    tempList.add(input);
                    applyConfigChanges(tempList);
                    blockTagInputField.setValue(""); // Clear the input field after adding
                  } else {
                    LOGGER.error("Invalid block/tag format: {}", input);
                  }
                })
            .pos(fieldX + fieldWidth / 2 + 2, buttonY)
            .width(fieldWidth / 2 - 2)
            .build());
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
    maxWidth = blockList.stream().mapToInt(block -> this.font.width(block)).max().orElse(100);
  }

  // Setup a remove button next to each block/tag
  private void setupRemoveButtonForEachBlock() {
    int yStart = 40; // Starting position for the first block/tag
    for (int i = 0; i < blockList.size(); i++) {
      final int index = i;
      int buttonX = (this.width / 2) + (maxWidth + 8) / 2 + 4;
      this.addRenderableWidget(
          Button.builder(
                  Component.literal("X"),
                  button -> {
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
      Minecraft.getInstance()
          .setScreen(
              new ForgeBlockListScreen(parent, this.title.getString(), updatedList, configUpdater));
    } catch (Exception e) {
      LOGGER.error("Error applying config changes: {}", e.getMessage());
    }
  }

  @Override
  public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    // Use simple background without blur to avoid 1.21.8 blur frame limit issue
    graphics.fill(0, 0, this.width, this.height, 0x80000000); // Semi-transparent black

    super.render(
        graphics,
        mouseX,
        mouseY,
        partialTicks); // Renders any children components like buttons and input fields

    int yStart = 40;

    // Add instructional text above the input field
    renderInstructionalText(graphics, yStart);

    yStart += 5;

    // Render each block/tag with its remove button
    renderBlocksAndTags(graphics, yStart);
  }

  private void renderInstructionalText(@Nonnull GuiGraphics graphics, int yStart) {
    String instructionalText = "Enter block names or tags here, for example:";

    int textY = yStart - 30;
    graphics.drawString(
        this.font,
        Component.literal(instructionalText),
        this.width / 2 - this.font.width(instructionalText) / 2,
        textY,
        0xFFFFFF);

    linkX = this.width / 2 - this.font.width(minecraftTagsLink) / 2;
    linkY = textY + 10;

    graphics.drawString(this.font, Component.literal(minecraftTagsLink), linkX, linkY, 0x55FF55);
  }

  // Render blocks/tags and their respective remove buttons
  private void renderBlocksAndTags(@Nonnull GuiGraphics graphics, int yStart) {
    for (String block : blockList) {
      int boxX = (this.width / 2) - (maxWidth + 8) / 2; // Center the box horizontally

      // Draw background box
      graphics.fill(
          boxX, yStart, boxX + maxWidth + 8, yStart + this.font.lineHeight + 8, 0xFF555555);

      // Draw text with better positioning and brighter color
      int textX = boxX + 4;
      int textY = yStart + 4;

      graphics.drawString(
          this.font,
          Component.literal(block),
          textX,
          textY,
          0xFFFFFFFF); // Use full white color with alpha

      yStart += this.font.lineHeight + 10; // Move down for the next block/tag
    }
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
