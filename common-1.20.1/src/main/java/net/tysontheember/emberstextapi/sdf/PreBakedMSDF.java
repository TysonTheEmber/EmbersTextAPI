package net.tysontheember.emberstextapi.sdf;

/**
 * Pre-computed MSDF texture data for a single glyph, generated asynchronously
 * during font loading to avoid blocking the render thread on first use.
 * <p>
 * Contains the raw 3-channel MSDF byte data and all metrics needed to construct
 * an {@link SDFSheetGlyphInfo} without re-running the expensive MSDF generation pipeline.
 *
 * @param msdfData   3-channel MSDF data (RGB, 3 bytes per pixel, row-major)
 * @param texW       Texture width in pixels
 * @param texH       Texture height in pixels
 * @param bearingX   Horizontal bearing in MC units (1.20.1 convention)
 * @param bearingY   Vertical bearing in MC units (1.20.1 convention)
 * @param oversample Texture pixels per MC text unit
 */
public record PreBakedMSDF(
        byte[] msdfData,
        int texW,
        int texH,
        float bearingX,
        float bearingY,
        float oversample
) {}
