## 🔍 Overview

Ember’s Text API lets your mod display polished, animated text overlays without building a custom renderer. Messages can include gradients, backgrounds, fonts, typewriter effects, and shake animations, all controlled through a fluent builder API.

 

## **Why make this?**

**Immersive Messaging API**(which this was built to replace) uses her lib, **txnilib**, which jar-in-jars(aka has another mod inside her mod) **Forgified Fabric API**, making all of his FORGE mods have FABRIC overhead. That, along with the features I want that IM didn't have(like multiple attributes working together like obfuscation and typewriter, and gradients), made me want to make this, so... cheers 🍻

***

## ✨ Features

*   **Fluent Builder**: Create `ImmersiveMessage` instances with chaining (`gradient`, `background`, `typewriter`, `shake`, `wrap`, etc.).
*   **Client Networking**: `EmbersTextAPI.sendMessage(ServerPlayer, ImmersiveMessage)` packs the message and sends it over a `SimpleChannel`.
*   **Demo Command**: `/emberstextapi test <id>` showcases nine sample effects out of the box.
*   **Custom Fonts & Backgrounds**: Place font files under `assets/emberstextapi/font/`, toggle tooltip-style backgrounds with gradient borders, or layer textured backgrounds with configurable scaling and padding.

***

## 🛠️ Developer Integration

### **Basic Example**

<div><div><div><div>&nbsp;</div></div></div><div><code>ImmersiveMessage msg = ImmersiveMessage.builder(80f, "Hello world")</code></div><div><code>&nbsp; &nbsp; .anchor(TextAnchor.CENTER_CENTER)</code></div><div><code>&nbsp; &nbsp; .offset(0f, -20f)</code></div><div><code>&nbsp; &nbsp; .gradient(0xFF0000, 0x00FF00)</code></div><div><code>&nbsp; &nbsp; .background(true) .typewriter(2f, true)</code></div><div><code>&nbsp; &nbsp; .fadeInTicks(10)</code></div><div><code>&nbsp; &nbsp; .fadeOutTicks(20)</code></div><div><code>&nbsp; &nbsp; .shake(ShakeType.WAVE, 1.5f);</code></div><div><code>EmbersTextAPI.sendMessage(player, msg);</code></div></div>

### Common Builder Methods

<div><div><table style="border-collapse: collapse; width: 100%; height: 285px; border-width: 1px; border-color: #ECF0F1; border-style: solid;" border="1"><thead><tr style="height: 25px;"><th style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 25px;">Method</th><th style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 25px;">Description</th></tr></thead><tbody><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>anchor(TextAnchor)</code>&nbsp;/&nbsp;<code>align(TextAnchor)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Screen anchor vs. text alignment</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>offset(float x, float y)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Move from anchor point</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>scale(float size)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Uniform size multiplier</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>color(int/String/ChatFormatting)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Single color</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>gradient(int... or String...)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Multi-stop text gradient</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>background(true)</code>&nbsp;/&nbsp;<code>bgColor(...)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Tooltip-style background (solid/gradient)</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>textureBackground(...)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Draw a textured quad behind text using the supplied sprite</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>textureBackgroundScale(...)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Scale U/V sampling before drawing (stretch vs. tile density)</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>textureBackgroundPadding(...)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Adjust empty space around the textured background quad</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>textureBackgroundSize/Width/Height(...)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Override draw dimensions separate from text bounds</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>textureBackgroundMode(...)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Choose stretching, cropping, or tiling behavior for the texture</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>borderGradient(int start, int end)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Gradient border colors</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>wrap(int width)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Line wrapping width</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>typewriter(speed [,center])</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Typewriter animation (chars per tick)</td></tr><tr style="height: 26px;"><td style="width: 49.9373%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;"><code>shake(ShakeType, float)</code>&nbsp;/&nbsp;<code>charShake(...)</code></td><td style="width: 50.0627%; border-width: 1px; border-color: rgb(236, 240, 241); height: 26px;">Whole-text or per-character shaking</td></tr></tbody></table><p>Custom fonts require both a <code>.json</code>&nbsp;and font file in&nbsp;<code>src/main/resources/assets/emberstextapi/font/</code>.</p></div></div>

***

 

## 📜 Commands

