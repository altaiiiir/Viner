package com.ael.viner.forge.gui;

// import com.ael.viner.common.gui.BlockListScreen; // Temporarily removed to avoid cross-module
// issues
import com.ael.viner.forge.config.ForgeConfigManager;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Forge-specific configuration screen. Contains all UI logic in Forge module to avoid cross-module
 * classloader issues in 1.21.8.
 */
@OnlyIn(Dist.CLIENT)
public class ForgeConfigScreen extends Screen {

  private final ForgeConfigManager configManager;

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

  public ForgeConfigScreen() {
    super(Component.translatable("screen.viner.config"));
    this.configManager = new ForgeConfigManager();
  }

  @Override
  protected void init() {
    super.init();
    calculateLayout();
    addConfigurationWidgets();
    addRedirectButtons();
    addApplyButton();
  }

  private void calculateLayout() {
    boxWidth = 120;
    padding = 10;
    leftColumnX = (width - (2 * boxWidth + padding)) / 2;
    rightColumnX = leftColumnX + boxWidth + padding;
    yStart = 50;
    stepSize = 30;
  }

  private void addConfigurationWidgets() {
    // Right column rows (explicit placement)
    // Row 0: Shape Vine
    shapeVineButton =
        createToggleButton(
            rightColumnX,
            yStart + 0 * stepSize,
            "Shape Vine",
            configManager::getShapeVine,
            value -> {
              configManager.setShapeVine(value);
            });
    addRenderableWidget(shapeVineButton);

    // Row 1: Height Above
    heightAboveField =
        createSlider(
            rightColumnX,
            yStart + 1 * stepSize,
            "Height Above",
            configManager.getHeightAbove(),
            0,
            100,
            value -> {
              configManager.setHeightAbove(value);
            });
    addRenderableWidget(heightAboveField);

    // Row 2: Height Below
    heightBelowField =
        createSlider(
            rightColumnX,
            yStart + 2 * stepSize,
            "Height Below",
            configManager.getHeightBelow(),
            0,
            100,
            value -> {
              configManager.setHeightBelow(value);
            });
    addRenderableWidget(heightBelowField);

    // Row 3: Width Left
    widthLeftField =
        createSlider(
            rightColumnX,
            yStart + 3 * stepSize,
            "Width Left",
            configManager.getWidthLeft(),
            0,
            100,
            value -> {
              configManager.setWidthLeft(value);
            });
    addRenderableWidget(widthLeftField);

    // Row 4: Width Right
    widthRightField =
        createSlider(
            rightColumnX,
            yStart + 4 * stepSize,
            "Width Right",
            configManager.getWidthRight(),
            0,
            100,
            value -> {
              configManager.setWidthRight(value);
            });
    addRenderableWidget(widthRightField);

    // Row 5: Layer Offset
    layerOffsetField =
        createSlider(
            rightColumnX,
            yStart + 5 * stepSize,
            "Layer Offset",
            configManager.getLayerOffset(),
            -64,
            256,
            value -> {
              configManager.setLayerOffset(value);
            });
    addRenderableWidget(layerOffsetField);

    // Left column rows (explicit placement)
    // Row 3: Vine All (leave a blank row after Non-Vineable Block List)
    vineAllButton =
        createToggleButton(
            leftColumnX,
            yStart + 3 * stepSize,
            "Vine All",
            configManager::getVineAll,
            value -> {
              configManager.setVineAll(value);
            });
    addRenderableWidget(vineAllButton);

    // Row 4: Vineable Limit
    vineableLimitField =
        createSlider(
            leftColumnX,
            yStart + 4 * stepSize,
            "Vineable Limit",
            configManager.getVineableLimit(),
            1,
            500,
            value -> {
              configManager.setVineableLimit(value);
            });
    addRenderableWidget(vineableLimitField);

    // Row 5: Hunger Per Block
    exhaustionPerBlockField =
        createSlider(
            leftColumnX,
            yStart + 5 * stepSize,
            "Hunger Per Block",
            (int) (configManager.getExhaustionPerBlock() * 100),
            0,
            1000,
            value -> {
              // Convert from percentage back to double (value is 0-1000, config expects 0.0-10.0)
              double exhaustionValue = value / 100.0;
              configManager.setExhaustionPerBlock(exhaustionValue);
            });
    addRenderableWidget(exhaustionPerBlockField);
  }

