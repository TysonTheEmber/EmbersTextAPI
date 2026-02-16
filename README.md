# EmbersTextAPI

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%201.21.1-brightgreen)](https://minecraft.net)
[![Forge](https://img.shields.io/badge/Forge-1.20.1-orange)](https://files.minecraftforge.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-1.21.1-blue)](https://neoforged.net/)
[![Fabric](https://img.shields.io/badge/Fabric-1.20.1%20%7C%201.21.1-green)](https://fabricmc.net/)

Advanced text rendering API for Minecraft mods with visual effects, animations, and markup parsing.

For full documentation, guides, and examples, visit **[tysontheember.dev](https://tysontheember.dev)**.

## Features

- **Visual Effects**: Rainbow, glitch, wave, shake, pulse, and more
- **Animations**: Typewriter text, fade in/out, progressive reveal
- **Markup Parser**: XML-style tags for easy text styling
- **Inline Rendering**: Items and entities within text
- **Server-Side Control**: Send styled messages from server to clients
- **Highly Customizable**: Per-character effects, gradients, backgrounds

## Supported Versions

| Minecraft Version | Loader   | Status              |
|-------------------|----------|---------------------|
| 1.20.1            | Forge    | Fully Supported     |
| 1.20.1            | Fabric   | Fully Supported     |
| 1.21.1            | NeoForge | Fully Supported     |
| 1.21.1            | Fabric   | Fully Supported     |

## Installation

1. Download the appropriate jar for your Minecraft version and loader
2. Place the jar in your `mods` folder
3. Launch Minecraft with the corresponding loader installed

## Building from Source

Requires Java 21 (for MC 1.21.1) or Java 17 (for MC 1.20.1), and Gradle 8.8.

```bash
# Build all modules
./gradlew :forge-1.20.1:build :neoforge-1.21.1:build :fabric-1.20.1:build :fabric-1.21.1:build

# Run in development
./gradlew :forge-1.20.1:runClient
./gradlew :neoforge-1.21.1:runClient
./gradlew :fabric-1.20.1:runClient
./gradlew :fabric-1.21.1:runClient
```

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Test your changes on MC 1.20.1 (Forge/Fabric) and 1.21.1 (NeoForge/Fabric)
4. Submit a pull request

## License

This project is licensed under the GPL-3.0 License - see the [LICENSE](LICENSE) file for details.

## Links

- **Website**: [tysontheember.dev](https://tysontheember.dev)
- **GitHub**: [github.com/TysonTheEmber/EmbersTextAPI](https://github.com/TysonTheEmber/EmbersTextAPI)
- **Issues**: [GitHub Issues](https://github.com/TysonTheEmber/EmbersTextAPI/issues)
