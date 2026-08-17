package com.donutsell.task;

/**
 * All possible states for the auto-sell task state machine.
 */
public enum SellState {
    /** No task running */
    IDLE,
    /** Finding and preparing the target item */
    PREPARING_ITEM,
    /** Adjusting item count in mainhand (dropping excess) */
    ADJUSTING_QUANTITY,
    /** Waiting for hotbar swap to sync */
    SWITCHING_HOTBAR,
    /** Sending /ah sell command */
    SENDING_COMMAND,
    /** Scanning the Auction House GUI for a matching market price */
    SMART_PRICING,
    /** Waiting for auction GUI to open */
    WAITING_FOR_GUI,
    /** Clicking confirm button in GUI */
    CLICKING_CONFIRM,
    /** Cooldown between sell cycles */
    COOLDOWN,

    // ========== Order Fetching States ==========
    /** Sending /order command to fetch items */
    FETCHING_ORDER,
    /** Waiting for order list GUI to open (Page 1) */
    WAITING_ORDER_GUI,
    /** Clicking on a completed order in the order list to enter it */
    NAVIGATING_TO_ORDER_EDIT,
    /** Waiting in the Edit Order screen and clicking the COLLECT button */
    NAVIGATING_TO_COLLECT,
    /** Collecting items in the "Collect Items" screen */
    COLLECTING_ORDER_ITEMS,

    // ========== Cooldown/Wait States ==========
    /** Waiting when Auction House listings are full */
    WAITING_FOR_AH_SLOT,

    // ========== /sell Inventory Workflow ==========
    /** Opening /sell GUI */
    SELLINV_OPEN,
    /** Moving inventory items into the sell GUI */
    SELLINV_FILL,
    /** Clicking the green confirm button */
    SELLINV_CONFIRM,

    /** All items sold successfully */
    FINISHED,
    /** Error occurred */
    ERROR
}