  private void addRedirectButtons() {
    // Row 0: Vineable Block List (left)
    vineableBlockListButton =
        Button.builder(
                Component.literal("Vineable Block List"),
                button -> {
                  // Debug: Check what we're getting from the config
                  List<String> vineableBlocks = configManager.getVineableBlocks();
                  System.out.println(
                      "DEBUG: Opening vineable block list, found "
                          + vineableBlocks.size()
                          + " entries:");
                  for (String block : vineableBlocks) {
                    System.out.println("  - '" + block + "' (length: " + block.length() + ")");
                  }

                  // Create and open a Forge-specific block list screen (within same classloader)
                  ForgeBlockListScreen blockListScreen =
                      new ForgeBlockListScreen(
                          this,
                          "Vineable Block List",
                          vineableBlocks,
                          updatedList -> {
                            configManager.setVineableBlocks(updatedList);
                          });
                  minecraft.setScreen(blockListScreen);
                })
            .bounds(leftColumnX, yStart + 0 * stepSize, boxWidth, 20)
            .build();
    addRenderableWidget(vineableBlockListButton);

    // Row 1: Non-Vineable Block List (left)
    nonVineableBlockListButton =
        Button.builder(
                Component.literal("Non-Vineable Block List"),
                button -> {
                  // Create and open a Forge-specific block list screen (within same classloader)
                  ForgeBlockListScreen blockListScreen =
                      new ForgeBlockListScreen(
                          this,
                          "Non-Vineable Block List",
                          configManager.getUnvineableBlocks(),
                          updatedList -> {
                            configManager.setUnvineableBlocks(updatedList);
                          });
                  minecraft.setScreen(blockListScreen);
                })
            .bounds(leftColumnX, yStart + 1 * stepSize, boxWidth, 20)
            .build();
    addRenderableWidget(nonVineableBlockListButton);
  }

  private void addApplyButton() {
    Button applyButton =
        Button.builder(
                Component.literal("Apply"),
                button -> {
                  // Force refresh the player registry to pick up config changes
                  refreshPlayerData();
                  onClose();
                })
            .bounds((width - 60) / 2, height - 30, 60, 20)
            .build();
    addRenderableWidget(applyButton);
  }

  private void refreshPlayerData() {
    try {
      // Sync config changes with server via network packets
      syncConfigChangesWithServer();

      // Refresh all player data to pick up the new config
      var instance = com.ael.viner.forge.VinerForge.getInstance();
      if (instance != null
          && instance.getPlayerRegistry()
              instanceof com.ael.viner.forge.registry.VinerPlayerRegistry registry) {
        registry.refreshAllPlayers();
        System.out.println("[DEBUG] Player data refreshed after Apply button clicked");
      }
    } catch (Exception e) {
      System.err.println("[ERROR] Failed to refresh player data: " + e.getMessage());
    }
  }

