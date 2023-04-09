#version 330 core

out vec4 FragColor;

uniform int debugDisplayMode;

uniform vec2 iResolution;

uniform sampler2D gBufferALBEDO;
uniform sampler2D gBufferNORMAL;
uniform sampler2D gBufferLIGHTING;

void main(){
    vec2 uv = gl_FragCoord.xy/iResolution.xy;

    if (debugDisplayMode != 0){
        if (debugDisplayMode == 1){
            FragColor = texture(gBufferALBEDO, uv);
            return;
        }
        if (debugDisplayMode == 2){
            FragColor = texture(gBufferNORMAL, uv);
            return;
        }
        if (debugDisplayMode == 3){
            FragColor = texture(gBufferLIGHTING, uv);
            return;
        }
    }

    vec3 albedo = texture(gBufferALBEDO, uv).rgb;
    vec3 normal = texture(gBufferNORMAL, uv).rgb;
    vec3 light = texture(gBufferLIGHTING, uv).rgb;

    if (normal.x > .9) albedo.xyz *= 0.5;
    if (normal.y > .9) albedo.xyz *= 1.0;
    if (normal.z > .9) albedo.xyz *= 0.75;

    albedo *= light;

    FragColor = vec4(albedo, 1.);
}