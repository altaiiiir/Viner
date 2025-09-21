package com.ael.viner.common.gui;

import com.ael.viner.common.config.IConfigManager;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.slf4j.Logger;

/**
 * Platform-agnostic configuration screen that works with any mod loader. Uses IConfigManager
 * interface to abstract away platform-specific config access.
 */
public class ConfigScreen extends Screen {

  private static final Logger LOGGER = LogUtils.getLogger();
  private final IConfigManager configManager;
  private final Consumer<String> networkSyncCallback;

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

  public ConfigScreen(IConfigManager configManager, Consumer<String> networkSyncCallback) {
    super(Component.literal("Viner Configurations"));
    this.configManager = configManager;
    this.networkSyncCallback = networkSyncCallback;
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
    boolean isVineAllEnabled = !configManager.getVineAll();
    vineableBlockListButton.active = isVineAllEnabled;
    nonVineableBlockListButton.active = isVineAllEnabled;

    boolean isShapeVineEnabled = configManager.getShapeVine();
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
            configManager.getVineableBlocks(),
            updatedList -> {
              configManager.setVineableBlocks(updatedList);
              networkSyncCallback.accept("vineableBlocks");
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
            configManager.getUnvineableBlocks(),
            updatedList -> {
              configManager.setUnvineableBlocks(updatedList);
              networkSyncCallback.accept("unvineableBlocks");
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

    yStart += stepSize * 2; // ORIGINAL: blank space after block lists

    vineAllButton =
        GuiUtils.createConfigBooleanButton(
            leftColumnX,
            yStart,
            boxWidth,
            20,
            "Vine All",
            configManager::getVineAll,
            value -> {
              configManager.setVineAll(value);
              updateBlockListButtonState();
              networkSyncCallback.accept("vineAll");
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
            configManager::getVineableLimit,
            value -> {
              configManager.setVineableLimit(value);
              networkSyncCallback.accept("vineableLimit");
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
            "Hunger Per Block", // ORIGINAL: was "Hunger Per Block", not "Exhaustion Per Block"
            configManager::getExhaustionPerBlock,
            value -> {
              configManager.setExhaustionPerBlock(value);
              networkSyncCallback.accept("exhaustionPerBlock");
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
            configManager::getShapeVine,
            value -> {
              configManager.setShapeVine(value);
              updateBlockListButtonState();
              networkSyncCallback.accept("shapeVine");
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
            configManager::getHeightAbove,
            value -> {
              configManager.setHeightAbove(value);
              networkSyncCallback.accept("heightAbove");
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
            configManager::getHeightBelow,
            value -> {
              configManager.setHeightBelow(value);
              networkSyncCallback.accept("heightBelow");
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
            configManager::getWidthLeft,
            value -> {
              configManager.setWidthLeft(value);
              networkSyncCallback.accept("widthLeft");
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
            configManager::getWidthRight,
            value -> {
              configManager.setWidthRight(value);
              networkSyncCallback.accept("widthRight");
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
            configManager::getLayerOffset,
            value -> {
              configManager.setLayerOffset(value);
              networkSyncCallback.accept("layerOffset");
            });
    this.addRenderableWidget(layerOffsetField);
  }

  private void addApplyButton() {
    this.addRenderableWidget(
        Button.builder(
                Component.literal("Apply"),
                button -> {
                  applyConfigChanges();
                })
            .pos((this.width / 2) - (boxWidth / 2), this.height - 30)
            .width(boxWidth)
            .build());
  }

  private void applyConfigChanges() {
    try {
      LOGGER.info("Applying configuration changes from GUI");
      configManager.save();
      Minecraft.getInstance().setScreen(null);
    } catch (Exception e) {
      LOGGER.error("Error applying config changes: {}", e.getMessage());
    }
  }

  @Override
  public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(graphics, mouseX, mouseY, partialTicks); // Renders the background
    super.render(graphics, mouseX, mouseY, partialTicks); // Renders widgets

    renderTooltips(graphics, mouseX - 60, mouseY - 10);

    // Centers the title text
    int titleWidth = this.font.width(this.title);
    graphics.drawString(this.font, this.title, (this.width - titleWidth) / 2, 15, 0xFFFFFF);
  }

  // Create Positioner
  ScreenRectangle s = new ScreenRectangle(0, 0, 0, 0);
  MenuTooltipPositioner positioner = new MenuTooltipPositioner(s);

  private void renderTooltips(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
    // Vineable Block List Button Tooltip
    if (vineableBlockListButton.isHoveredOrFocused()) {

      FormattedCharSequence text =
          FormattedCharSequence.forward(
              "Configure which blocks can be vined together.", Style.EMPTY);

      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    // Non-Vineable Block List Button Tooltip
    if (nonVineableBlockListButton.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward("Configure which blocks cannot be vined.", Style.EMPTY);

      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    // Shape Vine Button Tooltip
    if (shapeVineButton.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward(
              "Enables shape-based vining with custom dimensions.", Style.EMPTY);

      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    // Vine All Button Tooltip
    if (vineAllButton.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward(
              "When enabled, vines all connected blocks regardless of type.", Style.EMPTY);

      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    // Height Below Field Tooltip
    if (heightBelowField.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward(
              "Sets the vine growth limit below the source block.", Style.EMPTY);

      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    // Height Above Field Tooltip
    if (heightAboveField.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward(
              "Sets the vine growth limit above the source block.", Style.EMPTY);

      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    // Width Left Field Tooltip
    if (widthLeftField.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward(
              "Sets the vine growth limit left of the source block.", Style.EMPTY);

      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    // Width Right Field Tooltip
    if (widthRightField.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward(
              "Sets the vine growth limit right of the source block.", Style.EMPTY);

      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    // Layer Offset Field Tooltip
    if (layerOffsetField.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward(
              "Adjusts the vertical offset between each shape vined layer.", Style.EMPTY);

      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    // Vineable Limit Field Tooltip
    if (vineableLimitField.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward(
              "Limits the number of blocks that can be vineable.", Style.EMPTY);

      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    // Exhaustion Per Block Field Tooltip
    if (exhaustionPerBlockField.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward("Sets the hunger rate per vined block.", Style.EMPTY);

      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
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
