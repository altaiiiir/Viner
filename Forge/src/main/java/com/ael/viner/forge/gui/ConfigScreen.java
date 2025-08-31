package com.ael.viner.forge.gui;

import static com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer;

import com.ael.viner.forge.config.Config;
import com.ael.viner.forge.network.packets.ConfigSyncPacket;
import com.mojang.logging.LogUtils;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ConfigScreen extends Screen {

  private static final Logger LOGGER = LogUtils.getLogger();
  private int boxWidth, padding, leftColumnX, rightColumnX, yStart, stepSize;
  private Button vineableBlockListButton,
      nonVineableBlockListButton,
      shapeVineButton,
      vineAllButton;
  private AbstractSliderButton heightBelowField,
      heightAboveField,
      widthLeftField,
      widthRightField,
      layerOffsetField,
      vineableLimitField,
      exhaustionPerBlockField;

  public ConfigScreen() {
    super(Component.literal("Viner Configurations"));
  }

  @Override
  protected void init() {
    super.init();
    calculateLayoutParameters();
    addConfigWidgets();
    addApplyButton();
    updateBlockListButtonState();
  }

  // Dynamically calculate layout parameters to avoid overlap and adjust to screen scale
  private void calculateLayoutParameters() {
    boxWidth =
        Math.min(150, this.width / 4); // Ensure box width is not too large on smaller screens
    padding = this.width / 5; // Dynamic padding based on screen width

    leftColumnX = padding;
    rightColumnX = this.width - boxWidth - padding;

    yStart = this.height / 4;
    stepSize = 30; // Adjust to fit more components
  }

  private void updateBlockListButtonState() {
    boolean isVineAllEnabled = !Config.VINE_ALL.get();
    vineableBlockListButton.active = isVineAllEnabled;
    nonVineableBlockListButton.active = isVineAllEnabled;

    boolean isShapeVineEnabled = Config.SHAPE_VINE.get();
    heightBelowField.active = isShapeVineEnabled;
    heightAboveField.active = isShapeVineEnabled;
    widthLeftField.active = isShapeVineEnabled;
    widthRightField.active = isShapeVineEnabled;
    layerOffsetField.active = isShapeVineEnabled;
  }

  private void addConfigWidgets() {
    addLeftColumnWidgets();
    yStart = this.height / 4; // Reset yStart for Right Column Widgets
    addRightColumnWidgets();
  }

  private void addRedirectButtons() {
    Screen vineableBlockListScreen =
        new BlockListScreen(
            this,
            "Vineable Block List",
            Config.VINEABLE_BLOCKS.get(),
            updatedList -> {
              Config.VINEABLE_BLOCKS.set(updatedList);
              syncConfigWithServer(
                  ConfigSyncPacket.ConfigType.BLOCK_LIST, updatedList, "vineableBlocks");
            });
    vineableBlockListButton =
        GuiUtils.createRedirectButton(
            leftColumnX, yStart, boxWidth, 20, "Vineable Block List", vineableBlockListScreen);
    this.addRenderableWidget(vineableBlockListButton);

    yStart += stepSize; // increment starting point along the y-axis

    Screen nonVineableBlockListScreen =
        new BlockListScreen(
            this,
            "Non-Vineable Block List",
            Config.UNVINEABLE_BLOCKS.get(),
            updatedList -> {
              Config.UNVINEABLE_BLOCKS.set(updatedList);
              syncConfigWithServer(
                  ConfigSyncPacket.ConfigType.BLOCK_LIST, updatedList, "unvineableBlocks");
            });
    nonVineableBlockListButton =
        GuiUtils.createRedirectButton(
            leftColumnX,
            yStart,
            boxWidth,
            20,
            "Non-Vineable Block List",
            nonVineableBlockListScreen);
    this.addRenderableWidget(nonVineableBlockListButton);
  }

  private void addLeftColumnWidgets() {
    addRedirectButtons();

    yStart += stepSize * 2;

    vineAllButton =
        GuiUtils.createConfigBooleanButton(
            leftColumnX,
            yStart,
            boxWidth,
            20,
            "Vine All",
            Config.VINE_ALL,
            newValue -> {
              Config.VINE_ALL.set(newValue);
              syncConfigWithServer(ConfigSyncPacket.ConfigType.BOOLEAN, newValue, "vineAll");
              updateBlockListButtonState();
            });
    this.addRenderableWidget(vineAllButton);

    yStart += stepSize;

    vineableLimitField =
        GuiUtils.createConfigSlider(
            leftColumnX,
            yStart,
            boxWidth,
            20,
            0,
            500,
            "Vineable Limit",
            Config.VINEABLE_LIMIT,
            newValue -> {
              Config.VINEABLE_LIMIT.set(newValue);
              syncConfigWithServer(ConfigSyncPacket.ConfigType.INT, newValue, "vineableLimit");
            });
    this.addRenderableWidget(vineableLimitField);

    yStart += stepSize;

    exhaustionPerBlockField =
        GuiUtils.createConfigSlider(
            leftColumnX,
            yStart,
            boxWidth,
            20,
            20,
            "Hunger Per Block",
            Config.EXHAUSTION_PER_BLOCK,
            newValue -> {
              Config.EXHAUSTION_PER_BLOCK.set(newValue);
              syncConfigWithServer(
                  ConfigSyncPacket.ConfigType.DOUBLE, newValue, "exhaustionPerBlock");
            });
    this.addRenderableWidget(exhaustionPerBlockField);
  }

  private void addRightColumnWidgets() {
    shapeVineButton =
        GuiUtils.createConfigBooleanButton(
            rightColumnX,
            yStart,
            boxWidth,
            20,
            "Shape Vine",
            Config.SHAPE_VINE,
            newValue -> {
              Config.SHAPE_VINE.set(newValue);
              syncConfigWithServer(ConfigSyncPacket.ConfigType.BOOLEAN, newValue, "shapeVine");
              updateBlockListButtonState();
            });
    this.addRenderableWidget(shapeVineButton);

    yStart += stepSize;

    // Height Above Slider
    heightAboveField =
        GuiUtils.createConfigSlider(
            rightColumnX,
            yStart,
            boxWidth,
            20,
            0,
            100,
            "Height Above",
            Config.HEIGHT_ABOVE,
            newValue -> {
              Config.HEIGHT_ABOVE.set(newValue);
              syncConfigWithServer(ConfigSyncPacket.ConfigType.INT, newValue, "heightAbove");
            });
    this.addRenderableWidget(heightAboveField);

    yStart += stepSize;

    // Height Below Slider
    heightBelowField =
        GuiUtils.createConfigSlider(
            rightColumnX,
            yStart,
            boxWidth,
            20,
            0,
            100,
            "Height Below",
            Config.HEIGHT_BELOW,
            newValue -> {
              Config.HEIGHT_BELOW.set(newValue);
              syncConfigWithServer(ConfigSyncPacket.ConfigType.INT, newValue, "heightBelow");
            });
    this.addRenderableWidget(heightBelowField);

    yStart += stepSize;

    // Width Left Slider
    widthLeftField =
        GuiUtils.createConfigSlider(
            rightColumnX,
            yStart,
            boxWidth,
            20,
            0,
            100,
            "Width Left",
            Config.WIDTH_LEFT,
            newValue -> {
              Config.WIDTH_LEFT.set(newValue);
              syncConfigWithServer(ConfigSyncPacket.ConfigType.INT, newValue, "widthLeft");
            });
    this.addRenderableWidget(widthLeftField);

    yStart += stepSize;

    // Width Right Slider
    widthRightField =
        GuiUtils.createConfigSlider(
            rightColumnX,
            yStart,
            boxWidth,
            20,
            0,
            100,
            "Width Right",
            Config.WIDTH_RIGHT,
            newValue -> {
              Config.WIDTH_RIGHT.set(newValue);
              syncConfigWithServer(ConfigSyncPacket.ConfigType.INT, newValue, "widthRight");
            });
    this.addRenderableWidget(widthRightField);

    yStart += stepSize;

    // Layer Offset Slider
    layerOffsetField =
        GuiUtils.createConfigSlider(
            rightColumnX,
            yStart,
            boxWidth,
            20,
            -64,
            256,
            "Layer Offset",
            Config.LAYER_OFFSET,
            newValue -> {
              Config.LAYER_OFFSET.set(newValue);
              syncConfigWithServer(ConfigSyncPacket.ConfigType.INT, newValue, "layerOffset");
            });
    this.addRenderableWidget(layerOffsetField);
  }

  private void addApplyButton() {
    this.addRenderableWidget(
        Button.builder(Component.literal("Apply"), button -> applyConfigChanges())
            .pos(this.width / 2 - boxWidth / 2, this.height - 50)
            .width(boxWidth)
            .build());
  }

  private void applyConfigChanges() {
    try {
      LOGGER.info("Applying configuration changes from GUI");

      Minecraft.getInstance().setScreen(null);
    } catch (NumberFormatException e) {
      LOGGER.error("Error applying config changes: {}", e.getMessage());
    }
  }

  @Override
  public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(graphics); // Renders the background
    super.render(graphics, mouseX, mouseY, partialTicks); // Renders widgets

    renderTooltips(graphics, mouseX - 60, mouseY - 10);

    // Centers the title text
    int titleWidth = this.font.width(this.title);
    graphics.drawString(this.font, this.title, (this.width - titleWidth) / 2, 15, 0xFFFFFF);
  }

  private void renderTooltips(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
    // Shape Vine Button Tooltip
    if (shapeVineButton.isHoveredOrFocused()) {
      graphics.renderTooltip(
          this.font,
          Component.literal("Enables shaping vines according to specific dimensions."),
          mouseX,
          mouseY);
    }

    // Mineable Block List Button Tooltip
    if (vineableBlockListButton.isHoveredOrFocused()) {
      graphics.renderTooltip(
          this.font, Component.literal("Configure which blocks are vineable."), mouseX, mouseY);
    }

    // Non-Mineable Block List Button Tooltip
    if (nonVineableBlockListButton.isHoveredOrFocused()) {
      graphics.renderTooltip(
          this.font, Component.literal("Configure which blocks are non-vineable."), mouseX, mouseY);
    }

    // Vine All Button Tooltip
    if (vineAllButton.isHoveredOrFocused()) {
      graphics.renderTooltip(
          this.font,
          Component.literal("Toggle to vine all blocks without exceptions."),
          mouseX,
          mouseY);
    }

    // Height Below Field Tooltip
    if (heightBelowField.isHoveredOrFocused()) {
      graphics.renderTooltip(
          this.font,
          Component.literal("Sets the vine growth limit below the source block."),
          mouseX,
          mouseY);
    }

    // Height Above Field Tooltip
    if (heightAboveField.isHoveredOrFocused()) {
      graphics.renderTooltip(
          this.font,
          Component.literal("Sets the vine growth limit above the source block."),
          mouseX,
          mouseY);
    }

    // Width Left Field Tooltip
    if (widthLeftField.isHoveredOrFocused()) {
      graphics.renderTooltip(
          this.font,
          Component.literal("Sets the vine growth limit left of the source block."),
          mouseX,
          mouseY);
    }

    // Width Right Field Tooltip
    if (widthRightField.isHoveredOrFocused()) {
      graphics.renderTooltip(
          this.font,
          Component.literal("Sets the vine growth limit right of the source block."),
          mouseX,
          mouseY);
    }

    // Layer Offset Field Tooltip
    if (layerOffsetField.isHoveredOrFocused()) {
      graphics.renderTooltip(
          this.font,
          Component.literal("Adjusts the vertical offset between each shape vined layer."),
          mouseX,
          mouseY);
    }

    // Vineable Limit Field Tooltip
    if (vineableLimitField.isHoveredOrFocused()) {
      graphics.renderTooltip(
          this.font,
          Component.literal("Limits the number of blocks that can be vineable."),
          mouseX,
          mouseY);
    }

    // Exhaustion Per Block Field Tooltip
    if (exhaustionPerBlockField.isHoveredOrFocused()) {
      graphics.renderTooltip(
          this.font, Component.literal("Sets the hunger rate per vined block."), mouseX, mouseY);
    }
  }

  @Override
  public void onClose() {
    super.onClose();
    // save config on close or apply temporary changes here
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
