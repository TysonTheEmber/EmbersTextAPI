# Changelog

- Added per-message fadeIn/fadeOut tick attributes that wrap around duration.
- New builder methods: fadeInTicks(int), fadeOutTicks(int)
- Commands now accept NBT keys `fadeIn`, `fadeOut` (ints).

## Added
- Introduced Ember Markup parser, AST, and vanilla component emitter for tag-driven text.
- Added attribute registry with default handlers and placeholder overlay queue.
- Exposed EmberMarkup public API plus Rich DSL builder and documentation.
- Added parser/component unit tests.
