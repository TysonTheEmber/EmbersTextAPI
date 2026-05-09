# EmbersTextAPI

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%201.21.1%20%7C%2026.1-brightgreen)](https://minecraft.net)
[![Forge](https://img.shields.io/badge/Forge-1.20.1-orange)](https://files.minecraftforge.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-1.21.1%20%7C%2026.1-blue)](https://neoforged.net/)
[![Fabric](https://img.shields.io/badge/Fabric-1.20.1%20%7C%201.21.1%20%7C%2026.1-green)](https://fabricmc.net/)

Advanced text rendering API for Minecraft mods with visual effects, animations, and markup parsing.

For full documentation, guides, and examples, visit **[tysontheember.dev](https://tysontheember.dev)**.


## Features

- **Visual Effects**: Rainbow, glitch, wave, shake, pulse, and more
- **Animations**: Typewriter text, fade in/out, progressive reveal
- **Markup Parser**: XML-style tags for easy text styling
- **MSDF Font Rendering**: Crisp TrueType/OpenType fonts at any scale using Multi-Channel Signed Distance Fields with sharp corner reproduction
- **Inline Rendering**: Items and entities within text
- **Server-Side Control**: Send styled messages from server to clients
- **Highly Customizable**: Per-character effects, gradients, backgrounds

## Supported Versions

| Minecraft Version | Loader   | Status                                |
|-------------------|----------|---------------------------------------|
| 1.20.1            | Forge    | Fully Supported                       |
| 1.20.1            | Fabric   | Fully Supported                       |
| 1.21.1            | NeoForge | Fully Supported                       |
| 1.21.1            | Fabric   | Fully Supported                       |
| 26.1.x            | NeoForge | Alpha (NeoForge 26.1 itself is beta)  |
| 26.1.x            | Fabric   | Alpha (NeoForge 26.1 itself is beta)  |

## Patchouli Compatibility

EmbersTextAPI is compatible with [Patchouli](https://www.curseforge.com/minecraft/mc-mods/patchouli) including books that use `"i18n": true`. Patchouli's tokenizer runs without interference: user-defined book macros, `$(...)` codes, and translations behave as authored.

ETA markup that survives Patchouli's pipeline still renders effects on the resulting components. To avoid clashing with user-defined Patchouli macros, prefer namespaced ETA tags inside book entries — for example `<eta-rainbow>...</eta-rainbow>` rather than `<rainbow>...</rainbow>`. If a Patchouli book defines a macro for an unprefixed name (e.g. `"<b>": "$(l)"`), the macro takes precedence; the ETA tag will not run on that text.

## Installation

1. Download the appropriate jar for your Minecraft version and loader
2. Place the jar in your `mods` folder
3. Launch Minecraft with the corresponding loader installed

## Building from Source

Requires Java 17 (1.20.1) and Java 21 (1.21.1) toolchains; everything else (Gradle, Forge, Loom, MDG) is fetched by the wrappers.

The root Gradle build runs Fabric 1.20.1/1.21.1/26.1, NeoForge 1.21.1/26.1 and the `common-*` shared modules. Forge 1.20.1 is a separate Gradle 8.8 build inside `forge-1.20.1/` (ForgeGradle 6 doesn't support Gradle 9), exposed through `forge1201*` lifecycle tasks on the root.

```bash
# Build everything
./gradlew assemble forge1201Build

# Per-loader run-in-dev
./gradlew forge1201RunClient        # Forge 1.20.1 (delegates to forge-1.20.1/gradlew)
./gradlew :fabric-1.20.1:runClient
./gradlew :fabric-1.21.1:runClient
./gradlew :neoforge-1.21.1:runClient
./gradlew :neoforge-26.1:runClient
```

If you want to work on `forge-1.20.1` directly, `cd forge-1.20.1 && ./gradlew runClient` works too — same effect, slightly less typing.

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Test your changes on MC 1.20.1 (Forge/Fabric) and 1.21.1 (NeoForge/Fabric)
4. Submit a pull request

## License

This project is licensed under my own License - see the [LICENSE](LICENSE.md) file for details.

## Links

- **Website**: [tysontheember.dev](https://tysontheember.dev)
- **GitHub**: [github.com/TysonTheEmber/EmbersTextAPI](https://github.com/TysonTheEmber/EmbersTextAPI)
- **Issues**: [GitHub Issues](https://github.com/TysonTheEmber/EmbersTextAPI/issues)
