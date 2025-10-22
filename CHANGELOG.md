# Changelog

## 2.0.0

**BREAKING CHANGES - Major architectural refactor**

- **Span-based Architecture**: Messages now support per-span styling instead of global attributes
- **Markup Parser**: Added inline tag support like `<grad from=#ff0000 to=#00ff00>text</grad>`
- **Nested Effects**: Support for nested tags like `<shake><grad>text</grad></shake>`
- **Per-Span Effects**: Each text span can have its own gradient, typewriter, shake, and style properties
- **Backward Compatibility**: Legacy usage patterns still work (single-span messages)
- **Enhanced Commands**: `/emberstextapi sendcustom` now supports markup parsing

## 1.4.0

- Added per-message fadeIn/fadeOut tick attributes that wrap around duration.
- New builder methods: fadeInTicks(int), fadeOutTicks(int)
- Commands now accept NBT keys `fadeIn`, `fadeOut` (ints).
