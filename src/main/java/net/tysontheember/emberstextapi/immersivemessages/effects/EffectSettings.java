package net.tysontheember.emberstextapi.immersivemessages.effects;

import net.tysontheember.emberstextapi.typewriter.TypewriterTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable state container for character rendering with effects.
 * <p>
 * This class holds all the parameters that effects can modify when rendering a single character.
 * Effects apply their transformations by modifying this object in-place.
 * </p>
 * <p>
 * The EffectSettings object is created per-character during rendering and passed through
 * the effect stack. Each effect can read current values and modify them as needed.
 * </p>
 *
 * <h3>Common Effect Patterns:</h3>
 * <ul>
 *   <li><b>Position Effects:</b> Modify {@code x} and {@code y} (wave, turbulence, shake)</li>
 *   <li><b>Rotation Effects:</b> Modify {@code rot} (swing, pendulum)</li>
 *   <li><b>Color Effects:</b> Modify {@code r}, {@code g}, {@code b} (rainbow, pulse)</li>
 *   <li><b>Transparency Effects:</b> Modify {@code a} (fade, glitch blink)</li>
 *   <li><b>Multi-Layer Effects:</b> Add to {@code siblings} list (glitch slices, neon glow)</li>
 * </ul>
 *
 * @see Effect
 * @see EffectContext
 */
public class EffectSettings {

    // ===== Position & Rotation =====

    /**
     * Horizontal position offset in pixels.
     * Positive values move right, negative values move left.
     */
    public float x;

    /**
     * Vertical position offset in pixels.
     * Positive values move down, negative values move up.
     */
    public float y;

    /**
     * Rotation angle in radians.
     * Applied around the character's center point.
     */
    public float rot;

    /**
     * Scale multiplier for the character.
     * 1.0 is normal size, values > 1.0 enlarge, values < 1.0 shrink.
     */
    public float scale;

    // ===== Color & Alpha =====

    /**
     * Red color channel (0.0 to 1.0).
     * Values outside this range will be clamped during rendering.
     */
    public float r;

    /**
     * Green color channel (0.0 to 1.0).
     * Values outside this range will be clamped during rendering.
     */
    public float g;

    /**
     * Blue color channel (0.0 to 1.0).
     * Values outside this range will be clamped during rendering.
     */
    public float b;

    /**
     * Alpha transparency (0.0 = fully transparent, 1.0 = fully opaque).
     * Values outside this range will be clamped during rendering.
     */
    public float a;

    // ===== Character Context =====

    /**
     * Character index in the string (0-based).
     * Used by effects to create phase offsets between characters (e.g., wave propagation).
     */
    public int index;

    /**
     * Absolute character index across all text (0-based).
     * Used for effects that need global positioning context.
     */
    public int absoluteIndex;

    /**
     * Unicode codepoint of the character being rendered.
     * Can be used by effects for character-specific variations.
     */
    public int codepoint;

    /**
     * Whether this rendering pass is for the shadow layer.
     * Some effects skip or modify behavior for shadows (e.g., rainbow, pulse).
     */
    public boolean isShadow;

    /**
     * Shadow offset distance in pixels.
     * Used by some effects to adjust shadow positioning.
     */
    public float shadowOffset;

    // ===== Typewriter Effect Support =====

    /**
     * Typewriter track for animation state.
     * <p>
     * Set when the Style has a typewriter effect attached.
     * Null if no typewriter effect is active.
     * </p>
     */
    public TypewriterTrack typewriterTrack;

    /**
     * Typewriter index (global character position offset).
     * <p>
     * Combined with {@code index}, this gives the absolute character position
     * in the full text for typewriter animation ordering.
     * </p>
     * <p>
     * A value of -1 indicates uninitialized/not applicable.
     * </p>
     */
    public int typewriterIndex;

    // ===== Multi-Layer Rendering =====

    /**
     * Additional rendering layers created by multi-layer effects.
     * <p>
     * Effects like glitch can create multiple "sibling" characters that render
     * with different parameters (position, color, alpha) to create layered visual effects.
     * </p>
     * <p>
     * Each sibling is a complete copy of the settings at the time it was created,
     * and will be rendered after the main character.
     * </p>
     */
    public List<EffectSettings> siblings;

    /**
     * Vertical masking - top cutoff point (0.0 to 1.0).
     * Used by glitch effect to render only part of the character.
     * 0.0 = no top mask, 1.0 = completely masked from top.
     */
    public float maskTop;

    /**
     * Vertical masking - bottom cutoff point (0.0 to 1.0).
     * Used by glitch effect to render only part of the character.
     * 0.0 = no bottom mask, 1.0 = completely masked from bottom.
     */
    public float maskBottom;

