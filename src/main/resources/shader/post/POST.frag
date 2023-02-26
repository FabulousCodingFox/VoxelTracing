#version 330 core

out vec4 FragColor;

uniform int debugDisplayMode;

uniform vec2 iResolution;

uniform sampler2D gBufferALBEDO;
uniform sampler2D gBufferNORMAL;
uniform sampler2D gBufferLinearDepth;
uniform sampler2D gBufferPosition;
uniform sampler2D bufferSSAO;

void main(){
    vec2 uv = gl_FragCoord.xy/iResolution.xy;

    if(debugDisplayMode != 0){
        if(debugDisplayMode == 1){
            FragColor = texture(gBufferALBEDO, uv);
            return;
        }
        if(debugDisplayMode == 2){
            FragColor = texture(gBufferNORMAL, uv);
            return;
        }
        if(debugDisplayMode == 3){
            FragColor = vec4(texture(gBufferLinearDepth, uv).r, 0., 0., 1.);
            return;
        }
        if(debugDisplayMode == 4){
            FragColor = vec4(vec3(texture(bufferSSAO, uv).r), 1.);
            return;
        }
    }

    vec3 albedo = texture(gBufferALBEDO, uv).rgb;
    vec3 normal = texture(gBufferNORMAL, uv).rgb;
    float linearDepth = texture(gBufferLinearDepth, uv).r;
    float ao = texture(bufferSSAO, uv).r;

    if(normal.x > .9) albedo.xyz *= 0.5;
    if(normal.y > .9) albedo.xyz *= 1.0;
    if(normal.z > .9) albedo.xyz *= 0.75;

    albedo *= ao;

    FragColor = vec4(albedo, 1.);
}