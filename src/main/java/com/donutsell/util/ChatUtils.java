package com.donutsell.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Utility class for sending formatted chat messages to the player.
 * All messages are prefixed with [DonutSell] and color-coded.
 */
public class ChatUtils {
    private static final String PREFIX = "§6[DonutSell]§r ";

    public static void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(PREFIX + message), false);
        }
    }

    public static void sendSuccess(String message) {
        sendMessage("§a" + message);
    }

    public static void sendError(String message) {
        sendMessage("§c" + message);
    }

    public static void sendWarning(String message) {
        sendMessage("§e" + message);
    }

    public static void sendInfo(String message) {
        sendMessage("§7" + message);
    }

    public static void sendAction(String message) {
        sendMessage("§b" + message);
    }
}