    /**
     * Creates a new EffectSettings with default values.
     */
    public EffectSettings() {
        this.x = 0f;
        this.y = 0f;
        this.rot = 0f;
        this.scale = 1f;
        this.r = 1f;
        this.g = 1f;
        this.b = 1f;
        this.a = 1f;
        this.index = 0;
        this.absoluteIndex = 0;
        this.codepoint = 0;
        this.isShadow = false;
        this.shadowOffset = 1f;
        this.typewriterTrack = null;
        this.typewriterIndex = -1;
        this.siblings = new ArrayList<>();
        this.maskTop = 0f;
        this.maskBottom = 0f;
    }

    /**
     * Creates a new EffectSettings with specified initial values.
     *
     * @param x Horizontal position offset
     * @param y Vertical position offset
     * @param r Red color channel
     * @param g Green color channel
     * @param b Blue color channel
     * @param a Alpha transparency
     * @param index Character index
     * @param codepoint Character codepoint
     * @param isShadow Whether this is the shadow layer
     */
    public EffectSettings(float x, float y, float r, float g, float b, float a,
                          int index, int codepoint, boolean isShadow) {
        this.x = x;
        this.y = y;
        this.rot = 0f;
        this.scale = 1f;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.index = index;
        this.absoluteIndex = index; // Default to same as index
        this.codepoint = codepoint;
        this.isShadow = isShadow;
        this.shadowOffset = 1f;
        this.typewriterTrack = null;
        this.typewriterIndex = -1;
        this.siblings = new ArrayList<>();
        this.maskTop = 0f;
        this.maskBottom = 0f;
    }

    /**
     * Creates a deep copy of this EffectSettings.
     * <p>
     * Note: The siblings list is NOT copied (new empty list is created).
     * This prevents infinite recursion and is the intended behavior for sibling creation.
     * </p>
     *
     * @return A new EffectSettings with the same values
     */
    public EffectSettings copy() {
        EffectSettings copy = new EffectSettings();
        copy.x = this.x;
        copy.y = this.y;
        copy.rot = this.rot;
        copy.scale = this.scale;
        copy.r = this.r;
        copy.g = this.g;
        copy.b = this.b;
        copy.a = this.a;
        copy.index = this.index;
        copy.absoluteIndex = this.absoluteIndex;
        copy.codepoint = this.codepoint;
        copy.isShadow = this.isShadow;
        copy.shadowOffset = this.shadowOffset;
        copy.typewriterTrack = this.typewriterTrack;
        copy.typewriterIndex = this.typewriterIndex;
        copy.maskTop = this.maskTop;
        copy.maskBottom = this.maskBottom;
        // Intentionally create new empty siblings list
        return copy;
    }

    /**
     * Reset this EffectSettings to default values while preserving character context.
     * <p>
     * Resets position, rotation, color, alpha, and clears siblings, but keeps
     * the index, absoluteIndex, codepoint, and isShadow values.
     * </p>
     */
    public void reset() {
        this.x = 0f;
        this.y = 0f;
        this.rot = 0f;
        this.scale = 1f;
        this.r = 1f;
        this.g = 1f;
        this.b = 1f;
        this.a = 1f;
        this.siblings.clear();
        this.maskTop = 0f;
        this.maskBottom = 0f;
    }

    /**
     * Clamp color and alpha values to valid range [0.0, 1.0].
     * Should be called before rendering to ensure valid values.
     */
    public void clampColors() {
        this.r = Math.max(0f, Math.min(1f, this.r));
        this.g = Math.max(0f, Math.min(1f, this.g));
        this.b = Math.max(0f, Math.min(1f, this.b));
        this.a = Math.max(0f, Math.min(1f, this.a));
    }

    /**
     * Get combined color as packed ARGB integer.
     * Automatically clamps values to valid range.
     *
     * @return Packed ARGB color (0xAARRGGBB format)
     */
    public int getPackedColor() {
        int ai = (int) (Math.max(0f, Math.min(1f, a)) * 255);
        int ri = (int) (Math.max(0f, Math.min(1f, r)) * 255);
        int gi = (int) (Math.max(0f, Math.min(1f, g)) * 255);
        int bi = (int) (Math.max(0f, Math.min(1f, b)) * 255);
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }

    @Override
    public String toString() {
        return String.format("EffectSettings[pos=(%.1f,%.1f) rot=%.2f scale=%.2f rgba=(%.2f,%.2f,%.2f,%.2f) idx=%d abs=%d cp=%d shadow=%b siblings=%d]",
                x, y, rot, scale, r, g, b, a, index, absoluteIndex, codepoint, isShadow, siblings.size());
    }
}
