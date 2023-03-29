#version 330 core

out vec4 FragColor;

uniform int debugDisplayMode;

uniform vec2 iResolution;

uniform sampler2D gBufferALBEDO;
uniform sampler2D gBufferNORMAL;
uniform sampler2D gBufferMATERIAL;
uniform sampler2D gBufferPosition;
uniform sampler2D gBufferLIGHTING;
uniform sampler2D gBufferDEPTH;

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
            FragColor = texture(gBufferMATERIAL, uv);
            return;
        }
        if(debugDisplayMode == 4){
            FragColor = texture(gBufferLIGHTING, uv);
            return;
        }
        if(debugDisplayMode == 5){
            //float depth = float(texture(gBufferPosition, uv).r) / 4294967296.0; // 2^32
            float depth = texture(gBufferDEPTH, uv).r;
            FragColor = vec4(depth, depth, depth, 1.);
            //FragColor = vec4(vec3(texture(gBufferPosition, uv).z), 1.);
            return;
        }
    }

    vec3 albedo = texture(gBufferALBEDO, uv).rgb;
    vec3 normal = texture(gBufferNORMAL, uv).rgb;
    vec4 material = texture(gBufferMATERIAL, uv).rgba;
    vec3 light = texture(gBufferLIGHTING, uv).rgb;

    if(normal.x > .9) albedo.xyz *= 0.5;
    if(normal.y > .9) albedo.xyz *= 1.0;
    if(normal.z > .9) albedo.xyz *= 0.75;

    albedo *= light;

    FragColor = vec4(albedo, 1.);
}