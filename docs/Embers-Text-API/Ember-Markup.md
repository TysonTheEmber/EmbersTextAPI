# Ember Markup

Ember Markup is a lightweight tag language that lets you sprinkle animations and
styling directly into chat lines, tooltips and in-world quest text.

## Syntax

* Tags use the familiar `<tag>...</tag>` structure.
* Self-closing tags (`<shadow/>`) are supported.
* Attributes follow `key=value` format. Hex colours can be written as `#ff6600`.
* Use `\<` to insert a literal `<` into the output.
* Unknown or malformed tags are ignored so raw user input never breaks rendering.

### Examples

```text
<gradient from=#ff6a00 to=#ffd500><bold>EMBERCRAFT</bold></gradient>
Boss <shake amp=1.2>IGNIS</shake> approaches <fade in=10 out=10>now</fade>!
<typewriter speed=2><color value=#fff7e6>Quest Updated!</color></typewriter>
```

## Java DSL

```java
RSpan ast = Rich.text("Boss ")
    .then(Rich.span("shake").attr("amp", "1.2").text("IGNIS").close())
    .span("fade").attr("in", "10").attr("out", "10").text("now").close()
    .build();

player.displayClientMessage(EmberMarkup.toComponent(ast), false);
```

## Rendering helpers

* `EmberMarkup.parse(String)` → root AST span.
* `EmberMarkup.toComponent(String|RSpan)` → vanilla-friendly component tree.
* `EmberMarkup.draw(GuiGraphics, …)` → convenience draw call for UIs.

## Built-in attributes

| Tag | Description |
| --- | ----------- |
| `color` | Applies a vanilla text colour. |
| `bold`, `italic`, `underline`, `strikethrough`, `obfuscated` | Standard style toggles. |
| `font` | Switch to a custom font key. |
| `gradient` | Fades from `from` → `to`. Overlay renderer upgrades it to per-glyph colours. |
| `wave`, `shake`, `typewriter`, `fade`, `shadow`, `outline`, `bg` | Overlay-driven effects with vanilla fallbacks. |

## Preview command

Use `/emberstextapi preview <markup>` in a client-integrated world to see how a
string renders without writing a full script or datapack.
