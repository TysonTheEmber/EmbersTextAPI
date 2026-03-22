## 2.9.0

### Added
- **MSDF font rendering** — Complete rewrite of the SDF font rendering pipeline from single-channel EDT (Euclidean Distance Transform) to Multi-Channel Signed Distance Fields (MSDF). Eliminates stray pixel artifacts and rounded corners that plagued the old approach. Key improvements:
  - **Analytical Bézier distance computation** — Distance to glyph edges is computed directly from vector outlines using Newton-Raphson refinement on quadratic and cubic Bézier curves, replacing the old bitmap→EDT approximation pipeline.
  - **Multi-channel encoding** — Distance information is encoded in three color channels (RGB) with edge coloring, enabling the shader to reconstruct sharp corners that monochrome SDF fundamentally cannot represent.
  - **Cross-product sign determination** — Inside/outside is determined by the cross product of the curve tangent at the closest point, eliminating winding-number ray casting errors.
  - **Pseudo-distance** — Uses perpendicular distance to the tangent line at the closest point instead of true Euclidean distance, preventing "bleeding" around segment endpoints.
  - **Error correction** — Post-generation pass detects and corrects false-color artifacts at edge intersection points.
  - **Degenerate segment filtering** — Tiny segments produced by bold fonts at stroke junctions are filtered out during outline extraction.
- **New bundled MSDF fonts** — Almendra, Almendra Bold, Cardo, Cardo Bold, Cinzel, Cinzel Bold, Norse, Norse Bold (in addition to existing Metamorphous).
- **Font alias system** — `FontAliasRegistry` maps short names to full ResourceLocations, so markup can use `<font name=cinzel>` instead of `<font id=emberstextapi:cinzel>`. All bundled fonts have aliases registered at mod init.
- **New MSDF configuration options** in font JSON:
  - `px_range` (default 8.0) — Pixel range controlling anti-aliasing width. Replaces the deprecated `spread` parameter.
  - `angle_threshold` (default 3.0 radians) — Corner detection threshold for edge coloring. Lower values detect more corners for sharper reproduction.
- **Configurable anvil rename length** — New `anvilNameMaxLength` config option (default 50) allows servers to increase the character limit when renaming items in an anvil. Three mixins (`AnvilMenuMixin`, `AnvilRenameMixin`, `AnvilScreenMixin`) patch the vanilla hardcoded limit.
- **Server-side message limits** — `ServerMessageLimiter` enforces configurable limits on messages sent to clients: `maxServerMessageDuration`, `maxServerActiveMessages`, `maxQueueSize`, and `allowedEffects`.
- **Reduce motion support** — New `reduceMotion` config option disables motion-based effects (wave, shake, bounce, etc.) for accessibility. `isEffectDisabled()` now checks both the explicit disable list and the reduce-motion flag.
- **SDF debug texture dump** — Launch with `-Deta.sdf.debug=true` to dump each MSDF glyph texture as an RGB PNG to `debug-sdf/` in the game directory.
- **Emojiful compatibility layer** — ETA effects now work alongside Emojiful's emoji rendering. Regular characters get full effects while emoji sprites render normally through Emojiful's pipeline.
- **`EffectApplicator` utility class** — Shared effect-application logic extracted from `StringRenderOutputMixin`.

### Fixed
- **SDF text fringe on MC 1.20.1** — Fixed a visible anti-aliasing halo around MSDF-rendered text on all 1.20.1 loaders. MC 1.20.1's blit-to-screen reads framebuffer alpha, so semi-transparent SDF edge fragments caused a fringe artifact. Fixed by defining alpha-preserving blend state (`srcalpha=0, dstalpha=1`) in the shader JSON, which is applied inside `ShaderInstance.apply()` immediately before the draw call.

### Changed
- **Fragment shaders rewritten** for MSDF — `rendertype_eta_sdf_text.fsh` and `rendertype_eta_sdf_text_see_through.fsh` now sample all three RGB channels and take the median for distance reconstruction, with `fwidth`-based screen-space anti-aliasing and alpha squaring for edge sharpening.
- **Font JSON files updated** — All bundled font definitions now use `px_range` and `angle_threshold` instead of the deprecated `spread` field. Old `spread` values are still accepted for backward compatibility (`px_range = spread × 2`).
- Emojiful is no longer declared as incompatible. The previous hard block has been replaced with automatic compatibility mode detection.

### Removed
- **`SDFGenerator.java`** — Old single-channel SDF generator using bitmap→EDT pipeline. Replaced by `MSDFGenerator.java`.
- **Bitmap rendering path** in `FreeTypeManager` — `renderGlyphSDF()`, `computeSDFFromBitmap()`, and all EDT code removed. Only outline extraction via `FT_Outline_Decompose` remains.
- **`FT_Set_Pixel_Sizes` and `FT_Render_Glyph`** from `NativeFreeType` (1.20.1) — No longer needed since bitmap rendering was eliminated.

### Internal
- New class `MSDFGenerator` — Analytical distance computation engine with 3-channel MSDF texture generation, error correction, and pseudo-distance support.
- New class `EdgeColoring` — Chlumsky's simple edge coloring heuristic with corner detection and CYAN/MAGENTA/YELLOW color cycling.
- `SDFConfig` record extended with `pxRange` and `angleThreshold` fields, backward-compatible constructor mapping `spread → pxRange`.
- `SDFGlyphInfo.bake()` rewired to: extract outline → edge color → generate MSDF → upload 3-channel data.
- `SDFSheetGlyphInfo.upload()` now writes independent R, G, B channels to the RGBA atlas instead of duplicating a monochrome value.
- MC 1.20.1 SDF shader JSONs now include a `blend` section with alpha-preserving factors; `SDFRenderTypes.java` uses standard `TRANSLUCENT_TRANSPARENCY` on both versions.
- New SDF pipeline mixins: `FontManagerMixin`, `FontSetMixin` (1.21.1), `FontTextureMixin`, `GlyphRenderTypesMixin`, `BakedGlyphAccessor`.
- 40 unit tests across 3 test classes: `MSDFGeneratorTest` (20), `EdgeColoringTest` (13), `MSDFGenerationTest` (7).
- Added `@Pseudo` mixin `EmojiCharacterRendererMixin` for Emojiful compatibility with `require = 0` for graceful absence.
- Refactored `StringRenderOutputMixin` to delegate effect building, application, and character rendering to `EffectApplicator`.
