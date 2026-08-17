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
                                        ChatUtils.sendError("Undercut không hợp lệ: " + raw + ". Ví dụ: 1k, 2k, 6k.");
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
                                    ChatUtils.sendError("Undercut không hợp lệ: " + raw + ". Ví dụ: 1k, 2k, 6k.");
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
                                    ChatUtils.sendError("Undercut không hợp lệ: " + raw + ". Ví dụ: 1k, 2k, 6k.");
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
                                ChatUtils.sendError("Undercut không hợp lệ: " + raw + ". Ví dụ: 1k, 2k, 6k.");
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
                                    ChatUtils.sendError("Giá không hợp lệ: " + rawPrice + ". Ví dụ: 450k, 400k, 500000.");
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
                            ChatUtils.sendInfo("═══ Trạng thái ASell ═══");
                            ChatUtils.sendInfo("State:      §f" + taskManager.getState());
                            ChatUtils.sendInfo("Đã bán:     §f" + taskManager.getItemsSold() + " lần");
                            ChatUtils.sendInfo("Item:       §f" + config.targetItem);
                            ChatUtils.sendInfo("Còn lại:    §f" + total + " item");
                            ChatUtils.sendInfo("Giá:        §f" + config.defaultPrice);
                            ChatUtils.sendInfo("Số lượng:   §f" + config.desiredQuantity + "/lần");
                            ChatUtils.sendInfo("Collected:  §f" + taskManager.getItemsCollected());
                            ChatUtils.sendInfo("Sold:       §f" + taskManager.getConfirmedSales());
                            ChatUtils.sendInfo("Gross list: §f$" + taskManager.getGrossListedValue());
                            ChatUtils.sendInfo("Revenue:    §f$" + taskManager.getRealizedRevenue());
                            ChatUtils.sendInfo("Profit dự kiến: §f$" + taskManager.getProjectedProfit());
                            ChatUtils.sendInfo("Profit thực nhận: §f$" + taskManager.getRealizedProfit());
                            if (config.autoOrder) {
                                ChatUtils.sendInfo("Auto-order: §aBẬT §7(/" + config.orderCommand + ")");
                            } else {
                                ChatUtils.sendInfo("Auto-order: §cTẮT");
                            }
                            return 1;
                        })
                    )

                    // /asell reload
                    .then(ClientCommandManager.literal("reload")
                        .executes(ctx -> {
                            if (taskManager.isRunning()) {
                                ChatUtils.sendError("Không thể reload khi đang chạy! Dùng /asell stop trước.");
                                return 0;
                            }
                            DonutSellConfig newConfig = DonutSellConfig.load();
                            config.copyFrom(newConfig);
                            ChatUtils.sendSuccess("Đã tải lại config thành công!");
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
                                ChatUtils.sendSuccess("Đã đặt item mục tiêu: §f" + formatted);
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
                                ChatUtils.sendSuccess("Đã đặt số lượng: §f" + count + "/lần");
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
                                    ChatUtils.sendError("Cost không hợp lệ: " + rawPrice + ". Ví dụ: 300k.");
                                    return 0;
                                }
                                config.acquisitionCostPerItem = cost;
                                config.save();
                                ChatUtils.sendSuccess("Đã đặt cost mỗi item: §f$" + cost);
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
                                ChatUtils.sendSuccess("Đã đặt delay: §f" + ticks + " tick §7("
                                        + String.format("%.1f", ticks / 20.0) + "s)");
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
                                ChatUtils.sendSuccess("Đã đặt slot xác nhận: §f" + slot);
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
                                ChatUtils.sendSuccess("Auto-order: §aBẬT");
                                ChatUtils.sendInfo("Khi hết đồ, mod sẽ tự chạy §f/" + config.orderCommand + " §7để lấy thêm.");
                                return 1;
                            })
                        )
                        .then(ClientCommandManager.literal("off")
                            .executes(ctx -> {
                                config.autoOrder = false;
                                config.save();
                                ChatUtils.sendSuccess("Auto-order: §cTẮT");
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
                                ChatUtils.sendSuccess("Đã đặt lệnh order: §f/" + cmd);
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
        ChatUtils.sendInfo("═══ ASell - Trợ giúp ═══");
        ChatUtils.sendInfo("§e--- Điều khiển ---");
        ChatUtils.sendInfo("§f/asell asell <under> §7Cầm item mẫu, quét AH, undercut, tự fill /order");
        ChatUtils.sendInfo("§f/asell asell 1k diamond axe sharpness 5 §7Chỉ định tên quét AH");
        ChatUtils.sendInfo("§f/asell <under>       §7Alias ngắn, ví dụ /asell 1k");
        ChatUtils.sendInfo("§f/asell 1k diamond axe sharpness 5 §7Alias ngắn kèm tên quét AH");
        ChatUtils.sendInfo("§f/asell sharpness5axe  §7Quét AH + undercut Diamond Axe Sharpness V");
        ChatUtils.sendInfo("§f/asell axesharp5 <giá> §7List Diamond Axe Sharpness V theo giá cố định");
        ChatUtils.sendInfo("§f/asell <giá>          §7Bán với giá tùy chỉnh");
        ChatUtils.sendInfo("§f/asell                §7Bán với giá mặc định");
        ChatUtils.sendInfo("§f/asell report         §7Gửi financial report lên Discord");
        ChatUtils.sendInfo("§f/asell stop           §7Dừng tác vụ");
        ChatUtils.sendInfo("§f/asell status         §7Xem trạng thái");
        ChatUtils.sendInfo("§f/asell reload         §7Tải lại config");
        ChatUtils.sendInfo("§e--- Cài đặt ---");
        ChatUtils.sendInfo("§f/asell item <tên>     §7Đặt item (vd: lever, chest)");
        ChatUtils.sendInfo("§f/asell quantity <n>   §7Đặt số lượng mỗi lần bán");
        ChatUtils.sendInfo("§f/asell cost <giá>     §7Đặt cost mỗi item để tính profit");
        ChatUtils.sendInfo("§f/asell delay <ticks>  §7Đặt delay giữa các lần bán");
        ChatUtils.sendInfo("§f/asell slot <n>       §7Đặt slot xác nhận GUI");
        ChatUtils.sendInfo("§e--- Auto-Order ---");
        ChatUtils.sendInfo("§f/asell autoorder on   §7Bật tự lấy đồ từ /order");
        ChatUtils.sendInfo("§f/asell autoorder off  §7Tắt auto-order");
        ChatUtils.sendInfo("§f/asell ordercmd <cmd> §7Đặt lệnh order tùy chỉnh");
        ChatUtils.sendInfo("§7Config file: .minecraft/config/asell.json");
        ChatUtils.sendInfo("§7GitHub: github.com/nguyenttuca/asell-mod");
    }
}
