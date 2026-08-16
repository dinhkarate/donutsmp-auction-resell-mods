package com.donutsell.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration manager for ASell mod.
 * Stores settings as a JSON file in the Fabric config directory.
 *
 * @author nguyenttuca
 */
public class DonutSellConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("asell.json");

    // ====== Pricing ======
    /** Default sell price when no price argument is given */
    public int defaultPrice = 100;

    // ====== Target Item ======
    /** Registry ID of the item to sell (e.g., "minecraft:chest") */
    public String targetItem = "minecraft:diamond_axe";

    /** Optional enchantment registry path to require on the target item. */
    public String targetEnchantment = "sharpness";

    /** Exact enchantment level required when targetEnchantment is set. */
    public int targetEnchantmentLevel = 5;

    /** Desired quantity of the item to hold in mainhand before selling */
    public int desiredQuantity = 1;

    /** Enable direct GUI price scanning for /asell smart. */
    public boolean smartPricing = true;

    /** Amount to undercut the lowest matching listing by. */
    public int undercutAmount = 1000;

    /** Generic held-item workflow: exact ItemStack template captured at start. */
    public boolean heldItemWorkflow = false;

    /** Exact held-item component template encoded for the current run. */
    public String heldItemTemplate = "";

    /** Lowest accepted market price for smart pricing. */
    public int minimumMarketPrice = 400000;

    /** Highest accepted market price for smart pricing. */
    public int maximumMarketPrice = 500000;

    /** Acquisition cost paid per item through /order, used for profit reports. */
    public int acquisitionCostPerItem = 0;

    // ====== Timing (in ticks, 20 ticks = 1 second) ======
    /** Delay (ticks) before clicking confirm button in the auction GUI */
    public int guiClickDelay = 10;

    /** Delay (ticks) between sell cycles */
    public int itemDelay = 30;

    /** Delay (ticks) after sending the /ah sell command before checking for GUI */
    public int commandDelay = 5;

    /** Timeout (ticks) waiting for the auction GUI to open */
    public int guiTimeout = 100;

    // ====== Behavior ======
    /** Whether to auto-click the confirm button in the auction GUI */
    public boolean autoConfirmGui = true;

    /** Whether to show chat notifications for each action */
    public boolean chatNotifications = true;

    /** Enable optional Discord webhook notifications. URL stays local in asell.json. */
    public boolean discordWebhookEnabled = false;

    /** Discord webhook URL; never hard-code this into the mod source. */
    public String discordWebhookUrl = "";

    // ====== GUI Detection ======
    /** Slot index to click in the auction GUI for confirmation (0-indexed) */
    public int confirmSlotIndex = 15;

    /**
     * Partial text to match in the GUI title to identify the auction screen.
     * Empty string = accept any container GUI that opens after the command.
     */
    public String guiTitleContains = "";

    // ====== Auto-Order ======
    /** Enable auto-fetching from /order when out of stock */
    public boolean autoOrder = false;

    /** Command to use for ordering (without the /) */
    public String orderCommand = "order";

    // ====== Protection ======
    /** Items that should NEVER be dropped to make inventory space */
    public String[] protectedItems = {
            "minecraft:diamond",
            "minecraft:netherite_ingot",
            "minecraft:elytra",
            "minecraft:diamond_sword",
            "minecraft:diamond_pickaxe",
            "minecraft:diamond_axe",
            "minecraft:netherite_sword",
            "minecraft:netherite_pickaxe"
    };

    /**
     * Load config from file, or create default if not found.
     */
    public static DonutSellConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                DonutSellConfig config = GSON.fromJson(reader, DonutSellConfig.class);
                if (config != null) {
                    config.save();
                    return config;
                }
            } catch (Exception e) {
                System.err.println("[ASell] Failed to load config: " + e.getMessage());
            }
        }
        DonutSellConfig config = new DonutSellConfig();
        config.save();
        return config;
    }

    /** Save current config to file. */
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            System.err.println("[ASell] Failed to save config: " + e.getMessage());
        }
    }

    /**
     * Copy values from another config instance (used for hot-reload).
     */
    public void copyFrom(DonutSellConfig other) {
        this.defaultPrice = other.defaultPrice;
        this.targetItem = other.targetItem;
        this.targetEnchantment = other.targetEnchantment;
        this.targetEnchantmentLevel = other.targetEnchantmentLevel;
        this.desiredQuantity = other.desiredQuantity;
        this.smartPricing = other.smartPricing;
        this.undercutAmount = other.undercutAmount;
        this.heldItemWorkflow = other.heldItemWorkflow;
        this.heldItemTemplate = other.heldItemTemplate;
        this.minimumMarketPrice = other.minimumMarketPrice;
        this.maximumMarketPrice = other.maximumMarketPrice;
        this.acquisitionCostPerItem = other.acquisitionCostPerItem;
        this.guiClickDelay = other.guiClickDelay;
        this.itemDelay = other.itemDelay;
        this.commandDelay = other.commandDelay;
        this.guiTimeout = other.guiTimeout;
        this.autoConfirmGui = other.autoConfirmGui;
        this.chatNotifications = other.chatNotifications;
        this.discordWebhookEnabled = other.discordWebhookEnabled;
        this.discordWebhookUrl = other.discordWebhookUrl;
        this.confirmSlotIndex = other.confirmSlotIndex;
        this.guiTitleContains = other.guiTitleContains;
        this.protectedItems = other.protectedItems;
        this.autoOrder = other.autoOrder;
        this.orderCommand = other.orderCommand;
    }
}
