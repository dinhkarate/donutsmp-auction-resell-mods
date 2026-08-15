package com.donutsell.inventory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Inventory manipulation utilities for the DonutSell mod.
 * Handles item finding, counting, swapping, splitting, and dropping.
 *
 * Slot mapping reference (PlayerScreenHandler):
 *   0       = crafting output
 *   1-4     = crafting grid
 *   5-8     = armor (head, chest, legs, feet)
 *   9-35    = main inventory (3 rows of 9)
 *   36-44   = hotbar
 *   45      = offhand
 *
 * PlayerInventory (what we use for reading):
 *   0-8     = hotbar
 *   9-35    = main inventory
 *   36-39   = armor
 *   40      = offhand
 */
public class InventoryUtils {

    /**
     * Get the registry ID string for an ItemStack (e.g., "minecraft:chest").
     */
    public static String getItemId(ItemStack stack) {
        if (stack.isEmpty()) return "minecraft:air";
        return Registries.ITEM.getId(stack.getItem()).toString();
    }

    /**
     * Check if the player's mainhand holds the target item.
     */
    public static boolean isHoldingTargetItem(String targetItemId) {
        return isHoldingTargetItem(targetItemId, "", 0);
    }

    public static boolean isHoldingTargetItem(String targetItemId, String enchantmentId, int enchantmentLevel) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        ItemStack mainHand = mc.player.getMainHandStack();
        return isTargetStack(mainHand, targetItemId, enchantmentId, enchantmentLevel);
    }

    /** Match the item id and, when configured, an exact enchantment level. */
    public static boolean isTargetStack(ItemStack stack, String targetItemId,
                                        String enchantmentId, int enchantmentLevel) {
        if (stack == null || stack.isEmpty() || !getItemId(stack).equals(targetItemId)) return false;
        if (enchantmentId == null || enchantmentId.isBlank()) return true;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return false;
        var enchantments = mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        var enchantmentEntry = enchantments.getEntry(net.minecraft.util.Identifier.of("minecraft", enchantmentId));
        return enchantmentEntry.isPresent()
                && EnchantmentHelper.getLevel(enchantmentEntry.get(), stack) == enchantmentLevel;
    }

    /**
     * Get count of target item currently in the mainhand.
     */
    public static int getMainHandCount(String targetItemId) {
        return getMainHandCount(targetItemId, "", 0);
    }

    public static int getMainHandCount(String targetItemId, String enchantmentId, int enchantmentLevel) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 0;
        ItemStack mainHand = mc.player.getMainHandStack();
        return isTargetStack(mainHand, targetItemId, enchantmentId, enchantmentLevel)
                ? mainHand.getCount() : 0;
    }

    /**
     * Get count of target item in inventory (excluding the currently selected hotbar slot).
     */
    public static int getInventoryCount(String targetItemId) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 0;
        PlayerInventory inv = mc.player.getInventory();
        int selectedSlot = inv.selectedSlot;
        int count = 0;
        for (int i = 0; i < inv.main.size(); i++) {
            if (i == selectedSlot) continue;
            ItemStack stack = inv.main.get(i);
            if (!stack.isEmpty() && getItemId(stack).equals(targetItemId)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Get total count of target item across the entire main inventory (including selected slot).
     */
    public static int getTotalCount(String targetItemId) {
        return getTotalCount(targetItemId, "", 0);
    }

    public static int getTotalCount(String targetItemId, String enchantmentId, int enchantmentLevel) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 0;
        PlayerInventory inv = mc.player.getInventory();
        int count = 0;
        for (int i = 0; i < inv.main.size(); i++) {
            ItemStack stack = inv.main.get(i);
            if (isTargetStack(stack, targetItemId, enchantmentId, enchantmentLevel)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Find the first inventory slot (excluding selected hotbar slot) that contains the target item.
     * @return PlayerInventory index (0-35), or -1 if not found.
     */
    public static int findItemSlot(String targetItemId) {
        return findItemSlot(targetItemId, "", 0);
    }

    public static int findItemSlot(String targetItemId, String enchantmentId, int enchantmentLevel) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return -1;
        PlayerInventory inv = mc.player.getInventory();
        int selectedSlot = inv.selectedSlot;

        for (int i = 0; i < inv.main.size(); i++) {
            if (i == selectedSlot) continue;
            if (isTargetStack(inv.main.get(i), targetItemId, enchantmentId, enchantmentLevel)) return i;
        }
        return -1;
    }

    /**
     * Find an inventory slot with the target item that has >= minCount.
     * Prefers exact count matches first.
     * @param excludeSlot PlayerInventory slot to skip.
     * @return PlayerInventory index, or -1 if not found.
     */
    public static int findSlotWithMinCount(String targetItemId, int minCount, int excludeSlot) {
        return findSlotWithMinCount(targetItemId, "", 0, minCount, excludeSlot);
    }

    public static int findSlotWithMinCount(String targetItemId, String enchantmentId, int enchantmentLevel,
                                           int minCount, int excludeSlot) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return -1;
        PlayerInventory inv = mc.player.getInventory();

        for (int i = 0; i < inv.main.size(); i++) {
            if (i == excludeSlot) continue;
            ItemStack stack = inv.main.get(i);
            if (isTargetStack(stack, targetItemId, enchantmentId, enchantmentLevel)
                    && stack.getCount() == minCount) return i;
        }

        for (int i = 0; i < inv.main.size(); i++) {
            if (i == excludeSlot) continue;
            ItemStack stack = inv.main.get(i);
            if (isTargetStack(stack, targetItemId, enchantmentId, enchantmentLevel)
                    && stack.getCount() >= minCount) return i;
        }

        return -1;
    }

    /**
     * Find an empty slot in the main inventory.
     * @return PlayerInventory index (0-35), or -1 if none available.
     */
    public static int findEmptySlot() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return -1;
        PlayerInventory inv = mc.player.getInventory();
        int selectedSlot = inv.selectedSlot;

        // Prefer main inventory (9-35) over hotbar
        for (int i = 9; i < inv.main.size(); i++) {
            if (inv.main.get(i).isEmpty()) return i;
        }
        for (int i = 0; i < 9; i++) {
            if (i == selectedSlot) continue;
            if (inv.main.get(i).isEmpty()) return i;
        }
        return -1;
    }

    /**
     * Convert a PlayerInventory slot index to a PlayerScreenHandler slot index.
     */
    public static int toScreenSlot(int inventorySlot) {
        if (inventorySlot < 9) {
            return inventorySlot + 36; // Hotbar: inv 0-8 -> screen 36-44
        }
        return inventorySlot; // Main inv: inv 9-35 -> screen 9-35
    }

    /**
     * Swap an inventory slot's contents with the currently selected hotbar slot.
     * Uses the SWAP action type which is server-safe and atomic.
     */
    public static void swapToMainHand(int inventorySlot) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        int screenSlot = toScreenSlot(inventorySlot);
        int hotbarSlot = mc.player.getInventory().selectedSlot;

        mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                screenSlot,
                hotbarSlot,
                SlotActionType.SWAP,
                mc.player
        );
    }

    /**
     * Drop one item from the currently selected hotbar slot.
     * Uses THROW action (button 0 = drop 1 item).
     */
    public static boolean dropOneFromMainHand() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return false;

        int selectedScreenSlot = toScreenSlot(mc.player.getInventory().selectedSlot);
        mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                selectedScreenSlot,
                0, // button 0 = drop 1 item
                SlotActionType.THROW,
                mc.player
        );
        return true;
    }

    /**
     * Drop an entire stack from the currently selected hotbar slot.
     * Uses THROW action (button 1 = drop entire stack / Ctrl+Q).
     */
    public static boolean dropAllFromMainHand() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return false;

        int selectedScreenSlot = toScreenSlot(mc.player.getInventory().selectedSlot);
        mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                selectedScreenSlot,
                1, // button 1 = drop entire stack
                SlotActionType.THROW,
                mc.player
        );
        return true;
    }

    /**
     * Drop one item from a specific inventory slot.
     */
    public static void dropOneFromSlot(int inventorySlot) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        int screenSlot = toScreenSlot(inventorySlot);
        mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                screenSlot,
                0,
                SlotActionType.THROW,
                mc.player
        );
    }

    /**
     * Drop an entire stack from a specific inventory slot.
     */
    public static void dropAllFromSlot(int inventorySlot) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        int screenSlot = toScreenSlot(inventorySlot);
        mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                screenSlot,
                1,
                SlotActionType.THROW,
                mc.player
        );
    }

    /**
     * Find a non-protected, non-target item slot that can be dropped to free space.
     * @return PlayerInventory index, or -1 if nothing safe to drop.
     */
    public static int findDroppableSlot(String targetItemId, String[] protectedItems) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return -1;
        PlayerInventory inv = mc.player.getInventory();
        int selectedSlot = inv.selectedSlot;

        for (int i = 0; i < inv.main.size(); i++) {
            if (i == selectedSlot) continue;
            ItemStack stack = inv.main.get(i);
            if (stack.isEmpty()) continue;

            String itemId = getItemId(stack);
            if (itemId.equals(targetItemId)) continue;

            boolean isProtected = false;
            for (String protectedItem : protectedItems) {
                if (itemId.equals(protectedItem)) {
                    isProtected = true;
                    break;
                }
            }
            if (!isProtected) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Click a slot in the currently open screen handler (e.g., auction GUI).
     * Uses left-click (PICKUP with button 0).
     */
    public static void clickScreenSlot(int slotIndex) {
        clickScreenSlot(slotIndex, 0, SlotActionType.PICKUP);
    }

    /**
     * Click a slot in the currently open screen handler (e.g., auction GUI).
     * Uses specified button and action type.
     */
    public static void clickScreenSlot(int slotIndex, int button, SlotActionType actionType) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null
                || mc.player.currentScreenHandler == null) return;

        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                slotIndex,
                button,
                actionType,
                mc.player
        );
    }

    /**
     * Get the lore lines from an ItemStack.
     * Used by the market price scanner to read prices from auction items.
     *
     * @return List of lore Text lines, or empty list if no lore
     */
    public static List<Text> getItemLore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Collections.emptyList();

        try {
            LoreComponent lore = stack.get(DataComponentTypes.LORE);
            if (lore != null) {
                return lore.lines();
            }
        } catch (Exception e) {
            // Fallback: if DataComponentTypes not available in this version
            System.err.println("[DonutSell] Failed to read item lore: " + e.getMessage());
        }

        return Collections.emptyList();
    }
}
