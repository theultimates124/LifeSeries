package net.mat0u5.lifeseries.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.mat0u5.lifeseries.network.NetworkHandlerClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ClientKeybinds {
    private static KeyBinding superpower;
    public static void tick() {
        while (superpower.wasPressed()) {
            NetworkHandlerClient.pressSuperpowerKey();
        }
    }
    public static void registerKeybinds() {
        superpower = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lifeseries.superpower",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key.categories.lifeseries"));
    }
}
