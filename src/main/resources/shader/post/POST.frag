#version 460 core

out vec4 FragColor;

uniform int debugDisplayMode;

uniform vec2 iResolution;

uniform sampler2D gBufferALBEDO;
uniform sampler2D gBufferNORMAL;
uniform sampler2D gBufferPOSITION;
uniform sampler2D gBufferLIGHTING;
uniform sampler2D gBufferDEPTH;

void main(){
    vec2 uv = gl_FragCoord.xy/iResolution.xy;

    if (debugDisplayMode != 0){
        if (debugDisplayMode == 1){
            FragColor = vec4(texture(gBufferALBEDO, uv).rgb, 1.);
            return;
        }
        if (debugDisplayMode == 2){
            FragColor = vec4(texture(gBufferNORMAL, uv).rgb, 1.);
            return;
        }
        if (debugDisplayMode == 3){
            FragColor = vec4(texture(gBufferPOSITION, uv).rgb, 1.);
            return;
        }
        if (debugDisplayMode == 4){
            FragColor = vec4(texture(gBufferLIGHTING, uv).rgb, 1.);
            return;
        }
        if (debugDisplayMode == 5){
            FragColor = vec4(vec3(texture(gBufferDEPTH, uv).r), 1.);
            return;
        }
    }

    FragColor = vec4(texture(gBufferALBEDO, uv).rgb * texture(gBufferLIGHTING, uv).rgb, 1.);
}