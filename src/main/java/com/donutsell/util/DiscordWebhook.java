package com.donutsell.util;

import com.donutsell.config.DonutSellConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/** Optional, asynchronous Discord webhook notifications. */
public final class DiscordWebhook {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private DiscordWebhook() {
    }

    public static void send(DonutSellConfig config, String message) {
        if (config == null || !config.discordWebhookEnabled || !isAllowedWebhook(config.discordWebhookUrl)
                || message == null || message.isBlank()) {
            return;
        }

        String payload = "{\"content\":\"" + escapeJson(message) + "\",\"username\":\"ASell\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.discordWebhookUrl.trim()))
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAccept(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        System.err.println("[ASell] Discord webhook returned HTTP " + response.statusCode());
                    }
                })
                .exceptionally(error -> {
                    System.err.println("[ASell] Discord webhook failed: " + error.getMessage());
                    return null;
                });
    }

    private static boolean isAllowedWebhook(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) return false;
        try {
            URI uri = URI.create(rawUrl.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();
            return "https".equalsIgnoreCase(scheme)
                    && ("discord.com".equalsIgnoreCase(host) || "discordapp.com".equalsIgnoreCase(host))
                    && path != null && path.startsWith("/api/webhooks/");
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
