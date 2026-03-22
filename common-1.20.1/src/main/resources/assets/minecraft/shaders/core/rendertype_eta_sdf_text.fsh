#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// MSDF median: selects the middle value of three, which reconstructs
// the correct distance at corners where individual channels diverge.
float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

// Compute the SDF pixel range in screen pixels using the msdfgen reference
// formula. Uses texture coordinate derivatives (smooth, predictable) instead
// of distance value derivatives (noisy at MSDF channel transitions).
const float pxRange = 8.0;

float screenPxRange() {
    vec2 unitRange = vec2(pxRange) / vec2(textureSize(Sampler0, 0));
    vec2 screenTexSize = vec2(1.0) / fwidth(texCoord0);
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}

void main() {
    vec3 msd = texture(Sampler0, texCoord0).rgb;
    float sd = median(msd.r, msd.g, msd.b);

    // Screen-space anti-aliasing: convert SDF distance to screen pixels,
    // then apply a 1-pixel linear ramp centered on the glyph edge.
    float screenPxDist = screenPxRange() * (sd - 0.5);
    float alpha = clamp(screenPxDist + 0.5, 0.0, 1.0);

    // Sharpen the AA edge by squaring alpha (same curve as the MC 1.21.1 shader).
    // Dramatically reduces low-alpha fringe contribution while preserving the
    // smooth transition at the glyph edge (0.1 → 0.01, 0.5 → 0.25, 0.9 → 0.81).
    alpha *= alpha;

    if (alpha < 0.01) {
        discard;
    }

    float finalAlpha = vertexColor.a * alpha;
    vec4 color = vec4(vertexColor.rgb, finalAlpha) * ColorModulator;

    if (vertexDistance > FogStart) {
        float fogValue = vertexDistance < FogEnd ? smoothstep(FogStart, FogEnd, vertexDistance) : 1.0;
        color = vec4(mix(color.rgb, FogColor.rgb, fogValue * FogColor.a), color.a);
    }

    fragColor = color;
}
