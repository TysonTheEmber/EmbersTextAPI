## v2.7.0

### Config System Expansion
- Added **`immersiveMessagesEnabled`** (client) — Master toggle for immersive messages. When disabled, all immersive messages are silently ignored on the client.
- Added **`disabledEffects`** (client) — List of effect names to disable globally (e.g. `["glitch", "shake"]`). Disabled effects render as plain text.
- Added **`markupPermissionMode`** (server) — Controls which players can use markup tags in chat: `NONE` (default, no restrictions), `WHITELIST`, or `BLACKLIST`.
- Added **`markupPlayerList`** (server) — Player UUIDs for the markup whitelist/blacklist.
- Added **`maxMessageDuration`** (client) — Cap on immersive message duration in ticks. `0` = unlimited.
- Added **`maxActiveMessages`** (client) — Cap on simultaneous on-screen messages. `0` = unlimited.

### Config File Layout
- **Forge / NeoForge:** Config is now split into two files:
  - `emberstextapi-common.toml` — Server-side options (welcome message, markup permissions)
  - `emberstextapi-client.toml` — Client-side options (immersive messages toggle, disabled effects, limits)
- **Fabric:** All options in a single `emberstextapi.json` file (unchanged location).

### Chat Markup Stripping
- Server operators can now restrict which players use markup tags like `<rainbow>` in chat via the `markupPermissionMode` and `markupPlayerList` config options.
- When a player is not allowed to use markup, all tags are silently stripped from their chat messages, leaving plain text.

### Internal
- Added `NoOpEffect` class — a no-op effect used as a silent replacement for disabled effects.
- Added `MarkupStripper` utility — shared regex-based markup tag stripping used by all chat event handlers.
- Added `ChatMarkupHandler` (Forge/NeoForge) and `FabricChatMarkupHandler` (Fabric) for server-side chat filtering.
- `ClientMessageManager.open()` now enforces `immersiveMessagesEnabled` and `maxActiveMessages` client-side.
- Added `ClientMessageManager.getActiveMessageCount()`.

## Color
- Color effect now accepts `value` as an alternative to `col` (e.g. `<color value=FF0000>`)
- `<color value=HEX>` now routes through the effect system, making it stackable with other effects
