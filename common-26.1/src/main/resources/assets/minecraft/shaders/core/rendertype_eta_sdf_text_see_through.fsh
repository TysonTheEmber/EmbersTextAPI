#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// MSDF median: selects the middle value of three, which reconstructs
// the correct distance at corners where individual channels diverge.
float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

// Compute the SDF pixel range in screen pixels (see rendertype_eta_sdf_text.fsh)
const float pxRange = 8.0;

float screenPxRange() {
    vec2 unitRange = vec2(pxRange) / vec2(textureSize(Sampler0, 0));
    vec2 screenTexSize = vec2(1.0) / fwidth(texCoord0);
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}

void main() {
    vec3 msd = texture(Sampler0, texCoord0).rgb;
    float sd = median(msd.r, msd.g, msd.b);

    // Screen-space anti-aliasing (see rendertype_eta_sdf_text.fsh)
    float screenPxDist = screenPxRange() * (sd - 0.5);
    float alpha = clamp(screenPxDist + 0.5, 0.0, 1.0);

    // Sharpen AA edge (see rendertype_eta_sdf_text.fsh)
    alpha *= alpha;

    if (alpha < 0.01) {
        discard;
    }

    // See-through variant: no fog
    float finalAlpha = vertexColor.a * alpha;
    vec4 color = vec4(vertexColor.rgb, finalAlpha) * ColorModulator;

    fragColor = color;
}
