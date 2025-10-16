# Ember Markup

Ember Markup is a lightweight tag language that lets you express complex visual
effects in text. The parser is dependency free and produces a tiny AST that can
be consumed either by the bundled Component emitter or the overlay renderer.

## Syntax quick reference

```text
<bold>Hello</bold>
<gradient from=#ff6a00 to=#ffd500><bold>EMBERCRAFT</bold></gradient>
Boss <shake amp=1.2>IGNIS</shake> approaches <fade in=10 out=10>now</fade>!
```

* Escape a literal `<` with `\<`.
* Tags support attributes (`key=value` or quoted values).
* Unknown tags can be ignored or left verbatim via config.

## Java DSL

```java
RNode ast = Rich.text("Boss ")
    .then(Rich.span("shake").attr("amp", "1.2").text("IGNIS").close())
    .then(Rich.text(" approaches ")
            .span("fade").attr("in", "10").attr("out", "10").text("now").close())
    .build();
```

## Rendering

* `EmberMarkup.toComponent(String)` – best-effort vanilla fallback.
* `EmberMarkup.draw(GuiGraphics, RNode, float, float, DrawOptions)` – overlay assisted drawing.

The overlay pass queues spans tagged with advanced attributes (gradient, wave,
etc.) so client-side modules can animate them later.

## Built-in attributes

| Tag | Description |
| --- | --- |
| `color` | Sets the vanilla text color |
| `bold`, `italic`, `underline`, `strikethrough`, `obfuscated` | Map to existing `Style` flags |
| `font` | Switches to a namespaced font |
| `gradient`, `wave`, `shake`, `typewriter`, `fade` | Marked for overlay animation |

## Testing markup in-game

Use `/emberstextapi preview <markup>` to preview how a string renders. The
command works on the client even when a server does not know about Ember Markup.

For more elaborate integration examples, see the README section “Ember Markup”.
