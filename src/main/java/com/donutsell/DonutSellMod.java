package com.donutsell;

import com.donutsell.command.DonutSellCommand;
import com.donutsell.config.DonutSellConfig;
import com.donutsell.keybind.KeybindHandler;
import com.donutsell.task.SellTaskManager;
import com.donutsell.util.ChatUtils;
import com.donutsell.util.DiscordWebhook;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.Map;

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

    // Auto-reconnect state
    private ServerInfo lastServerInfo = null;
    private int reconnectTicks = -1;
    private int reconnectAttempts = 0;
    private boolean wasReconnecting = false;

    @Override
    public void onInitializeClient() {
        System.out.println("[ASell] Khởi động...");

        // Load configuration
        config = DonutSellConfig.load();

        // Apply saved language; prompt for one on first run
        ChatUtils.setLang(config.language);
        if (config.language == null || config.language.isBlank()) {
            ChatUtils.sendInfo("Chọn ngôn ngữ / Choose language: §f/asell lang vi§r hoặc §f/asell lang en");
        }

        // Create the task manager (state machine)
        taskManager = new SellTaskManager(config);

        // Register client commands (/asell ...)
        DonutSellCommand.register(taskManager, config);

        // Register keybind
        keybindHandler = new KeybindHandler(taskManager, config);
        keybindHandler.register();

        // Register tick event - drives reconnect, keybind and the state machine
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            keybindHandler.tick();
            taskManager.tick();
            tickAutoReconnect(client);
        });

        // Remember the server we joined so reconnect can rejoin it
        ClientPlayConnectionEvents.JOIN.register((ClientPlayNetworkHandler handler,
                                                  PacketSender sender, MinecraftClient client) -> {
            lastServerInfo = client.getCurrentServerEntry();
            if (wasReconnecting) {
                wasReconnecting = false;
                reconnectTicks = -1;
                reconnectAttempts = 0;
                if (config.chatNotifications) ChatUtils.sendSuccess(ChatUtils.lang("Đã reconnect thành công!", "Reconnected successfully!"));
                DiscordWebhook.send(config, ChatUtils.lang("Đã reconnect thành công; thị trường tiếp tục.", "Reconnected successfully; resuming the market."));
                if (config.autoResumeSell) taskManager.resumeLastRun();
            }
        });

        // Disconnect event - auto-stop sell and schedule reconnect
        ClientPlayConnectionEvents.DISCONNECT.register((ClientPlayNetworkHandler handler, MinecraftClient client) -> {
            taskManager.onDisconnect();
            if (client.isIntegratedServerRunning()) return;
            if (config.autoReconnect && taskManager.hasLastWorkflow()) {
                if (lastServerInfo == null) lastServerInfo = client.getCurrentServerEntry();
                if (lastServerInfo != null) {
                    reconnectTicks = Math.max(2, config.reconnectDelaySeconds * 20);
                    reconnectAttempts = 0;
                    wasReconnecting = true;
                    if (config.chatNotifications) {
                        ChatUtils.sendWarning(ChatUtils.lang("Bị disconnect! Sẽ tự reconnect sau ", "Disconnected! Reconnecting in ")
                                + config.reconnectDelaySeconds + ChatUtils.lang(" giây...", " seconds..."));
                    }
                    DiscordWebhook.send(config, ChatUtils.lang("Bị disconnect; tự reconnect sau ", "Disconnected; reconnecting in ")
                            + config.reconnectDelaySeconds + "s (nỗ lực 1/" + config.maxReconnectAttempts + ")");
                }
            }
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
            if (overlay) return;

            String plainText = message.getString().toLowerCase();
            boolean isListed = plainText.contains("you listed");
            if (isListed) {
                taskManager.onListed();
            }

            boolean isItemSold = plainText.contains("bought your")
                    || plainText.contains("đã mua")
                    || plainText.contains("purchased");
            if (isItemSold) {
                taskManager.triggerItemSold(message.getString());
            }

            if (taskManager.isRunning()) {
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

            }
        });

        System.out.println("[ASell] Đã tải! Dùng /asell <price> hoặc /asell help");
    }

    private void tickAutoReconnect(MinecraftClient client) {
        if (reconnectTicks < 0) return;

        // User went back to the title screen manually -> cancel
        if (client.currentScreen == null && client.world == null) {
            reconnectTicks = -1;
            reconnectAttempts = 0;
            wasReconnecting = false;
            return;
        }

        // Already connected again
        if (client.world != null && client.getNetworkHandler() != null) {
            reconnectTicks = -1;
            reconnectAttempts = 0;
            return;
        }

        if (reconnectTicks > 0) {
            reconnectTicks--;
            return;
        }

        if (lastServerInfo != null && reconnectAttempts < config.maxReconnectAttempts) {
            reconnectAttempts++;
            reconnectTicks = Math.max(2, config.reconnectDelaySeconds * 20);
            final ServerInfo target = lastServerInfo;
            if (config.chatNotifications) {
                ChatUtils.sendInfo(ChatUtils.lang("Reconnect nỗ lực ", "Reconnect attempt ") + reconnectAttempts + ChatUtils.lang("/", "/")
                        + config.maxReconnectAttempts + ChatUtils.lang(" → ", " -> ") + target.address);
            }
            DiscordWebhook.send(config, ChatUtils.lang("Reconnect nỗ lực ", "Reconnect attempt ") + reconnectAttempts + "/"
                    + config.maxReconnectAttempts);
            client.execute(() -> ConnectScreen.connect(
                    client.currentScreen, client,
                    ServerAddress.parse(target.address), target, false,
                    new CookieStorage(Map.of())));
        } else {
            reconnectTicks = -1;
            wasReconnecting = false;
            taskManager.clearLastWorkflow();
            DiscordWebhook.send(config, ChatUtils.lang("Không reconnect được sau ", "Could not reconnect after ") + reconnectAttempts
                    + " lần. Nếu lỗi 'invalid session' hãy restart launcher để mod không tự làm gì thêm.");
            if (config.chatNotifications) {
                ChatUtils.sendError(ChatUtils.lang("Hết số lần reconnect. Nếu 'invalid session' → restart game/launcher.", "Out of reconnect attempts. If 'invalid session', restart the game/launcher."));
            }
        }
    }
}
