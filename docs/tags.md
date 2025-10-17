# Embers Text Tags

The Embers Text API lets you add animation and styling to any text surface by
embedding lightweight tags in your strings. Tags look like XML and can be
nested. They never reach the server – all parsing happens on the client.

```text
<grad from=#ff6a00 to=#ffd500><bold>EMBERCRAFT</bold></grad>
<typewriter speed=30 by=char>Incoming…</typewriter>
<wiggle a=1.2 f=2>Alert!</wiggle>
```

## Syntax

* Opening tags: `<name key=value ...>` (namespace defaults to `embers` if
  omitted).
* Closing tags: `</name>`.
* Self-closing tags: `<name .../>`.
* Escaping: use `\<` or `&lt;` for a literal `<` character.
* Values can be numbers, booleans, `#RRGGBB` colors, or quoted strings.

Malformed tags are ignored. When debug logging is enabled (or when the
`client.dev.showTagWarnings` config is set to `true`) the parser logs helpful
messages that make it easier to find mismatched or unknown tags.

## Built-in effects

| Tag | Parameters | Description |
| --- | ---------- | ----------- |
| `bold` | _(none)_ | Toggles bold glyphs for the covered text. |
| `italic` | _(none)_ | Draws the span in italic. |
| `color` | `value` (`#RRGGBB`, named) | Forces an RGB color for the span. |
| `grad` | `from`, `to`, `hue` (bool, default `false`), `f` (float, flow speed), `sp` (float span scale), `uni` (bool uniform phase) | Animating gradient that lerps either in RGB or HSV space. |
| `typewriter` | `speed` (float, glyphs per second), `by` (`char`/`word`), `delay` (float) | Reveals text progressively. |
| `wiggle`/`shake` | `a` (amplitude), `f` (frequency), `w` (wavelength) | Adds positional jitter or wave motion. |
| `fade` | `in` (seconds), `out` (seconds) | Adjusts alpha over time. |
| `shadow` | `offset` (pixels), `alpha` (0-1) | Adds a drop shadow behind glyphs. |

## Cookbook

* **Fire gradient heading**
  ```text
  <grad from=#ff6a00 to=#ffd500><bold>EMBERCRAFT</bold></grad>
  ```
* **Typewriter alert**
  ```text
  <typewriter speed=28 by=char>Incoming transmission…</typewriter>
  ```
* **Wiggle highlight**
  ```text
  <wiggle a=1.2 f=2>Danger!</wiggle>
  ```
* **HSV rainbow marquee**
  ```text
  <grad hue=true f=0.35 sp=18 uni=false>~ RAINBOW ~</grad>
  ```

## Migration tips

| Builder API | Tag equivalent |
| ----------- | -------------- |
| `.typewriter(2f, true)` | `<typewriter speed=40 by=char>…</typewriter>` |
| `.charShake(true)` | `<wiggle a=1.2 f=2>…</wiggle>` |
| `.color(ChatFormatting.GOLD)` | `<color value=gold>…</color>` |

Existing builder calls continue to work; tags are an additive feature. The
builder now also exposes `.text(String)` (auto-parsed) and `.attributed` for
programmatic control.

## Demo command

```
/emberstextapi send @p 120 <grad from=#ff6a00 to=#ffd500><bold>EMBERCRAFT</bold></grad>
```

For more examples check the in-game `/emberstextapi sendcustom` command which
now accepts tagged strings.