<div><div><table style="border-collapse: collapse; width: 100%; height: 103px; border-width: 1px; border-color: #FFFFFF; border-style: solid;" border="1"><thead><tr style="height: 25px;"><th style="width: 65.7658%; border-color: rgb(255, 255, 255); border-width: 1px; height: 25px;">Command</th><th style="width: 34.2342%; border-color: rgb(255, 255, 255); border-width: 1px; height: 25px;">Purpose</th></tr></thead><tbody><tr style="height: 26px;"><td style="width: 65.7658%; border-color: rgb(255, 255, 255); border-width: 1px; height: 26px;"><code>/emberstextapi test &lt;id&gt;</code></td><td style="width: 34.2342%; border-color: rgb(255, 255, 255); border-width: 1px; height: 26px;">Play built-in demonstration messages</td></tr><tr style="height: 26px;"><td style="width: 65.7658%; border-color: rgb(255, 255, 255); border-width: 1px; height: 26px;"><code>/emberstextapi send &lt;player&gt; &lt;duration&gt; [fadeIn] [fadeOut] &lt;text&gt;</code></td><td style="width: 34.2342%; border-color: rgb(255, 255, 255); border-width: 1px; height: 26px;">Send a basic message</td></tr><tr style="height: 26px;"><td style="width: 65.7658%; border-color: rgb(255, 255, 255); border-width: 1px; height: 26px;"><code>/emberstextapi sendcustom &lt;player&gt; &lt;nbt&gt; &lt;duration&gt; &lt;text&gt;</code></td><td style="width: 34.2342%; border-color: rgb(255, 255, 255); border-width: 1px; height: 26px;">Full control via NBT tags</td></tr></tbody></table></div></div>

 

### **Common NBT Tags for `sendcustom`**

