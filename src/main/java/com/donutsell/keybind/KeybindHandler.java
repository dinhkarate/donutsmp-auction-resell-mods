package com.donutsell.keybind;

import com.donutsell.config.DonutSellConfig;
import com.donutsell.task.SellTaskManager;
import com.donutsell.util.ChatUtils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Handles the configurable keybind for toggling auto-sell on/off.
 * Default key: Backslash (\)
 */
public class KeybindHandler {
    private KeyBinding toggleKeybind;
    private final SellTaskManager taskManager;
    private final DonutSellConfig config;

    public KeybindHandler(SellTaskManager taskManager, DonutSellConfig config) {
        this.taskManager = taskManager;
        this.config = config;
    }

    /**
     * Register the keybinding with the Fabric API.
     * Must be called during mod initialization.
     */
    public void register() {
        toggleKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutsell.toggle",       // translation key
                InputUtil.Type.KEYSYM,          // input type
                GLFW.GLFW_KEY_BACKSLASH,        // default key
                "category.donutsell"            // category
        ));
    }

    /**
     * Check if the keybind was pressed this tick.
     * Should be called every client tick.
     */
    public void tick() {
        while (toggleKeybind.wasPressed()) {
            if (taskManager.isRunning()) {
                taskManager.stop();
            } else {
                taskManager.start(config.defaultPrice);
            }
        }
    }
}
