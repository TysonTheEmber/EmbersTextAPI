# Typewriter Effect - Implementation Guide

## Overview

The typewriter effect reveals text character-by-character, automatically resetting every time the text becomes visible. This creates a fresh animation for tooltips and quest descriptions each time they are viewed.

## Features

- **Automatic Reset**: Animation restarts from character 0 every time text is viewed
- **Tooltip Tracking**: Resets when hovering over items
- **Screen Tracking**: Resets when opening screens (including FTB Quests)
- **Independent Contexts**: Different tooltips/screens have independent animations
- **Configurable Speed**: Control how fast text appears
- **Initial Delay**: Add delay before animation starts
- **Cycling**: Optional repeating animation

## Basic Usage

### Simple Typewriter

```xml
<typewriter>This text appears character by character</typewriter>
```

Default behavior:
- Speed: 20 characters per second
- No initial delay
- Does not cycle

### Custom Speed

```xml
<typewriter s=30>Fast typing (30 chars/sec)</typewriter>
<typewriter s=10>Slow typing (10 chars/sec)</typewriter>
```

### Initial Delay

```xml
<typewriter s=20 d=1.0>Waits 1 second, then types at 20 chars/sec</typewriter>
<typewriter d=0.5>Half second delay before typing</typewriter>
```

### Cycling Animation

```xml
<typewriter s=15 c=true>This text repeats indefinitely</typewriter>
```

### Custom Context ID

```xml
<typewriter s=20 id=quest_123>Quest-specific typewriter</typewriter>
```

Use custom IDs when you want fine-grained control over reset behavior.

## Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `s` | float | 20.0 | Characters per second reveal rate |
| `d` | float | 0.0 | Initial delay in seconds before animation starts |
| `c` | boolean | false | Whether to cycle the animation (restart when complete) |
| `id` | string | auto | Custom context identifier for independent tracking |

## Reset Behavior

### Tooltip Reset Triggers

The typewriter effect resets when:

1. **Hovering over a new item**: Each different item triggers a reset
2. **Re-hovering the same item**: Moving away and back triggers a reset
3. **Item count changes**: Same item with different stack size triggers a reset
4. **NBT data changes**: Items with different NBT (enchantments, etc.) trigger reset

Example scenarios:
- Hover over Diamond Sword → animation plays
- Move away, hover again → animation plays from start
- Hover over Enchanted Diamond Sword → animation plays (different NBT)

### Screen Reset Triggers

The typewriter effect resets when:

1. **Opening a screen**: First time opening triggers reset
2. **Changing screens**: Switching between screens triggers reset
3. **Reopening same screen**: Closing and reopening triggers reset
4. **FTB Quest changes**: Viewing different quests triggers reset

### Context Isolation

Each unique context maintains its own animation state:

```xml
<!-- Item tooltip -->
<typewriter id=tooltip_diamond>Diamond tooltip text</typewriter>

<!-- Quest description -->
<typewriter id=quest_main_1>Quest description text</typewriter>

<!-- Another quest -->
<typewriter id=quest_side_3>Different quest text</typewriter>
```

These three typewriters run independently and reset independently.

## Combining with Other Effects

Typewriter can be combined with other effects for enhanced visuals:

### Typewriter + Rainbow

```xml
<rainbow><typewriter>Colorful typing effect</typewriter></rainbow>
```

### Typewriter + Wave

```xml
<wave><typewriter>Wavy typing effect</typewriter></wave>
```

### Typewriter + Fade

```xml
<fade><typewriter s=25>Fading typewriter</typewriter></fade>
```

### Multiple Effects Stacked

```xml
<rainbow>
  <wave>
    <typewriter s=15>
      Rainbow wavy typewriter effect!
    </typewriter>
  </wave>
</rainbow>
```

## FTB Quests Integration

### Basic Quest Text

```json
{
  "title": "<typewriter>Welcome to the Quest!</typewriter>",
  "description": "<typewriter s=25>This is the quest description that appears character by character.</typewriter>"
}
```

### Multi-Paragraph Quest

```json
{
  "description": "<typewriter s=20>First paragraph appears.</typewriter>\n\n<typewriter s=20 d=1.0>Second paragraph appears after a delay.</typewriter>"
}
```

### Quest with Effects

```json
{
  "title": "<rainbow><typewriter>Epic Quest Title</typewriter></rainbow>",
  "description": "<typewriter s=25>Complete the following tasks:\n\n<bold>- Collect 10 Diamonds\n- Defeat the Dragon\n- Craft the Artifact</bold></typewriter>"
}
```

## Item Tooltip Examples

### Custom Item Lore

```java
Component lore = Component.literal("")
    .append(Component.literal("<typewriter s=30>")
    .append(Component.literal("This legendary sword was forged in dragon fire...")
    .append(Component.literal("</typewriter>")));

stack.getOrCreateTag().put("display", new CompoundTag());
// Add lore using appropriate NBT methods
```

### Enchantment Description

