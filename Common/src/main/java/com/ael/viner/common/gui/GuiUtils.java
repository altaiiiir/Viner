package com.ael.viner.common.gui;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiUtils {

  public static Button createRedirectButton(
      int x, int y, int width, int height, String label, Screen newScreen) {
    return Button.builder(
            Component.literal(label),
            button -> {
              Minecraft.getInstance().setScreen(newScreen);
            })
        .pos(x, y)
        .width(width)
        .build();
  }

  public static Button createConfigBooleanButton(
      int x,
      int y,
      int width,
      int height,
      String label,
      Supplier<Boolean> getter,
      Consumer<Boolean> setter) {
    // Initial state of the boolean config
    boolean initialValue = getter.get();

    // Create and return the button
    return Button.builder(
            Component.literal(label + ": " + (initialValue ? "Enabled" : "Disabled")),
            button -> {
              // Toggle the state
              boolean newValue = !getter.get();
              setter.accept(newValue); // Update the config value

              // Update button text to reflect the new state
              button.setMessage(
                  Component.literal(label + ": " + (newValue ? "Enabled" : "Disabled")));
            })
        .pos(x, y)
        .width(width)
        .build();
  }

  public static AbstractSliderButton createConfigSlider(
      int x,
      int y,
      int width,
      int height,
      double upperLimit,
      String label,
      Supplier<Double> getter,
      Consumer<Double> setter) {
    return new AbstractSliderButton(
        x, y, width, height, Component.empty(), getter.get() / upperLimit) {
      {
        this.value = getter.get() / upperLimit; // Normalize value
        updateMessage();
      }

      @Override
      protected void updateMessage() {
        setMessage(
            Component.literal(label + ": " + String.format("%.2f", this.value * upperLimit)));
      }

      @Override
      protected void applyValue() {
        setter.accept(this.value * upperLimit); // Apply value, denormalize it back
      }
    };
  }

  public static AbstractSliderButton createConfigSlider(
      int x,
      int y,
      int width,
      int height,
      int lowerLimit,
      int upperLimit,
      String label,
      Supplier<Integer> getter,
      Consumer<Integer> setter) {
    return new AbstractSliderButton(
        x, y, width, height, Component.empty(), getter.get() / (double) upperLimit) {
      {
        this.value =
            (getter.get() - lowerLimit) / (double) (upperLimit - lowerLimit); // Normalize value
        updateMessage();
      }

      @Override
      protected void updateMessage() {
        setMessage(
            Component.literal(
                label + ": " + (int) (this.value * (upperLimit - lowerLimit) + lowerLimit)));
      }

      @Override
      protected void applyValue() {
        setter.accept(
            (int)
                (this.value * (upperLimit - lowerLimit)
                    + lowerLimit)); // Apply value, denormalize it back
      }
    };
  }
}