| Tag                                                 |Purpose                                                                                             |Usage Example                                                                  |
| --------------------------------------------------- |--------------------------------------------------------------------------------------------------- |------------------------------------------------------------------------------ |
| <code>fadeIn</code>, <code>fadeOut</code>             |Fade durations before and after the main display (ticks).           |<code>{fadeIn:10,fadeOut:20}</code>                                                |
| <code>font</code>                                   |Apply a specific font resource to the message.                                                      |<code>{font:"modid:font_name"}</code>                                          |
| <code>bold</code>, <code>italic</code>, <code>underlined</code>, <code>strikethrough</code>, <code>obfuscated</code> |Enable text style flags. Set to <code>true</code> to apply.                                         |<code>{bold:true,italic:true}</code>                                           |
| <code>color</code>                                  |Set a single text color by name (<code>"red"</code>) or hex (<code>"#FF0000"</code>).               |<code>{color:"#FFAA00"}</code>                                                 |
| <code>gradient</code>                               |Apply a multi-stop text gradient (list or <code>{start,end}</code>).                                |<code>{gradient:["#FF0000","#00FF00"]}</code> or <code>{gradient:{start:"#FF0000",end:"#00FF00"}}</code> |
| <code>bgGradient</code>                             |Gradient fill for the background frame. Uses list or <code>{start,end}</code>; enables background automatically. |<code>{bgGradient:{start:"#FF000080",end:"#0000FF80"}}</code>                  |
| <code>borderGradient</code>                         |Gradient for the background border.                                                                 |<code>{borderGradient:["#FF0000","#00FF00"]}</code>                            |
| <code>bgColor</code>                                |Solid background color (implies background).                                                        |<code>{bgColor:"#333333CC"}</code>                                             |
| <code>borderColor</code>                            |Solid color for the border (implies background).                                                    |<code>{borderColor:"#FFFFFF"}</code>                                           |
| <code>textureBackground</code>                      |Apply a textured background.<br/>String form uses a single <code>ResourceLocation</code>; compound form supports:<br/><code>texture</code> (sprite id), <code>u</code>/<code>v</code>, <code>width</code>/<code>height</code>, atlas <code>textureWidth</code>/<code>textureHeight</code>, per-axis <code>paddingX</code>/<code>paddingY</code>, <code>scaleX</code>/<code>scaleY</code>, <code>sizeX</code>/<code>sizeY</code> (<code>drawWidth</code>/<code>drawHeight</code>), and <code>mode</code>/<code>resize</code> for stretch vs. crop.<br/>Examples: <code>{textureBackground:"modid:textures/gui/panel.png"}</code>, <code>{textureBackground:{texture:"modid:textures/gui/panel.png",mode:"STRETCH"}}</code>, <code>{textureBackground:{texture:"modid:textures/gui/panel.png",mode:"CROP",sizeX:120,sizeY:40}}</code> |<code>{textureBackground:{texture:"modid:textures/gui/panel.png",paddingX:6,paddingY:4,scaleX:0.5,scaleY:0.5}}</code> |
| <code>size</code>                                   |Uniform scale multiplier for text.                                                                  |<code>{size:1.5}</code>                                                        |
| <code>typewriter</code> &amp; <code>center</code>   |Animate text appearing at a given speed; optional <code>center</code> keeps text centered during animation. |<code>{typewriter:2.0,"center":true}</code>                                    |
| <code>background</code>                             |Toggle background frame on/off.                                                                     |<code>{background:true}</code>                                                 |
| <code>bgAlpha</code>                                |Background opacity (0–1).                                                                           |<code>{bgAlpha:0.5}</code>                                                     |
| <code>wrap</code>                                   |Wrap text at the given pixel width.                                                                 |<code>{wrap:120}</code>                                                        |
| <code>obfuscate</code> &amp; <code>obfuscateSpeed</code> |Gradually reveal obfuscated text using mode (<code>LEFT</code>, <code>RIGHT</code>, <code>CENTER</code>, <code>RANDOM</code>) and optional speed. |<code>{obfuscate:"LEFT",obfuscateSpeed:0.1}</code>                             |
| <code>anchor</code>                                 |Anchor screen position; values from <code>TextAnchor</code> (e.g., <code>TOP_LEFT</code>, <code>CENTER_CENTER</code>). |<code>{anchor:"CENTER_CENTER"}</code>                                          |
| <code>align</code>                                  |Text alignment relative to anchor (same enum as <code>anchor</code>).                               |<code>{align:"CENTER_CENTER"}</code>                                           |
| <code>offsetX</code>, <code>offsetY</code>          |Pixel offsets from anchor point.                                                                    |<code>{offsetX:10,offsetY:-20}</code>                                          |
| <code>shadow</code>                                 |Enable or disable drop shadow.                                                                      |<code>{shadow:true}</code>                                                     |
| <code>shakeWave</code> (<code>wave</code>*)         |Wave-like whole-message shake amplitude.                                                            |<code>{shakeWave:1.5}</code>                                                   |
| <code>shakeCircle</code> (<code>circle</code>*)     |Circular whole-message shake amplitude.                                                             |<code>{shakeCircle:1.0}</code>                                                 |
| <code>shakeRandom</code> (<code>random</code>*)     |Random jitter whole-message shake amplitude.                                                        |<code>{shakeRandom:0.8}</code>                                                 |
| <code>charShakeWave</code> (<code>waveChar</code>*) |Wave shake per character.                                                                           |<code>{charShakeWave:1.0}</code>                                               |
| <code>charShakeCircle</code> (<code>circleChar</code>*) |Circular shake per character.                                                                       |<code>{charShakeCircle:1.0}</code>                                             |
| <code>charShakeRandom</code> (<code>randomChar</code>*) |Random jitter per character.                                                                        |<code>{charShakeRandom:1.0}</code>                                             |

  
\*Deprecated tags still work, but with log warnings.

### Fade Timing Example

```java
ImmersiveMessage msg = ImmersiveMessage.builder(60f, "Boss Approaches!")
        .fadeInTicks(10)
        .fadeOutTicks(20);
EmbersTextAPI.sendMessage(player, msg);
```

Command variant:

```
/immersivemessages sendcustom @p {text:"Boss Approaches!",duration:60,fadeIn:10,fadeOut:20}
```

### Multiple Concurrent Messages

```java
UUID id = EmbersMessages.open(player,
    ImmersiveMessage.builder(100f, "⚔ Boss Appears")
        .anchor(TextAnchor.TOP_CENTER)
        .build());
EmbersMessages.update(player, id,
    ImmersiveMessage.builder(100f, "🔥 Phase 2: Enrage")
        .shake(ShakeType.RANDOM, 1.2f)
        .build());
EmbersMessages.close(player, id);
// Or clear everything:
EmbersMessages.closeAll(player);
```

## Text tag quickstart

Try the in-game demo command to see animated gradients:

```
/emberstextapi send @p 120 <grad from=#ff6a00 to=#ffd500><bold>EMBERCRAFT</bold></grad>
```

The complete syntax reference, built-in parameters, and migration notes live in
[docs/tags.md](docs/tags.md).
