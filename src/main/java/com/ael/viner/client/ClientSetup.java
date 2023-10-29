package com.ael.viner.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;


/**
 * client class for setting up key mappings and initializing block registry.
 */
public class ClientSetup {

    /**
     * Key mapping for the SHIFT key, used in gameplay controls.
     */
    public static final KeyMapping SHIFT_KEY_BINDING = new KeyMapping(
            "key.shift",
            GLFW.GLFW_KEY_LEFT_SHIFT,
            "key.categories.gameplay"
    );

    /**
     * Sets up key mappings and initializes block registry during the common setup phase.
     *
     * @param event The client setup event, triggered during mod initialization.
     */
    public static void setup(final FMLClientSetupEvent event) {

        // Add the SHIFT key mapping to the game
        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(
                Minecraft.getInstance().options.keyMappings,
                SHIFT_KEY_BINDING
        );

    }
}

