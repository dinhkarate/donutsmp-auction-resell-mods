package com.donutsell;

import com.donutsell.command.DonutSellCommand;
import com.donutsell.config.DonutSellConfig;
import com.donutsell.keybind.KeybindHandler;
import com.donutsell.task.SellTaskManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

/**
 * ASell - Automatic Auction House Selling Mod
 *
 * Main entry point for the Fabric client mod.
 * Registers all event handlers, commands, and keybindings.
 *
 * Features:
 *   - Auto-sell items via /ah sell <price>
 *   - Configurable keybind toggle
 *   - Inventory management (find, swap, drop items)
 *   - GUI auto-confirm
 *   - Tick-based state machine with randomized anti-ban delays
 *   - Break simulator (giả lập nghỉ ngơi)
 *   - Auto-order when out of stock
 *   - Staff chat monitor (dừng khi bị admin nhắn)
 *   - JSON config file
 *
 * Compatible with Lunar Client + Fabric.
 * No mixins, no heavy dependencies.
 *
 * @author nguyenttuca
 */
public class DonutSellMod implements ClientModInitializer {
    public static final String MOD_ID = "donutsell";

    private DonutSellConfig config;
    private SellTaskManager taskManager;
    private KeybindHandler keybindHandler;

    @Override
    public void onInitializeClient() {
        System.out.println("[ASell] Khởi động...");

        // Load configuration
        config = DonutSellConfig.load();

        // Create the task manager (state machine)
        taskManager = new SellTaskManager(config);

        // Register client commands (/asell ...)
        DonutSellCommand.register(taskManager, config);

        // Register keybind
        keybindHandler = new KeybindHandler(taskManager, config);
        keybindHandler.register();

        // Register tick event - drives the state machine and keybind checks
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            keybindHandler.tick();
            taskManager.tick();
        });

        // Register disconnect event - auto-stop on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            taskManager.onDisconnect();
        });

        // Anti-Staff Chat Monitor: dừng khi phát hiện tin nhắn nghi ngờ
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTime) -> {
            if (taskManager.isRunning()) {
                String plainText = message.getString().toLowerCase();
                String playerName = MinecraftClient.getInstance().player != null ?
                    MinecraftClient.getInstance().player.getName().getString().toLowerCase() : "";

                boolean containsName = !playerName.isEmpty() && plainText.contains(playerName);
                boolean isWhisper = plainText.contains("whisper") ||
                                    plainText.contains("nhắn riêng") ||
                                    plainText.contains("nhắn cho bạn") ||
                                    plainText.contains("đến bạn") ||
                                    plainText.contains("nhắn từ") ||
                                    plainText.contains("mật");

                if (containsName || isWhisper) {
                    taskManager.stopAndAlert("Phát hiện tin nhắn nghi ngờ: " + message.getString());
                }
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay && taskManager.isRunning()) {
                String plainText = message.getString().toLowerCase();
                String playerName = MinecraftClient.getInstance().player != null ?
                    MinecraftClient.getInstance().player.getName().getString().toLowerCase() : "";

                boolean containsName = !playerName.isEmpty() && plainText.contains(playerName);
                boolean isWhisper = plainText.contains("whisper") ||
                                    plainText.contains("nhắn riêng") ||
                                    plainText.contains("nhắn cho bạn") ||
                                    plainText.contains("đến bạn") ||
                                    plainText.contains("nhắn từ") ||
                                    plainText.contains("mật");

                if (containsName || isWhisper) {
                    taskManager.stopAndAlert("Phát hiện thông báo hệ thống nghi ngờ: " + message.getString());
                }

                boolean isAhFull = plainText.contains("you have too many listed items") || 
                                   plainText.contains("too many listed items") || 
                                   plainText.contains("have to many");

                if (isAhFull) {
                    taskManager.triggerAhFull();
                }

                boolean isItemSold = plainText.contains("bought your") || 
                                     plainText.contains("đã mua") || 
                                     plainText.contains("purchased");

                if (isItemSold) {
                    taskManager.triggerItemSold();
                }
            }
        });

        System.out.println("[ASell] Đã tải! Dùng /asell <price> hoặc /asell help");
    }
}
