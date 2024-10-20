package com.ael.viner.network;

import com.ael.viner.network.packets.ConfigSyncPacket;
import com.ael.viner.network.packets.MouseScrollPacket;
import com.ael.viner.network.packets.VeinMiningPacket;
import com.ael.viner.network.packets.VinerKeyPressedPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Handles packet registration and networking channel setup for the Viner mod.
 */
public class VinerPacketHandler {

    /**
     * Networking channel instance for sending and receiving packets.
     */
    public static Object INSTANCE;

    /**
     * This method registers all packets based on the Minecraft version.
     */
    public static void register() {
        try {
            if (classExists("net.minecraftforge.network.PayloadChannel")) {
                // For 1.21.x or later
                registerUsingPayloadChannel();
            } else {
                // For 1.20.x
                registerUsingSimpleChannel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object getNetworkInstance() {
        try {
            // Fetch the NetworkInstance dynamically (adjust this based on the actual API)
            Class<?> networkInstanceClass = Class.forName("net.minecraftforge.network.NetworkInstance");
            Constructor<?> constructor = networkInstanceClass.getDeclaredConstructor();
            return constructor.newInstance(); // Assuming it has a no-arg constructor
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void registerUsingSimpleChannel() {
        try {
            Class<?> simpleChannelClass = Class.forName("net.minecraftforge.network.simple.SimpleChannel");
            Class<?> networkRegistryClass = Class.forName("net.minecraftforge.network.NetworkRegistry");

            // Check if 1.21.x uses SimpleChannel or an alternative approach
            Method newSimpleChannelMethod = networkRegistryClass.getDeclaredMethod("newSimpleChannel", createResourceLocation("", "").getClass(), Supplier.class, Supplier.class, Supplier.class);

            String protocolVersion = "1";
            Supplier<String> protocolSupplier = () -> protocolVersion;

            Object resourceLocation = createResourceLocation("viner", "main");

            Object simpleChannel = newSimpleChannelMethod.invoke(null, resourceLocation, protocolSupplier, protocolSupplier, protocolSupplier);

            Method registerMessageMethod = simpleChannel.getClass().getMethod("registerMessage", int.class, Class.class, Object.class, Object.class, Object.class);

            int id = 0;
            registerMessageMethod.invoke(simpleChannel, id++, VeinMiningPacket.class,
                    (BiConsumer<VeinMiningPacket, FriendlyByteBuf>) VeinMiningPacket::encode,
                    (Function<FriendlyByteBuf, VeinMiningPacket>) buf -> VeinMiningPacket.decode(buf, VeinMiningPacket.FACTORY),
                    (BiConsumer<VeinMiningPacket, Supplier<Object>>) (packet, ctx) -> packet.handle(packet, ctx));

            registerMessageMethod.invoke(simpleChannel, id++, VinerKeyPressedPacket.class,
                    (BiConsumer<VinerKeyPressedPacket, FriendlyByteBuf>) VinerKeyPressedPacket::encode,
                    (Function<FriendlyByteBuf, VinerKeyPressedPacket>) buf -> VinerKeyPressedPacket.decode(buf, VinerKeyPressedPacket.FACTORY),
                    (BiConsumer<VinerKeyPressedPacket, Supplier<Object>>) (packet, ctx) -> packet.handle(packet, ctx));

            registerMessageMethod.invoke(simpleChannel, id++, MouseScrollPacket.class,
                    (BiConsumer<MouseScrollPacket, FriendlyByteBuf>) MouseScrollPacket::encode,
                    (Function<FriendlyByteBuf, MouseScrollPacket>) buf -> MouseScrollPacket.decode(buf, MouseScrollPacket.FACTORY),
                    (BiConsumer<MouseScrollPacket, Supplier<Object>>) (packet, ctx) -> packet.handle(packet, ctx));

            registerMessageMethod.invoke(simpleChannel, id++, ConfigSyncPacket.class,
                    (BiConsumer<ConfigSyncPacket, FriendlyByteBuf>) ConfigSyncPacket::encode,
                    (Function<FriendlyByteBuf, ConfigSyncPacket>) buf -> ConfigSyncPacket.decode(buf, ConfigSyncPacket.FACTORY),
                    (BiConsumer<ConfigSyncPacket, Supplier<Object>>) (packet, ctx) -> packet.handle(packet, ctx));

            // Set the instance to the simpleChannel
            INSTANCE = simpleChannel;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static Object createResourceLocation(String namespace, String path) throws Exception {
        Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
        Constructor<?> constructor;
        try {
            constructor = resourceLocationClass.getDeclaredConstructor(String.class, String.class);
            return constructor.newInstance(namespace, path);
        } catch (NoSuchMethodException e) {
            Method ofMethod = resourceLocationClass.getDeclaredMethod("of", String.class, String.class);
            return ofMethod.invoke(null, namespace, path);
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
