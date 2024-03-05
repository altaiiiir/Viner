package com.ael.viner.gui;

import com.ael.viner.config.Config;
import com.ael.viner.registry.VinerBlockRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class ConfigScreen extends Screen {

    private static final Logger LOGGER = LogUtils.getLogger();
    private int boxWidth, padding, leftColumnX, rightColumnX, yStart, stepSize;

    public ConfigScreen() {
        super(Component.literal("VINER CONFIGURATIONS"));
    }

    @Override
    protected void init() {
        super.init();
        calculateLayoutParameters();
        addConfigWidgets();
        addApplyButton();
    }

    private void calculateLayoutParameters() {
        boxWidth = 150;
        padding = 100;

        leftColumnX = padding;

        rightColumnX = this.width - boxWidth - padding;

        yStart = this.height / 4;
        stepSize = 30; // Vertical space between each widget
    }

    private void addConfigWidgets() {
        // Left Column Widgets
        addLeftColumnWidgets();

        // Reset yStart for Right Column Widgets
        yStart = this.height / 4;

        // Right Column Widgets
        addRightColumnWidgets();
    }

    private void addRedirectButtons() {
        Screen mineableBlockListScreen = new BlockListScreen(this,  "Mineable Block List", Config.VINEABLE_BLOCKS.get(), updatedList -> Config.VINEABLE_BLOCKS.set(new ArrayList<>(updatedList)));
        this.addRenderableWidget(GuiUtils.createRedirectButton(leftColumnX, yStart, boxWidth, 20, "Mineable Block List", mineableBlockListScreen));

        yStart += stepSize;

        Screen nonMineableBlockListScreen = new BlockListScreen(this, "Non-Mineable Block List", Config.UNVINEABLE_BLOCKS.get(), updatedList -> Config.UNVINEABLE_BLOCKS.set(new ArrayList<>(updatedList)));
        this.addRenderableWidget(GuiUtils.createRedirectButton(leftColumnX, yStart, boxWidth, 20, "Non-Mineable Block List", nonMineableBlockListScreen));
    }

    private void addLeftColumnWidgets() {
        addRedirectButtons();

        yStart += stepSize * 2;

        Button vineAllButton = GuiUtils.createConfigBooleanButton(leftColumnX, yStart, boxWidth, 20, "Vine All", Config.VINE_ALL, Config.VINE_ALL::set);
        this.addRenderableWidget(vineAllButton);

        yStart += stepSize;

        AbstractSliderButton vineableLimitField = GuiUtils.createConfigSlider(leftColumnX, yStart, boxWidth, 20, 100, "Vineable Limit", Config.VINEABLE_LIMIT, Config.VINEABLE_LIMIT::set);
        this.addRenderableWidget(vineableLimitField);

        yStart += stepSize;

        AbstractSliderButton exhaustionPerBlockField = GuiUtils.createConfigSlider(leftColumnX, yStart, boxWidth, 20, 100, "Exhaustion Per Block", Config.EXHAUSTION_PER_BLOCK, Config.EXHAUSTION_PER_BLOCK::set);
        this.addRenderableWidget(exhaustionPerBlockField);
    }

    private void addRightColumnWidgets() {
        Button shapeVineButton = GuiUtils.createConfigBooleanButton(rightColumnX, yStart, boxWidth, 20, "Shape Vine", Config.SHAPE_VINE, Config.SHAPE_VINE::set);
        this.addRenderableWidget(shapeVineButton);

        yStart += stepSize;

        AbstractSliderButton heightBelowField = GuiUtils.createConfigSlider(rightColumnX, yStart, boxWidth, 20, 100, "Height Below", Config.HEIGHT_BELOW, Config.HEIGHT_BELOW::set);
        this.addRenderableWidget(heightBelowField);

        yStart += stepSize;

        AbstractSliderButton heightAboveField = GuiUtils.createConfigSlider(rightColumnX, yStart, boxWidth, 20, 100, "Height Above", Config.HEIGHT_ABOVE, Config.HEIGHT_ABOVE::set);
        this.addRenderableWidget(heightAboveField);

        yStart += stepSize;

        AbstractSliderButton widthLeftField = GuiUtils.createConfigSlider(rightColumnX, yStart, boxWidth, 20, 100, "Width Left", Config.WIDTH_LEFT, Config.WIDTH_LEFT::set);
        this.addRenderableWidget(widthLeftField);

        yStart += stepSize;

        AbstractSliderButton widthRightField = GuiUtils.createConfigSlider(rightColumnX, yStart, boxWidth, 20, 100, "Width Right", Config.WIDTH_RIGHT, Config.WIDTH_RIGHT::set);
        this.addRenderableWidget(widthRightField);

        yStart += stepSize;

        AbstractSliderButton layerOffsetField = GuiUtils.createConfigSlider(rightColumnX, yStart, boxWidth, 20, 100, "Layer Offset", Config.LAYER_OFFSET, Config.LAYER_OFFSET::set);
        this.addRenderableWidget(layerOffsetField);
    }

    private void addApplyButton() {
        this.addRenderableWidget(Button.builder(Component.literal("Apply"), button -> applyConfigChanges())
                .pos(this.width / 2 - 100, this.height - 30)
                .width(200)
                .build());
    }

    private void applyConfigChanges() {
        try {
            VinerBlockRegistry.setup();
            Minecraft.getInstance().setScreen(null);
        } catch (NumberFormatException e) {
            LOGGER.error("Error applying config changes: {}", e.getMessage());
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics); // Renders the background
        super.render(graphics, mouseX, mouseY, partialTicks); // Renders any children components

        // Centers the title text
        int titleWidth = this.font.width(this.title);
        graphics.drawString(this.font, this.title, (this.width - titleWidth) / 2, 15, 0xFFFFFF);
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
