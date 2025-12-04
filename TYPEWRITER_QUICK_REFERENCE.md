# Typewriter Effect - Quick Reference Card

## Markup Syntax

```xml
<typewriter [s=speed] [d=delay] [c=cycle] [id=contextId]>text</typewriter>
```

## Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `s` | float | 20.0 | Characters per second |
| `d` | float | 0.0 | Initial delay (seconds) |
| `c` | bool | false | Cycle/repeat animation |
| `id` | string | auto | Custom context ID |

## Common Patterns

### Basic Usage
```xml
<typewriter>Default 20 chars/sec</typewriter>
<typewriter s=30>Fast typing</typewriter>
<typewriter s=10>Slow typing</typewriter>
<typewriter d=1.0>1 second delay</typewriter>
```

### With Other Effects
```xml
<rainbow><typewriter>Rainbow typing</typewriter></rainbow>
<wave><typewriter>Wavy typing</typewriter></wave>
<gradient from=#FF0000 to=#0000FF><typewriter>Gradient</typewriter></gradient>
```

### FTB Quests
```json
{
  "title": "<typewriter s=30>Quest Title</typewriter>",
  "description": "<typewriter s=25>Quest description here...</typewriter>"
}
```

## Reset Triggers

| Event | When It Resets |
|-------|---------------|
| **Tooltip** | Hover over item, re-hover, different item, NBT change |
| **Screen** | Open screen, change screen, reopen screen |
| **Quest** | View quest, change quest, reopen quest |
| **Custom** | Call `ViewStateTracker.resetContext(id)` |

## API Quick Reference

```java
import net.tysontheember.emberstextapi.util.ViewStateTracker;

// Reset animation manually
ViewStateTracker.resetContext("my_context");

// Mark view started (same as reset)
ViewStateTracker.markViewStarted("my_context");

// Screen tracking
ViewStateTracker.markScreenOpened("MyScreen");
ViewStateTracker.markScreenClosed();

// Quest tracking
ViewStateTracker.markQuestViewed("quest_123");

// Get current contexts
String tooltip = ViewStateTracker.getCurrentTooltipContext();
String screen = ViewStateTracker.getCurrentScreenContext();

// Get view start time
long startTime = ViewStateTracker.getViewStartTime("my_context");
```

## File Locations

| Component | Path |
|-----------|------|
| Effect Class | `/src/main/java/.../effects/visual/TypewriterEffect.java` |
| View Tracker | `/src/main/java/.../util/ViewStateTracker.java` |
| Event Handler | `/src/main/java/.../client/ViewStateEventHandler.java` |
| Screen Mixin | `/src/main/java/.../mixin/client/ScreenMixin.java` |
| Quest Mixin | `/src/main/java/.../mixin/client/ftbquests/QuestScreenMixin.java` |

## Speed Guide

| Speed | Description | Best For |
|-------|-------------|----------|
| 5-10 | Very slow | Dramatic reveals, horror |
| 15-20 | Normal | Regular tooltips, dialogue |
| 25-30 | Fast | Quest titles, short text |
| 40+ | Very fast | Action text, effects |

## Troubleshooting

### Not Resetting?
1. Enable DEBUG logging: check for ViewStateTracker messages
2. Try custom ID: `<typewriter id=unique_name>`
3. Verify standard rendering is used

### Too Fast/Slow?
1. Adjust `s` parameter (try 15-25 range)
2. Add delay: `<typewriter d=0.5>`

### FTB Quests Issues?
1. Ensure FTB Quests is installed
2. Use custom ID per quest: `<typewriter id=quest_1>`

## Effect Combinations

```xml
<!-- Rainbow + Typewriter -->
<rainbow><typewriter>Text</typewriter></rainbow>

<!-- Multiple Effects -->
<rainbow><wave><typewriter s=20>Text</typewriter></wave></rainbow>

<!-- Gradient + Shadow + Typewriter -->
<gradient from=#FF0000 to=#0000FF>
  <shadow x=2 y=2>
    <typewriter s=25>Text</typewriter>
  </shadow>
</gradient>

<!-- Neon + Typewriter (cyberpunk) -->
<neon><typewriter s=30>CYBER TEXT</typewriter></neon>

<!-- Glitch + Typewriter (horror) -->
<glitch f=2.0><typewriter s=15>Gl1tch3d</typewriter></glitch>
```

## Integration Examples

### Custom Screen Integration
```java
@SubscribeEvent
public static void onMyScreenRender(MyScreenRenderEvent event) {
    ViewStateTracker.markViewStarted("screen:myscreen:" + event.getScreenId());
}
```

### Custom Tooltip Integration
```java
Component tooltip = Component.literal(
    "<typewriter s=25 id=my_tooltip>My custom tooltip text</typewriter>"
);
```

### Quest Integration
```java
// In quest JSON or builder
{
  "description": "<typewriter s=20 id=quest_" + questId + ">Description</typewriter>"
}
```

## Performance Notes

- **Memory**: ~1KB per tracked context
- **CPU**: O(1) per character render
- **Typical Usage**: < 100 contexts tracked
- **Thread-Safe**: Yes (ConcurrentHashMap)
- **Server Load**: None (client-only)

## Version Info

- **Minecraft**: 1.20.1
- **Mod Loader**: Forge
- **API Version**: v2.1.0+
- **FTB Quests**: Optional

## Support

- Full Guide: `TYPEWRITER_EFFECT_GUIDE.md`
- Examples: `typewriter_examples.txt`
- Implementation: `TYPEWRITER_IMPLEMENTATION_SUMMARY.md`
