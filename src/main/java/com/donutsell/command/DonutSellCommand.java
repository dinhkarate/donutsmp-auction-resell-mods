package com.donutsell.command;

import com.donutsell.config.DonutSellConfig;
import com.donutsell.inventory.InventoryUtils;
import com.donutsell.task.SellTaskManager;
import com.donutsell.util.ChatUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

/**
 * Registers the /asell client-side command tree.
 *
 * Commands:
 *   /asell <price>         - Bắt đầu bán với giá tùy chỉnh
 *   /asell                 - Bán với giá mặc định
 *   /asell stop            - Dừng tác vụ
 *   /asell status          - Xem trạng thái
 *   /asell reload          - Tải lại config
 *   /asell item <id>       - Đặt item mục tiêu (tự thêm minecraft: prefix)
 *   /asell quantity <n>    - Đặt số lượng mỗi lần bán
 *   /asell delay <ticks>   - Đặt delay giữa các lần bán
 *   /asell slot <n>        - Đặt slot xác nhận GUI
 *   /asell autoorder on|off - Bật/tắt auto lấy đồ từ /order
 *   /asell ordercmd <cmd>  - Đặt lệnh order
 *   /asell help            - Hiển thị trợ giúp
 *
 * @author nguyenttuca
 */
public class DonutSellCommand {

    public static void register(SellTaskManager taskManager, DonutSellConfig config) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("asell")

