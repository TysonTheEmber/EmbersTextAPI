# Ember Markup

Ember Markup extends vanilla chat components with a tiny tag-based language.

```
<gradient from=#ff6a00 to=#ffd500><bold>EMBERCRAFT</bold></gradient>
```

## Syntax

- Nested `<tag> ... </tag>` spans.
- Self-closing tags: `<shake/>`.
- Attributes with `key=value` or shorthand values: `<color #ff7700>`.
- Escape `<` with `\<` when you need a literal character.

## Java DSL

```java
RNode ast = Rich.text("Boss ")
    .then(Rich.span("shake").attr("amp", "1.2").text("IGNIS").close())
    .then(Rich.text(" approaches ").span("fade").attr("in", "10").attr("out", "10").text("now").close())
    .build();
```

Render via:

```java
EmberMarkup.draw(guiGraphics, ast, 8, 8, EmberMarkup.defaults());
```
