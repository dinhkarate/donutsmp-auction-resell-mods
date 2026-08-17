# Known Risks & Limitations

This file documents known risks, detection surface, and limitations of this
mod. Read it before using the bot on any server.

## 1. Server rules & ban risk (the big one)

This is an **automation mod**. Even though it only sends the same commands and
clicks a human player would, most servers — including DonutSMP — can and do ban
automation. **Use at your own risk.**

Recommended mitigations (configurable, see README):

| Setting | Default | Why |
|---------|---------|-----|
| `maxRunMinutes` | `0` (unlimited) | Player-set cap if you ever want an auto-stop; runs 24/7 by default |
| `itemDelay` | `30` ticks | Player can raise (up to 600 via `/asell delay`) for a slower cadence |
| `breakAfterSalesMin/Max` | `10/20` | Player-adjustable human-like break frequency |
| `breakSecondsMin/Max` | `5/10` | Player-adjustable break length |
| `ownPriceCheckChance` | `1.0` | Player can lower (e.g. `0.5`) to skip the own-listing scan sometimes |

## 2. Behavioral fingerprints

The server does not need to read the mod to flag the account. It sees behavior:

- A **regular `/ah sell <price>` cadence** (1–3s apart for hours).
- The **two-phase scan pattern**: `/ah <player> <item>` → `/ah <item>` →
  `/ah sell X`, repeated on a fixed cycle before every listing.
- Synthetic inventory clicks (swap/select packets) without real mouse input.
- Zero player activity: no movement, no chat, no item pickup.

`ownPriceCheckChance`, larger delays, and `maxRunMinutes` reduce the
regularity of these patterns; they do not make the bot undetectable.

## 3. "Invalid session" / "restart your game and launcher" kicks

Frequent kicks with this message are usually the server's **anti-bot
session-check**, not an expired Microsoft token. The mod auto-reconnects
(`autoReconnect`) and stops after `maxReconnectAttempts` with a webhook alert.

## 4. Use a separate account

The safest pattern is to run the bot on an account you are prepared to lose.
Never run it on your main account if that account matters to you.

## 5. Repository is intentionally public

This repository is public by choice (shared on Reddit / socials). That means
server staff can read the exact code and fingerprint the behavior above.
Do not commit `discordWebhookUrl` or any other secret — the webhook URL lives
only in the local `config/asell.json`.

## 6. Known technical limitations

- **Swap clicks can be rejected** on some server versions for items stored in
  the main inventory (slots 9–35). Hotbar items are selected via the
  `UpdateSelectedSlotC2SPacket` path and work reliably; main-inventory items
  may hit the swap-attempt cap and stop with a warning.
- **Order GUI flow** (`/order` → Your Orders → Collect) is tuned for the
  DonutSMP plugin; other Auction House plugins may need small adjustments.
- **llms.txt** is provided for non-Google AI crawlers; Google Search ignores
  it by design (per Google's AI optimization guide, 2026).

## 7. Not a guarantee

None of the settings above guarantee you won't be flagged or banned.
Anti-cheat systems evolve. If you get banned, don't blame the tool — accept
the risk before you run it.
