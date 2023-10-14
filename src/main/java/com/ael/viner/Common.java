package com.ael.viner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


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
