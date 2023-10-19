package com.ael.viner.common;

import com.ael.viner.registry.VinerBlockRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;


public class Common {

    public static final KeyMapping SHIFT_KEY_BINDING = new KeyMapping(
            "key.shift",
            GLFW.GLFW_KEY_LEFT_SHIFT,
            "key.categories.gameplay"
    );

    public static void setup(final FMLCommonSetupEvent event) {

        // Add keymapping to Game
        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(
                Minecraft.getInstance().options.keyMappings,
                SHIFT_KEY_BINDING
        );

        VinerBlockRegistry.setup();
    }


}
