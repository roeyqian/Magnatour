#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:fog.glsl>
#include <minecraft:globals.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:oit.glsl>

uniform sampler2D Sampler0;
uniform sampler2D GlintSampler;

#ifndef OIT_ALPHA_ONLY
layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
layout(location = 3) in vec4 lightMapColor;
layout(location = 4) in vec4 overlayColor;
layout(location = 6) in vec2 texCoordGlint;
layout(location = 0) out vec4 fragColor;
#endif
layout(location = 2) in vec4 vertexColor;
layout(location = 5) in vec2 texCoord0;

void main() {
    // One surface contains both the item and its emissive glint. A second,
    // coplanar pass would compete with shaderpack depth and temporal jitter.
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    #ifdef OIT_ALPHA_ONLY
    executeAlphaOnlyPhase(gl_FragCoord.z, color.a);
    #else
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    vec3 glint = texture(GlintSampler, texCoordGlint).rgb * GlintAlpha;
    color.rgb += glint * glint;
    #ifdef OIT_ACCUMULATE
    color = sampleColorForAccumulation(color);
    vec4 fogColor = vec4(FogColor.rgb * color.a, FogColor.a);
    #else
    vec4 fogColor = FogColor;
    #endif
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
        FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, fogColor);
    #endif
}
