package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Bounce effect that creates vertical bouncing motion with physics-like easing.
 * <p>
 * Simulates characters bouncing vertically using Robert Penner's bounce-out easing function.
 * The animation has three phases: rise, bounce (with multiple smaller bounces), and rest.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code a} (amplitude, default: 1.0) - Bounce height (multiplied by 4)</li>
 *   <li>{@code f} (frequency, default: 1.0) - Bounce speed</li>
 *   <li>{@code w} (wave, default: 1.0) - Phase offset between characters</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <bounce>BOING!</bounce>
 * <bounce a=2.0>High Bounce</bounce>
 * <bounce f=2.0>Fast Bounce</bounce>
 * <bounce w=0.2>Wave Bounce</bounce>
 * }</pre>
 *
 * <h3>Animation Phases:</h3>
 * <ul>
 *   <li><b>Rise (0.0-0.2):</b> Sine ease-in to peak height</li>
 *   <li><b>Bounce (0.2-0.8):</b> Multiple diminishing bounces using bounce-out easing</li>
 *   <li><b>Rest (0.8-1.0):</b> Character at rest (no offset)</li>
 * </ul>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Uses MIT-licensed easing equations from Tween.js/Robert Penner</li>
 *   <li>Negative Y offset (moves upward)</li>
 *   <li>Creates realistic bounce physics with multiple smaller bounces</li>
 * </ul>
 */
public class BounceEffect extends BaseEffect {

    private final float amp;
    private final float speed;
    private final float phase;

    /**
     * Creates a new bounce effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public BounceEffect(@NotNull Params params) {
        super(params);
        this.amp = ValidationHelper.clamp("bounce", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 50f);
        this.speed = ValidationHelper.clamp("bounce", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.phase = ValidationHelper.clamp("bounce", "w",
                params.getDouble("w").map(Number::floatValue).orElse(1.0f), 0f, 10f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Calculate normalized time (0.0-1.0) with speed and phase offset
        // Modulo creates repeating animation
        float t = (Util.getMillis() * 0.001f * speed - settings.index * phase * 0.2f) % 1;

        float offset = 0f;

        if (t < 0.2f) {
            // Rise phase: Sine ease-in to peak
            offset = Mth.sin(t / 0.2f * Mth.HALF_PI);

        } else if (t < 0.8f) {
            // Bounce phase: Use Robert Penner's bounce-out easing
            // Normalize time to 0.0-1.0 for the bounce phase
            t = (t - 0.2f) / 0.6f;

            /*
             * The MIT License
             *
             * Copyright (c) 2010-2012 Tween.js authors.
             *
             * Easing equations Copyright (c) 2001 Robert Penner
             * http://robertpenner.com/easing/
             *
             * Permission is hereby granted, free of charge, to any person obtaining a copy
             * of this software and associated documentation files (the "Software"), to deal
             * in the Software without restriction, including without limitation the rights
             * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
             * copies of the Software, and to permit persons to whom the Software is
             * furnished to do so, subject to the following conditions:
             *
             * The above copyright notice and this permission notice shall be included in
             * all copies or substantial portions of the Software.
             *
             * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
             * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
             * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
             * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
             * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
             * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
             * THE SOFTWARE.
             */

            // Bounce-out easing with multiple smaller bounces
            if (t < 1 / 2.75f) {
                offset = 7.5625f * t * t;
            } else if (t < 2 / 2.75f) {
                t -= 1.5f / 2.75f;
                offset = 7.5625f * t * t + 0.75f;
            } else if (t < 2.5f / 2.75f) {
                t -= 2.25f / 2.75f;
                offset = 7.5625f * t * t + 0.9375f;
            } else {
                t -= 2.625f / 2.75f;
                offset = 7.5625f * t * t + 0.984375f;
            }

            // Invert to get bounce-down effect (1 = top, 0 = bottom)
            offset = 1 - offset;
        }
        // else: Rest phase (t >= 0.8), offset remains 0

        // Apply offset (negative = upward)
        // Multiplied by amp * 4 for visible effect
        settings.y -= offset * amp * 4f;
    }

    @NotNull
    @Override
    public String getName() {
        return "bounce";
    }
}
