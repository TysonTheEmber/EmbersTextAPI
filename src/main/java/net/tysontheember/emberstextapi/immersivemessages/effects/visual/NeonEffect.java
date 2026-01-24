package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Neon effect that creates a glowing appearance.
 * <p>
 * Creates a neon/glow effect by rendering multiple semi-transparent sibling
 * layers with slight offsets around the character. The glow is created by
 * rendering the character multiple times with reduced opacity in a circular
 * pattern.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code p} (passes, default: 10, min: 4) - Number of glow layers (more = smoother glow)</li>
 *   <li>{@code r} (radius, default: 2.0) - Glow radius in pixels</li>
 *   <li>{@code a} (alpha multiplier, default: 0.12) - Opacity of each glow layer</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <neon>Glowing Text</neon>
 * <neon p=20 r=3>Strong Glow</neon>
 * <neon r=1 a=0.2>Tight Bright Glow</neon>
 * <neon p=15 r=4 a=0.08>Soft Wide Glow</neon>
 * }</pre>
 *
 * <h3>Technical Details:</h3>
 * <ul>
 *   <li>Creates multiple sibling rendering layers around the character</li>
 *   <li>Siblings are positioned in a circle around the main character</li>
 *   <li>Each sibling has reduced opacity (alpha multiplier)</li>
 *   <li>More passes = smoother but more expensive rendering</li>
 * </ul>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Performance impact scales with number of passes</li>
 *   <li>Recommended passes: 8-15 for good balance</li>
 *   <li>Combine with color effects for colored glow</li>
 *   <li>May appear differently depending on background color</li>
 * </ul>
 */
public class NeonEffect extends BaseEffect {

    private final int passes;
    private final float radius;
    private final float alphaMul;

    /**
     * Creates a new neon effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public NeonEffect(@NotNull Params params) {
        super(params);
        this.passes = (int) Math.max(4, params.getDouble("p").orElse(10.0));
        this.radius = params.getDouble("r").map(Number::floatValue).orElse(2.0f);
        this.alphaMul = params.getDouble("a").map(Number::floatValue).orElse(0.12f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Create multiple glow layers in a circle around the character
        float angleStep = (float) (Math.PI * 2.0 / passes);

        for (int i = 0; i < passes; i++) {
            // Calculate position on circle
            float angle = angleStep * i;
            float offsetX = (float) Math.cos(angle) * radius;
            float offsetY = (float) Math.sin(angle) * radius;

            // Create sibling layer for glow
            EffectSettings glowLayer = settings.copy();
            glowLayer.x += offsetX;
            glowLayer.y += offsetY;
            glowLayer.a *= alphaMul;

            settings.addSibling(glowLayer);
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "neon";
    }
}
