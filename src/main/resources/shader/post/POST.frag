#version 330 core

out vec4 FragColor;

uniform vec2 iResolution;

uniform sampler2D gBufferALBEDO;
uniform sampler2D gBufferNORMAL;
uniform sampler2D gBufferLinearDepth;

void main(){
    vec2 uv = gl_FragCoord.xy/iResolution.xy;

    vec3 albedo = texture(gBufferALBEDO, uv).rgb;
    vec3 normal = texture(gBufferNORMAL, uv).rgb;
    float linearDepth = texture(gBufferLinearDepth, uv).r;

    if(normal.x > .9) albedo.xyz *= 0.5;
    if(normal.y > .9) albedo.xyz *= 1.0;
    if(normal.z > .9) albedo.xyz *= 0.75;

    FragColor = vec4(albedo, 1.);
}