```xml
<typewriter s=25 d=0.5>
  <gradient from=#FF0000 to=#FFFF00>
    Legendary Enchantment
  </gradient>
</typewriter>
```

## Advanced Usage

### Per-Character Speed Control

For truly variable speed, you can use multiple typewriter spans with different delays:

```xml
<typewriter s=30>Fast start</typewriter>
<typewriter s=10 d=0.5> then slow middle</typewriter>
<typewriter s=30 d=1.0> and fast end!</typewriter>
```

### Quest Stages with Typewriter

```json
{
  "quest_1": {
    "description": "<typewriter s=20 id=quest_1_desc>Stage 1: Gather resources</typewriter>"
  },
  "quest_2": {
    "description": "<typewriter s=20 id=quest_2_desc>Stage 2: Build the structure</typewriter>"
  }
}
```

Each stage maintains independent animation state.

### Conditional Typewriter

You can use the custom ID system to control which contexts get typewriter effects:

```xml
<!-- Only applies to tutorial tooltips -->
<typewriter s=15 id=tutorial>Welcome! This is a tutorial item.</typewriter>

<!-- Only applies to quest items -->
<typewriter s=20 id=quest_item>This item is required for the quest.</typewriter>
```

## Technical Details

### View State Tracking

The typewriter effect uses `ViewStateTracker` to monitor when text becomes visible:

- Tooltips are tracked via `Screen.renderTooltip()` mixin
- Screens are tracked via Forge screen events
- FTB Quests are tracked via pseudo-mixin (optional, requires FTB Quests)

### Performance

- **Minimal overhead**: Only tracks active contexts
- **Thread-safe**: Safe for concurrent rendering
- **Memory efficient**: Uses ConcurrentHashMap for tracking
- **No polling**: Event-driven updates only

### Compatibility

- **Minecraft Version**: 1.20.1
- **Mod Loader**: Forge
- **FTB Quests**: Optional integration via pseudo-mixin
- **Other Mods**: Compatible with any mod using standard tooltip/screen systems

## Troubleshooting

### Effect Not Resetting

**Problem**: Typewriter doesn't reset when expected

**Solutions**:
1. Check logs for ViewStateTracker messages (set log level to DEBUG)
2. Verify tooltip/screen is using standard Minecraft rendering
3. Try using a custom `id` parameter for explicit control

### Effect Too Fast/Slow

**Problem**: Animation speed doesn't feel right

**Solutions**:
1. Adjust `s` parameter (try values between 10-40)
2. Add initial delay with `d` parameter
3. Consider text length - longer text may need faster speed

### FTB Quests Not Resetting

**Problem**: Quest descriptions don't reset properly

**Solutions**:
1. Ensure FTB Quests is installed (pseudo-mixin requires it)
2. Check FTB Quests version compatibility
3. Use custom `id` for each quest: `<typewriter id=quest_unique_id>`

### Conflicts with Other Effects

**Problem**: Typewriter interferes with other effects

**Solutions**:
1. Check effect order - typewriter should generally be outermost
2. Some effects modify alpha - combine carefully
3. Use effect parameters to fine-tune behavior

## Examples Directory

See `/examples/typewriter/` for complete working examples:

- `basic_tooltip.txt` - Simple item tooltip examples
- `ftb_quests.json` - FTB Quests integration examples
- `advanced_combinations.txt` - Complex multi-effect examples

## API Usage

### Programmatic Access

```java
import net.tysontheember.emberstextapi.util.ViewStateTracker;

// Manual reset
ViewStateTracker.resetContext("my_custom_context");

// Mark view started
ViewStateTracker.markViewStarted("my_custom_context");

// Check current tooltip
String tooltip = ViewStateTracker.getCurrentTooltipContext();

// Mark screen opened
ViewStateTracker.markScreenOpened("MyCustomScreen");

// Mark quest viewed
ViewStateTracker.markQuestViewed("quest_123");
```

### Custom Integration

To integrate typewriter reset with your own GUI:

```java
@SubscribeEvent
public static void onMyCustomGUIRender(MyCustomGUIRenderEvent event) {
    // Mark your GUI as a view context
    ViewStateTracker.markViewStarted("custom_gui:" + event.getGuiId());

    // Now typewriter effects in your GUI will reset
}
```

## Best Practices

1. **Speed Selection**: Use 15-25 chars/sec for normal reading speed
2. **Delay Usage**: Add 0.5-1.0 second delay for multi-paragraph text
3. **Context IDs**: Use descriptive IDs like `quest_main_1`, `tooltip_tutorial_sword`
4. **Effect Ordering**: Place typewriter as outermost effect when combining
5. **Testing**: Test with different text lengths to ensure good pacing
6. **Performance**: Avoid excessive cycling animations on busy screens

## Version History

### v2.1.0
- Initial typewriter effect implementation
- ViewStateTracker for automatic reset detection
- Tooltip and screen integration
- FTB Quests support (optional)
- Event-based view tracking

## Support

For issues, questions, or feature requests:
- GitHub Issues: [Your repository URL]
- Discord: [Your Discord server]
- Documentation: [Your docs URL]
