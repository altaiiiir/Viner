package com.ael.viner.gui;

import com.ael.viner.config.Config;
import com.ael.viner.registry.VinerBlockRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ConfigScreen extends Screen {

    private static final Logger LOGGER = LogUtils.getLogger();
    private AbstractSliderButton vineableLimitField, heightBelowField, heightAboveField, widthLeftField, widthRightField;
    private Button vineAllButton;

    public ConfigScreen() {
        super(Component.literal("Viner Config"));
    }
    @Override
    protected void init() {
        super.init();
        int leftColumnX = this.width / 4 - 100;
        int rightColumnX = this.width * 3 / 4 - 100;
        int yStart = this.height / 4;
        int stepSize = 30; // Vertical space between each widget

        // Left Column for Other Configs
        this.vineableLimitField = createConfigSlider(leftColumnX, yStart, 200, 20, 100, "Vineable Limit", Config.VINEABLE_LIMIT, Config.VINEABLE_LIMIT::set);
        this.addRenderableWidget(this.vineableLimitField);
        yStart += stepSize; // Move Y position down for the next widget

        this.vineAllButton = createConfigBooleanButton(leftColumnX, yStart, 200, 20, "Vine All", Config.VINE_ALL, Config.VINE_ALL::set);
        this.addRenderableWidget(this.vineAllButton);

        // Reset yStart for the right column or use a separate variable
        yStart = this.height / 4; // If you want to start at the same initial Y for the right column
        // Right Column for Shape Vine Configs
        this.heightBelowField = createConfigSlider(rightColumnX, yStart, 200, 20, 100, "Height Below", Config.HEIGHT_BELOW, Config.HEIGHT_BELOW::set);
        this.addRenderableWidget(this.heightBelowField);
        yStart += stepSize;

        this.heightAboveField = createConfigSlider(rightColumnX, yStart, 200, 20, 100, "Height Above", Config.HEIGHT_ABOVE, Config.HEIGHT_ABOVE::set);
        this.addRenderableWidget(this.heightAboveField);
        yStart += stepSize;

        this.widthLeftField = createConfigSlider(rightColumnX, yStart, 200, 20, 100, "Width Left", Config.WIDTH_LEFT, Config.WIDTH_LEFT::set);
        this.addRenderableWidget(this.widthLeftField);
        yStart += stepSize;

        this.widthRightField = createConfigSlider(rightColumnX, yStart, 200, 20, 100, "Width Right", Config.WIDTH_RIGHT, Config.WIDTH_RIGHT::set);
        this.addRenderableWidget(this.widthRightField);

        // Single Apply Button (Consider one for each column if configs are independent)
        Button applyButton = Button.builder(Component.literal("Apply"), button -> applyConfigChanges())
                .pos(this.width / 2 - 100, this.height - 30)
                .width(200)
                .build();
        this.addRenderableWidget(applyButton);
    }

    private void applyConfigChanges() {
        try {
            // reinitialize the new config
            VinerBlockRegistry.setup();

            // Close the screen after applying changes
            assert this.minecraft != null;
            this.minecraft.setScreen(null);
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
    private Button createConfigBooleanButton(int x, int y, int width, int height, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        // Initial state of the boolean config
        boolean initialValue = getter.get();

        // Create and return the button
        return Button.builder(Component.literal(label + ": " + (initialValue ? "Enabled" : "Disabled")), button -> {
                    // Toggle the state
                    boolean newValue = !getter.get();
                    setter.accept(newValue); // Update the config value

                    // Update button text to reflect the new state
                    button.setMessage(Component.literal(label + ": " + (newValue ? "Enabled" : "Disabled")));
                })
                .pos(x, y)
                .width(width)
                .build();
    }


    private AbstractSliderButton createConfigSlider(int x, int y, int width, int height, int upperLimit, String label, Supplier<Integer> getter, Consumer<Integer> setter) {
        return new AbstractSliderButton(x, y, width, height, Component.empty(), getter.get() / (double) upperLimit) {
            {
                this.value = getter.get() / (double) upperLimit; // Normalize value (assuming 0-100 range for slider)
                updateMessage();
            }

            @Override
            protected void updateMessage() {
                setMessage(Component.literal(label + ": " + (int) (this.value * upperLimit)));
            }

            @Override
            protected void applyValue() {
                setter.accept((int) (this.value * upperLimit)); // Apply value, denormalize it back
            }
        };
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
