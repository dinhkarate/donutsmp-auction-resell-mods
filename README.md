# Fabric Auction House Auto-Sell Mod

<div align="center">

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green?style=for-the-badge&logo=minecraft)
![Fabric](https://img.shields.io/badge/Fabric-0.100+-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**Tự động bán hàng trên Auction House — nhanh, an toàn, chống ban**

</div>

---

## ✨ Tính năng nổi bật

| Tính năng | Mô tả |
|-----------|-------|
| 🤖 **Auto-sell** | Tự động gửi `/ah sell <price>`, click xác nhận GUI, lặp liên tục |
| 🎲 **Randomized delay** | Thời gian chờ ngẫu nhiên ±50% mỗi lần bán để chống phát hiện bot |
| ☕ **Break Simulator** | Tự động nghỉ 5–10 giây sau mỗi 10–20 lần bán, giả lập hành vi người chơi |
| 📦 **Auto-Order** | Khi hết hàng, tự động gửi `/order` và lấy đồ từ GUI |
| 🛡️ **Staff Chat Monitor** | Tự dừng ngay khi phát hiện tin nhắn riêng/nghi ngờ từ admin |
| 🔍 **Auto-detect GUI** | Tự tìm nút xác nhận (kính xanh lá) trong GUI, không cần cài slot thủ công |
| ⚙️ **JSON Config** | Mọi cài đặt lưu vào file `.minecraft/config/asell.json` |
| ⌨️ **Keybind toggle** | Bật/tắt mod bằng phím tắt (mặc định: không đặt) |
| 🌐 **Background** | Hoạt động khi alt-tab (tắt "Pause on Lost Focus" trong Minecraft) |

---

## 🛠️ Công nghệ sử dụng

- **[Fabric API](https://fabricmc.net/)** — Framework mod client-side cho Minecraft
- **[Fabric Loader](https://fabricmc.net/use/loader/)** — Loader để khởi động mod
- **[Brigadier](https://github.com/Mojang/brigadier)** — Thư viện parse lệnh của Mojang (đăng ký `/asell`)
- **[Gson](https://github.com/google/gson)** — Đọc/ghi file config JSON
- **Java 21** — Ngôn ngữ lập trình chính
- **Gradle + Fabric Loom** — Build system

### Kiến trúc

```
com.donutsell/
├── DonutSellMod.java          # Entry point, đăng ký event listeners
├── command/
│   └── DonutSellCommand.java  # Xử lý lệnh /asell
├── config/
│   └── DonutSellConfig.java   # Load/save config JSON
├── inventory/
│   └── InventoryUtils.java    # Tìm, đếm, swap item trong inventory
├── keybind/
│   └── KeybindHandler.java    # Xử lý phím tắt
├── task/
│   ├── SellState.java         # Enum các trạng thái state machine
│   └── SellTaskManager.java   # Core logic: state machine điều khiển toàn bộ
└── util/
    └── ChatUtils.java         # Gửi message màu sắc vào chat
```

**State Machine:**
```
IDLE → PREPARING_ITEM → SENDING_COMMAND → WAITING_FOR_GUI → CLICKING_CONFIRM → COOLDOWN → (loop)
                      ↘ ADJUSTING_QUANTITY ↗
                      ↘ SWITCHING_HOTBAR  ↗
                      ↘ FETCHING_ORDER → WAITING_ORDER_GUI → COLLECTING_ORDER_ITEMS ↗
```

---

## 📦 Cài đặt

### Yêu cầu

- Minecraft **1.21.1**
- [Fabric Loader](https://fabricmc.net/use/installer/) ≥ 0.16
- [Fabric API](https://modrinth.com/mod/fabric-api)

### Hướng dẫn cài

1. Tải file `.jar` từ [Releases](https://github.com/nguyenttuca/DonutSMP-Auto-Seller-Mod/releases/tag/v1.0.0)
2. Copy vào thư mục `.minecraft/mods/`
3. Khởi động Minecraft với Fabric profile
4. Vào game và dùng `/asell help`

[Video Hướng Dẫn Chi Tiết](https://www.youtube.com/watch?v=hk0OvS75VD4)

---

## 🎮 Hướng dẫn sử dụng

### Bước đầu: Thiết lập item

```
/asell item lever        → Đặt item cần bán là minecraft:lever
/asell item chest        → Đặt item cần bán là minecraft:chest
/asell item oak_log      → Đặt item cần bán là minecraft:oak_log
/asell quantity 1        → Mỗi lần bán 1 cái
/asell delay 30          → Delay 30 tick (1.5 giây) giữa các lần bán
```

> **Lưu ý:** Không cần nhập `minecraft:` — mod tự thêm prefix.

### Bắt đầu bán

```bash
/asell 5000                    # Bán với giá 5000
/asell                         # Bán với giá mặc định (config)
/asell asell 1k                # Cầm item mẫu, undercut AH 1k, tự fill /order
/asell asell 1k diamond axe sharpness 5   # Chỉ định tên quét AH rồi undercut 1k
/asell 1k                       # Alias ngắn của lệnh trên
/asell 1k diamond axe sharpness 5         # Alias ngắn kèm tên quét AH
/asell cost 300k                # Cost mua mỗi item từ /order để tính profit
/asell axesharp5 450k           # List Sharpness V, tự collect /order khi hết hàng
/asell sharpness5axe            # Quét AH, undercut 1,000, tự collect /order
/asell report                   # Gửi financial report lên Discord
/asell stop                    # Dừng
/asell status                  # Xem trạng thái
```

### Danh sách lệnh đầy đủ

| Lệnh | Mô tả |
|------|-------|
| `/asell <giá>` | Bắt đầu bán với giá chỉ định |
| `/asell` | Bán với giá mặc định trong config |
| `/asell stop` | Dừng tác vụ ngay lập tức |
| `/asell status` | Xem trạng thái, số lần đã bán, item còn lại |
| `/asell reload` | Tải lại file config (không cần khởi động lại) |
| `/asell item <tên>` | Đặt item cần bán (không cần `minecraft:`) |
| `/asell quantity <n>` | Số lượng item mỗi lần bán (1–64) |
| `/asell delay <ticks>` | Delay giữa các lần bán (5–200 tick) |
| `/asell slot <n>` | Slot GUI xác nhận (fallback nếu auto-detect lỗi) |
| `/asell autoorder on\|off` | Bật/tắt tự lấy đồ từ `/order` khi hết hàng |
| `/asell ordercmd <cmd>` | Đặt lệnh order tùy chỉnh (mặc định: `order`) |
| `/asell help` | Hiển thị trợ giúp trong game |

---

## ⚙️ File Config

Config lưu tại: `.minecraft/config/asell.json`

```json
{
  "defaultPrice": 100,
  "targetItem": "minecraft:diamond_axe",
  "heldItemWorkflow": false,
  "heldItemTemplate": "",
  "targetEnchantment": "sharpness",
  "targetEnchantmentLevel": 5,
  "desiredQuantity": 1,
  "acquisitionCostPerItem": 0,
  "guiClickDelay": 10,
  "itemDelay": 30,
  "commandDelay": 5,
  "guiTimeout": 100,
  "autoConfirmGui": true,
  "chatNotifications": true,
  "discordWebhookEnabled": false,
  "discordWebhookUrl": "",
  "confirmSlotIndex": 15,
  "guiTitleContains": "",
  "autoOrder": true,
  "orderCommand": "order",
  "protectedItems": [
    "minecraft:diamond",
    "minecraft:netherite_ingot",
    "minecraft:elytra"
  ]
}
```

| Trường | Mô tả | Mặc định |
|--------|-------|----------|
| `defaultPrice` | Giá bán mặc định khi dùng `/asell` không có tham số | `100` |
| `targetItem` | Item cần bán (registry ID) | `minecraft:chest` |
| `desiredQuantity` | Số lượng mỗi lần bán | `1` |
| `guiClickDelay` | Tick chờ trước khi click GUI xác nhận | `10` |
| `itemDelay` | Tick chờ giữa các lần bán | `30` |
| `commandDelay` | Tick chờ sau khi gửi lệnh `/ah sell` | `5` |
| `guiTimeout` | Tick timeout chờ GUI mở | `100` |
| `autoConfirmGui` | Tự click xác nhận trong GUI | `true` |
| `chatNotifications` | Hiện thông báo trong chat | `true` |
| `acquisitionCostPerItem` | Cost mua mỗi item từ order để tính profit | `0` |
| `discordWebhookEnabled` | Bật thông báo Discord tùy chọn | `false` |
| `discordWebhookUrl` | Discord webhook URL, chỉ lưu local | `""` |
| `confirmSlotIndex` | Slot fallback nếu auto-detect lỗi | `15` |
| `guiTitleContains` | Lọc GUI theo tiêu đề (để trống = bỏ qua) | `""` |
| `autoOrder` | Tự lấy đồ từ `/order` khi hết hàng | `false` |
| `orderCommand` | Lệnh order tùy chỉnh | `"order"` |
| `protectedItems` | Danh sách item không bao giờ bị vứt | xem config |

---

## 🛡️ Tính năng chống ban

ASell được thiết kế để tránh bị phát hiện là bot:

1. **Random delay**: Thời gian giữa các lần bán ngẫu nhiên trong khoảng ±50% so với giá trị đặt
2. **Break Simulator**: Tự động nghỉ 5–10 giây sau mỗi 10–20 lần bán liên tiếp
3. **Staff Monitor**: Ngay khi phát hiện whisper/tin nhắn riêng từ admin → dừng ngay + phát âm thanh cảnh báo
4. **World change detection**: Tự dừng khi đổi server/world
5. **Disconnect detection**: Tự dừng khi mất kết nối
6. **No mixin**: Không hook vào game code, giảm nguy cơ bị phát hiện bởi anti-cheat

### ⚠️ Lưu ý

- Tắt **"Pause on Lost Focus"** trong Video Settings nếu muốn mod chạy khi alt-tab
- Mod hoạt động trên client-side, không gửi packet bất thường
- Vẫn có rủi ro khi sử dụng trên server có hệ thống anti-bot nghiêm ngặt

---

### Discord webhook tùy chọn

Webhook mặc định tắt. Để bật, sửa `config/asell.json` trong profile Minecraft (không commit URL webhook vào Git):

```json
{
  "discordWebhookEnabled": true,
  "discordWebhookUrl": "https://discord.com/api/webhooks/..."
}
```

Mod chỉ chấp nhận HTTPS webhook của `discord.com` hoặc `discordapp.com` và gửi thông báo bất đồng bộ khi bắt đầu, collect order, list thành công, xác nhận sale, hoàn tất hoặc dừng do lỗi.

Financial report gồm số item collected/listed/sold, gross listed value, realized revenue, cost mỗi item, projected profit và realized profit. Đặt cost bằng `/asell cost 300k`, kiểm tra bằng `/asell status`, hoặc gửi snapshot Discord bằng `/asell report`.

## 🔧 Build từ source

```bash
git clone https://github.com/nguyenttuca/asell-mod.git
cd asell-mod
./gradlew build
# Output: build/libs/asell-fabric-1.21.1-*.jar
```

Yêu cầu: JDK 21+

---

## 🤝 Đóng góp

Pull request và issue đều được hoan nghênh!

1. Fork repo
2. Tạo branch mới: `git checkout -b feature/ten-tinh-nang`
3. Commit: `git commit -m "feat: thêm tính năng X"`
4. Push và mở PR

---

## 📄 License

MIT License — Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

---

<div align="center">

Made with ❤️ by **[nguyenttuca](https://github.com/nguyenttuca)**

⭐ Nếu mod hữu ích, hãy cho một star nhé!

</div>
