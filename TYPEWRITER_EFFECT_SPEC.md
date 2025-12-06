# Typewriter Effect Specification

## Goals
- Independent typewriter instances per render location; restarting in one must not affect any other.
- Text reveals in natural reading order, respecting existing UI wrapping: left→right within each wrapped line, then next line top-to-bottom.
- Timing is tick-based like other effects; linear per-character delay (speed expressed per tick). We can switch to wall-clock later if needed.
- No cursor and no skip-to-end.
- Optional per-character sound; default silent.
- Global config toggle can disable all typewriter effects.

## Tag Usage
Use the `<typewriter>` tag around any text block.

Attributes:
- `speed`: integer milliseconds per character (e.g., `speed="20"`).
- `sound`: `off` (default) or a sound ID string (e.g., `sound="minecraft:block.note_block.hat"`). Sound plays per character when enabled.
- `type`: `once` or `repeat`. If omitted, the system auto-detects based on render context (see below); explicit value overrides detection.

Examples:
- `<typewriter speed="18" type="once">Hello world!</typewriter>`
- `<typewriter speed="12" sound="minecraft:block.note_block.hat" type="repeat">Hover text</typewriter>`

## Behavior by Context
- **Chat messages**: defaults to `type="once"`; animates when the message is sent, then stays revealed on revisit.
- **Tooltips (e.g., item hovers)**: defaults to `type="repeat"`; restarts every time the tooltip is shown.
- **FTB Quests description panel**: defaults to `type="repeat"`; restarts each time the panel opens.
- **Others**: defaults to `type="repeat"` restarts every time its viewed
- Explicit `type` always overrides these defaults.

## Rendering Rules
- Reuse the host UI’s existing wrapping logic (`lineWrapProvider`) so layout is accurate and no extra reflow occurs.
- Reveal characters in wrap order: left→right within the current wrapped line, then proceed line-by-line top→bottom.
- Sound (when set) triggers per character. No cursor is drawn. Skip-to-end is disabled.

## Lifecycle and Control
- A `TypewriterController` manages instances keyed by content identity; each instance holds `(text, speed, sound, type, lineWrapProvider)`.
- UI layers request an instance per renderable text block and call `tick(deltaTicks)` and `render()` each frame.
- Reset rules:
  - `type="once"`: run once per content instance; cache completed state.
  - `type="repeat"`: reset whenever the UI element reappears (tooltip reopen, quest screen reopen, etc.).
- Global config flag can short-circuit the controller to render full text immediately when typewriter is disabled.

## Performance Notes
- Drive timing from game ticks for consistency with other effects (current default). If a future wall-clock mode is added, it should be opt-in.
- Keep updates lightweight; avoid extra layout passes by delegating wrapping to existing UI components.
- No hard cap on text length; if needed in the future, a guard can skip animation for very large strings to prevent lag.