  private void syncConfigChangesWithServer() {
    // Sync all config values with server to ensure changes are applied

    // Block lists
    com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer(
        com.ael.viner.forge.network.packets.ConfigSyncPacket.ConfigType.BLOCK_LIST,
        configManager.getVineableBlocks(),
        "vineableBlocks");

    com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer(
        com.ael.viner.forge.network.packets.ConfigSyncPacket.ConfigType.BLOCK_LIST,
        configManager.getUnvineableBlocks(),
        "unvineableBlocks");

    // Boolean values
    com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer(
        com.ael.viner.forge.network.packets.ConfigSyncPacket.ConfigType.BOOLEAN,
        configManager.getVineAll(),
        "vineAll");

    com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer(
        com.ael.viner.forge.network.packets.ConfigSyncPacket.ConfigType.BOOLEAN,
        configManager.getShapeVine(),
        "shapeVine");

    // Integer values
    com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer(
        com.ael.viner.forge.network.packets.ConfigSyncPacket.ConfigType.INT,
        configManager.getVineableLimit(),
        "vineableLimit");

    com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer(
        com.ael.viner.forge.network.packets.ConfigSyncPacket.ConfigType.INT,
        configManager.getHeightAbove(),
        "heightAbove");

    com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer(
        com.ael.viner.forge.network.packets.ConfigSyncPacket.ConfigType.INT,
        configManager.getHeightBelow(),
        "heightBelow");

    com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer(
        com.ael.viner.forge.network.packets.ConfigSyncPacket.ConfigType.INT,
        configManager.getWidthLeft(),
        "widthLeft");

    com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer(
        com.ael.viner.forge.network.packets.ConfigSyncPacket.ConfigType.INT,
        configManager.getWidthRight(),
        "widthRight");

    com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer(
        com.ael.viner.forge.network.packets.ConfigSyncPacket.ConfigType.INT,
        configManager.getLayerOffset(),
        "layerOffset");

    // Double values
    com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer(
        com.ael.viner.forge.network.packets.ConfigSyncPacket.ConfigType.DOUBLE,
        configManager.getExhaustionPerBlock(),
        "exhaustionPerBlock");

    System.out.println("[DEBUG] Config changes synced with server");
  }

  private Button createToggleButton(
      int x, int y, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
    return Button.builder(
            Component.literal(label + ": " + (getter.get() ? "Enabled" : "Disabled")),
            button -> {
              boolean newValue = !getter.get();
              setter.accept(newValue);
              button.setMessage(
                  Component.literal(label + ": " + (newValue ? "Enabled" : "Disabled")));
              System.out.println("[DEBUG] " + label + " set to: " + newValue);
            })
        .bounds(x, y, boxWidth, 20)
        .build();
  }

  private AbstractSliderButton createSlider(
      int x, int y, String label, int currentValue, int min, int max, Consumer<Integer> onChange) {
    return new AbstractSliderButton(
        x,
        y,
        boxWidth,
        20,
        Component.literal(label + ": " + currentValue),
        (double) (currentValue - min) / (max - min)) {

      @Override
      protected void updateMessage() {
        int value = (int) (min + this.value * (max - min));
        setMessage(Component.literal(label + ": " + value));
      }

      @Override
      protected void applyValue() {
        int value = (int) (min + this.value * (max - min));
        onChange.accept(value);
      }
    };
  }

  @Override
  public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    // Use simple background without blur to avoid 1.21.8 blur frame limit issue
    guiGraphics.fill(0, 0, this.width, this.height, 0x80000000); // Semi-transparent black

    // Draw title
    guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

    // Render all widgets
    super.render(guiGraphics, mouseX, mouseY, partialTick);

    // Render tooltips
    renderTooltips(guiGraphics, mouseX, mouseY);
  }

  private void renderTooltips(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
    var positioner = new MenuTooltipPositioner(new ScreenRectangle(0, 0, width, height));

    // Add tooltips for each widget
    if (vineAllButton.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward("Enables vining for all blocks.", Style.EMPTY);
      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    if (shapeVineButton.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward("Enables shape-aware vining.", Style.EMPTY);
      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

    if (vineableLimitField.isHoveredOrFocused()) {
      FormattedCharSequence text =
          FormattedCharSequence.forward("Maximum number of blocks to vine at once.", Style.EMPTY);
      graphics.renderTooltip(
          this.font,
          List.of(ClientTooltipComponent.create(text)),
          mouseX,
          mouseY,
          positioner,
          null);
    }

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
  public boolean isPauseScreen() {
    return false;
  }
}