                    // /asell asell <undercut> [searchQuery]
                    .then(ClientCommandManager.literal("asell")
                        .then(ClientCommandManager.argument("undercut", StringArgumentType.word())
                            .then(ClientCommandManager.argument("searchQuery", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String raw = StringArgumentType.getString(ctx, "undercut");
                                    Integer undercut = SellTaskManager.parsePrice(raw);
                                    if (undercut == null) {
                                        ChatUtils.sendError(ChatUtils.lang("Undercut không hợp lệ: ", "Invalid undercut: ") + raw + ChatUtils.lang(". Ví dụ: 1k, 2k, 6k.", ". Examples: 1k, 2k, 6k."));
                                        return 0;
                                    }
                                    String query = StringArgumentType.getString(ctx, "searchQuery").trim();
                                    taskManager.startHeldItemUndercut(undercut, query);
                                    return 1;
                                })
                            )
                            .executes(ctx -> {
                                String raw = StringArgumentType.getString(ctx, "undercut");
                                Integer undercut = SellTaskManager.parsePrice(raw);
                                if (undercut == null) {
                                    ChatUtils.sendError(ChatUtils.lang("Undercut không hợp lệ: ", "Invalid undercut: ") + raw + ChatUtils.lang(". Ví dụ: 1k, 2k, 6k.", ". Examples: 1k, 2k, 6k."));
                                    return 0;
                                }
                                taskManager.startHeldItemUndercut(undercut);
                                return 1;
                            })
                        )
                    )

                    // /asell <undercut> [searchQuery] — shorthand
                    .then(ClientCommandManager.argument("undercutOnly", StringArgumentType.word())
                        .then(ClientCommandManager.argument("searchQueryShort", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String raw = StringArgumentType.getString(ctx, "undercutOnly");
                                Integer undercut = SellTaskManager.parsePrice(raw);
                                if (undercut == null) {
                                    ChatUtils.sendError(ChatUtils.lang("Undercut không hợp lệ: ", "Invalid undercut: ") + raw + ChatUtils.lang(". Ví dụ: 1k, 2k, 6k.", ". Examples: 1k, 2k, 6k."));
                                    return 0;
                                }
                                String query = StringArgumentType.getString(ctx, "searchQueryShort").trim();
                                taskManager.startHeldItemUndercut(undercut, query);
                                return 1;
                            })
                        )
                        .executes(ctx -> {
                            String raw = StringArgumentType.getString(ctx, "undercutOnly");
                            Integer undercut = SellTaskManager.parsePrice(raw);
                            if (undercut == null) {
                                ChatUtils.sendError(ChatUtils.lang("Undercut không hợp lệ: ", "Invalid undercut: ") + raw + ChatUtils.lang(". Ví dụ: 1k, 2k, 6k.", ". Examples: 1k, 2k, 6k."));
                                return 0;
                            }
                            taskManager.startHeldItemUndercut(undercut);
                            return 1;
                        })
                    )

                    // /asell sharpness5axe
                    .then(ClientCommandManager.literal("sharpness5axe")
                        .executes(ctx -> {
                            taskManager.startSharpness5Axe();
                            return 1;
                        })
                    )

                    // /asell axesharp5 <price>
                    .then(ClientCommandManager.literal("axesharp5")
                        .then(ClientCommandManager.argument("priceText", StringArgumentType.word())
                            .executes(ctx -> {
                                String rawPrice = StringArgumentType.getString(ctx, "priceText");
                                Integer price = SellTaskManager.parsePrice(rawPrice);
                                if (price == null) {
                                    ChatUtils.sendError(ChatUtils.lang("Giá không hợp lệ: ", "Invalid price: ") + rawPrice + ChatUtils.lang(". Ví dụ: 450k, 400k, 500000.", ". Examples: 450k, 400k, 500000."));
                                    return 0;
                                }
                                taskManager.startAxeSharp5Fixed(price);
                                return 1;
                            })
                        )
                    )

                    // /asell report
                    .then(ClientCommandManager.literal("report")
                        .executes(ctx -> {
                            taskManager.sendFinancialReport();
                            return 1;
                        })
                    )

                    // /asell sellclick <min> <max>
                    .then(ClientCommandManager.literal("sellclick")
                        .then(ClientCommandManager.argument("min", IntegerArgumentType.integer(1, 600))
                            .then(ClientCommandManager.argument("max", IntegerArgumentType.integer(1, 600))
                                .executes(ctx -> {
                                    int min = IntegerArgumentType.getInteger(ctx, "min");
                                    int max = IntegerArgumentType.getInteger(ctx, "max");
                                    if (min > max) { int t = min; min = max; max = t; }
                                    config.sellInvClickDelayMin = min;
                                    config.sellInvClickDelayMax = max;
                                    config.save();
                                    ChatUtils.sendSuccess(ChatUtils.lang("Sell click delay: §f", "Sell click delay: §f") + min + ChatUtils.lang("–", "-") + max + ChatUtils.lang(" tick", " ticks"));
                                    return 1;
                                })
                            )
                        )
                    )

                    // /asell sellconfirm <min> <max>
                    .then(ClientCommandManager.literal("sellconfirm")
                        .then(ClientCommandManager.argument("min", IntegerArgumentType.integer(1, 600))
                            .then(ClientCommandManager.argument("max", IntegerArgumentType.integer(1, 600))
                                .executes(ctx -> {
                                    int min = IntegerArgumentType.getInteger(ctx, "min");
                                    int max = IntegerArgumentType.getInteger(ctx, "max");
                                    if (min > max) { int t = min; min = max; max = t; }
                                    config.sellInvConfirmDelayMin = min;
                                    config.sellInvConfirmDelayMax = max;
                                    config.save();
                                    ChatUtils.sendSuccess(ChatUtils.lang("Sell confirm delay: §f", "Sell confirm delay: §f") + min + ChatUtils.lang("–", "-") + max + ChatUtils.lang(" tick", " ticks"));
                                    return 1;
                                })
                            )
                        )
                    )

                    // /asell lang <vi|en>
                    .then(ClientCommandManager.literal("lang")
                        .then(ClientCommandManager.argument("langCode", StringArgumentType.word())
                            .executes(ctx -> {
                                String code = StringArgumentType.getString(ctx, "langCode").trim().toLowerCase();
                                if (!code.equals("vi") && !code.equals("en")) {
                                    ChatUtils.sendError("Ngôn ngữ/Language không hợp lệ/invalid: vi hoặc en.");
                                    return 0;
                                }
                                config.language = code;
                                config.save();
                                ChatUtils.setLang(code);
                                ChatUtils.sendSuccess("Đã chọn ngôn ngữ / Language set: " + code.toUpperCase());
                                return 1;
                            })
                        )
                    )

                    // /asell sellinv
                    .then(ClientCommandManager.literal("sellinv")
                        .executes(ctx -> {
                            taskManager.startSellInv();
                            return 1;
                        })
                    )

                    // /asell stop
                    .then(ClientCommandManager.literal("stop")
                        .executes(ctx -> {
                            taskManager.stop();
                            return 1;
                        })
                    )

                    // /asell status
                    .then(ClientCommandManager.literal("status")
                        .executes(ctx -> {
                            int total = InventoryUtils.getTotalCount(config.targetItem,
                                    config.targetEnchantment, config.targetEnchantmentLevel);
                            ChatUtils.sendInfo(ChatUtils.lang("═══ Trạng thái ASell ═══", "=== ASell status ==="));
                            ChatUtils.sendInfo(ChatUtils.lang("State:      §f", "State:      §f") + taskManager.getState());
                            ChatUtils.sendInfo(ChatUtils.lang("Đã bán:     §f", "Sold:       §f") + taskManager.getItemsSold() + ChatUtils.lang(" lần", " times"));
                            ChatUtils.sendInfo(ChatUtils.lang("Item:       §f", "Item:       §f") + config.targetItem);
                            ChatUtils.sendInfo(ChatUtils.lang("Còn lại:    §f", "Left:       §f") + total + ChatUtils.lang(" item", " item"));
                            ChatUtils.sendInfo(ChatUtils.lang("Giá:        §f", "Price:      §f") + config.defaultPrice);
                            ChatUtils.sendInfo(ChatUtils.lang("Số lượng:   §f", "Quantity:   §f") + config.desiredQuantity + ChatUtils.lang("/lần", "/sale"));
                            ChatUtils.sendInfo(ChatUtils.lang("Collected:  §f", "Collected:  §f") + taskManager.getItemsCollected());
                            ChatUtils.sendInfo(ChatUtils.lang("Sold:       §f", "Sold:       §f") + taskManager.getConfirmedSales());
                            ChatUtils.sendInfo(ChatUtils.lang("Gross list: §f$", "Gross list: §f$") + taskManager.getGrossListedValue());
                            ChatUtils.sendInfo(ChatUtils.lang("Revenue:    §f$", "Revenue:    §f$") + taskManager.getRealizedRevenue());
                            ChatUtils.sendInfo(ChatUtils.lang("Profit dự kiến: §f$", "Projected profit: §f$") + taskManager.getProjectedProfit());
                            ChatUtils.sendInfo(ChatUtils.lang("Profit thực nhận: §f$", "Realized profit: §f$") + taskManager.getRealizedProfit());
                            if (config.autoOrder) {
                                ChatUtils.sendInfo(ChatUtils.lang("Auto-order: §aBẬT §7(/", "Auto-order: §aON §7(/") + config.orderCommand + ChatUtils.lang(")", ")"));
                            } else {
                                ChatUtils.sendInfo(ChatUtils.lang("Auto-order: §cTẮT", "Auto-order: §cOFF"));
                            }
                            return 1;
                        })
                    )

                    // /asell reload
                    .then(ClientCommandManager.literal("reload")
                        .executes(ctx -> {
                            if (taskManager.isRunning()) {
                                ChatUtils.sendError(ChatUtils.lang("Không thể reload khi đang chạy! Dùng /asell stop trước.", "Cannot reload while running! Use /asell stop first."));
                                return 0;
                            }
                            DonutSellConfig newConfig = DonutSellConfig.load();
                            config.copyFrom(newConfig);
                            ChatUtils.sendSuccess(ChatUtils.lang("Đã tải lại config thành công!", "Config reloaded successfully!"));
                            return 1;
                        })
                    )

                    // /asell item <id>
                    .then(ClientCommandManager.literal("item")
                        .then(ClientCommandManager.argument("itemId", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String rawInput = StringArgumentType.getString(ctx, "itemId").trim();
                                // Chuyển sang chữ thường và thay khoảng trắng bằng gạch dưới để khớp registry ID
                                String formatted = rawInput.toLowerCase().replace(" ", "_");
                                // Auto-prefix minecraft: nếu chưa có namespace
                                if (!formatted.contains(":")) {
                                    formatted = "minecraft:" + formatted;
                                }
                                config.targetItem = formatted;
                                config.save();
                                ChatUtils.sendSuccess(ChatUtils.lang("Đã đặt item mục tiêu: §f", "Target item set: §f") + formatted);
                                return 1;
                            })
                        )
                    )

                    // /asell quantity <n>
                    .then(ClientCommandManager.literal("quantity")
                        .then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1, 64))
                            .executes(ctx -> {
                                int count = IntegerArgumentType.getInteger(ctx, "count");
                                config.desiredQuantity = count;
                                config.save();
                                ChatUtils.sendSuccess(ChatUtils.lang("Đã đặt số lượng: §f", "Quantity set: §f") + count + ChatUtils.lang("/lần", "/sale"));
                                return 1;
                            })
                        )
                    )

                    // /asell cost <price>
                    .then(ClientCommandManager.literal("cost")
                        .then(ClientCommandManager.argument("priceText", StringArgumentType.word())
                            .executes(ctx -> {
                                String rawPrice = StringArgumentType.getString(ctx, "priceText");
                                Integer cost = SellTaskManager.parsePrice(rawPrice);
                                if (cost == null) {
                                    ChatUtils.sendError(ChatUtils.lang("Cost không hợp lệ: ", "Invalid cost: ") + rawPrice + ChatUtils.lang(". Ví dụ: 300k.", ". Example: 300k."));
                                    return 0;
                                }
                                config.acquisitionCostPerItem = cost;
                                config.save();
                                ChatUtils.sendSuccess(ChatUtils.lang("Đã đặt cost mỗi item: §f$", "Cost per item set: §f$") + cost);
                                return 1;
                            })
                        )
                    )

                    // /asell delay <ticks>
                    .then(ClientCommandManager.literal("delay")
                        .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(5, 600))
                            .executes(ctx -> {
                                int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
                                config.itemDelay = ticks;
                                config.save();
                                ChatUtils.sendSuccess(ChatUtils.lang("Đã đặt delay: §f", "Delay set: §f") + ticks + ChatUtils.lang(" tick §7(", " ticks §7(")
                                        + String.format("%.1f", ticks / 20.0) + ChatUtils.lang("s)", "s)"));
                                return 1;
                            })
                        )
                    )

                    // /asell slot <n>
                    .then(ClientCommandManager.literal("slot")
                        .then(ClientCommandManager.argument("slotIndex", IntegerArgumentType.integer(0, 53))
                            .executes(ctx -> {
                                int slot = IntegerArgumentType.getInteger(ctx, "slotIndex");
                                config.confirmSlotIndex = slot;
                                config.save();
                                ChatUtils.sendSuccess(ChatUtils.lang("Đã đặt slot xác nhận: §f", "Confirm slot set: §f") + slot);
                                return 1;
                            })
                        )
                    )

                    // /asell autoorder on|off
                    .then(ClientCommandManager.literal("autoorder")
                        .then(ClientCommandManager.literal("on")
                            .executes(ctx -> {
                                config.autoOrder = true;
                                config.save();
                                ChatUtils.sendSuccess(ChatUtils.lang("Auto-order: §aBẬT", "Auto-order: §aON"));
                                ChatUtils.sendInfo(ChatUtils.lang("Khi hết đồ, mod sẽ tự chạy §f/", "When out of stock the mod will run §f/") + config.orderCommand + ChatUtils.lang(" §7để lấy thêm.", " §7to refill."));
                                return 1;
                            })
                        )
                        .then(ClientCommandManager.literal("off")
                            .executes(ctx -> {
                                config.autoOrder = false;
                                config.save();
                                ChatUtils.sendSuccess(ChatUtils.lang("Auto-order: §cTẮT", "Auto-order: §cOFF"));
                                return 1;
                            })
                        )
                    )

                    // /asell ordercmd <command>
                    .then(ClientCommandManager.literal("ordercmd")
                        .then(ClientCommandManager.argument("cmd", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String cmd = StringArgumentType.getString(ctx, "cmd").trim();
                                config.orderCommand = cmd;
                                config.save();
                                ChatUtils.sendSuccess(ChatUtils.lang("Đã đặt lệnh order: §f/", "Order command set: §f/") + cmd);
                                return 1;
                            })
                        )
                    )

                    // /asell help
                    .then(ClientCommandManager.literal("help")
                        .executes(ctx -> {
                            showHelp();
                            return 1;
                        })
                    )

                    // /asell <price>
                    .then(ClientCommandManager.argument("price", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int sellPrice = IntegerArgumentType.getInteger(ctx, "price");
                            taskManager.startPlain(sellPrice);
                            return 1;
                        })
                    )

                    // /asell (no args - dùng giá mặc định)
                    .executes(ctx -> {
                        taskManager.startPlain(config.defaultPrice);
                        return 1;
                    })
            );
        });
    }

    private static void showHelp() {
        ChatUtils.sendInfo(ChatUtils.lang("═══ ASell - Trợ giúp ═══", "=== ASell - Help ==="));
        ChatUtils.sendInfo(ChatUtils.lang("§e--- Điều khiển ---", "§e--- Control ---"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell asell <under> §7Cầm item mẫu, quét AH, undercut, tự fill /order", "§f/asell asell <under> §7Resell held item, scan AH, undercut"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell asell 1k diamond axe sharpness 5 §7Chỉ định tên quét AH", "§f/asell asell 1k diamond axe sharpness 5 §7With AH search text"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell <under>       §7Alias ngắn, ví dụ /asell 1k", "§f/asell <under>       §7Short alias, e.g. /asell 1k"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell 1k diamond axe sharpness 5 §7Alias ngắn kèm tên quét AH", "§f/asell 1k diamond axe sharpness 5 §7Short alias + search text"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell sharpness5axe  §7Quét AH + undercut Diamond Axe Sharpness V", "§f/asell sharpness5axe  §7Scan AH + undercut Diamond Axe Sharpness V"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell axesharp5 <giá> §7List Diamond Axe Sharpness V theo giá cố định", "§f/asell axesharp5 <price> §7List Diamond Axe Sharpness V at a fixed price"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell <giá>          §7Bán với giá tùy chỉnh", "§f/asell <price>       §7Sell at a custom price"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell                §7Bán với giá mặc định", "§f/asell                §7Sell at the default price"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell sellinv        §7Mở /sell, đẩy hết inventory vào rồi bấm confirm", "§f/asell sellinv        §7Open /sell, move inventory in, click confirm"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell sellclick <min> <max>  §7Delay giữa các lần đẩy item vào /sell", "§f/asell sellclick <min> <max>  §7Delay between /sell item moves"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell sellconfirm <min> <max> §7Delay trước khi bấm confirm", "§f/asell sellconfirm <min> <max> §7Delay before clicking confirm"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell stop           §7Dừng tác vụ", "§f/asell stop           §7Stop the task"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell status         §7Xem trạng thái", "§f/asell status         §7Show status"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell reload         §7Tải lại config", "§f/asell reload         §7Reload config"));
        ChatUtils.sendInfo(ChatUtils.lang("§e--- Cài đặt ---", "§e--- Settings ---"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell item <tên>     §7Đặt item (vd: lever, chest)", "§f/asell item <name>   §7Set target item (e.g. lever, chest)"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell quantity <n>   §7Đặt số lượng mỗi lần bán", "§f/asell quantity <n>   §7Set quantity per sale"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell cost <giá>     §7Đặt cost mỗi item để tính profit", "§f/asell cost <price>   §7Set cost per item for profit"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell delay <ticks>  §7Đặt delay giữa các lần bán", "§f/asell delay <ticks>  §7Set delay between sales"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell slot <n>       §7Đặt slot xác nhận GUI", "§f/asell slot <n>       §7Set GUI confirm slot"));
        ChatUtils.sendInfo(ChatUtils.lang("§e--- Auto-Order ---", "§e--- Auto-Order ---"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell autoorder on   §7Bật tự lấy đồ từ /order", "§f/asell autoorder on   §7Enable auto-refill from /order"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell autoorder off  §7Tắt auto-order", "§f/asell autoorder off  §7Disable auto-order"));
        ChatUtils.sendInfo(ChatUtils.lang("§f/asell ordercmd <cmd> §7Đặt lệnh order tùy chỉnh", "§f/asell ordercmd <cmd> §7Set custom order command"));
        ChatUtils.sendInfo(ChatUtils.lang("§7Config file: .minecraft/config/asell.json", "§7Config file: .minecraft/config/asell.json"));
        ChatUtils.sendInfo(ChatUtils.lang("§7GitHub: github.com/nguyenttuca/asell-mod", "§7GitHub: github.com/nguyenttuca/asell-mod"));
    }
}
