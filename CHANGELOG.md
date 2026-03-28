### Fixed
- Preset system now loads correctly. Presets were not being loaded from resource files due to `PresetLoader.loadAll()` never being called during resource reload. Preset tags like `<legendary>`, `<epic>`, `<spooky>`, etc. now work as expected.

### Added
- 5 new built-in presets:
  - `<arcane>` - Neon + Turbulence, purple, Cinzel font
  - `<chaotic>` - Rainbow + Glitch + Bounce, bold
  - `<divine>` - Neon + Wave + Pulse, bold gold, Almendra font
  - `<frozen>` - Gradient + Pendulum + Neon, italic, Cardo font
  - `<infernal>` - Gradient + Shake + Neon, bold, Norse font