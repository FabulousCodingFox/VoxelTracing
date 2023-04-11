#version 460 core

out vec4 FragColor;

uniform int debugDisplayMode;

uniform vec2 iResolution;

uniform sampler2D gBufferALBEDO;
uniform sampler2D gBufferNORMAL;
uniform sampler2D gBufferPOSITION;
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
            FragColor = texture(gBufferPOSITION, uv);
            return;
        }
        if (debugDisplayMode == 4){
            FragColor = texture(gBufferLIGHTING, uv);
            return;
        }
    }

    FragColor = vec4(texture(gBufferALBEDO, uv).rgb * texture(gBufferLIGHTING, uv).rgb, 1.);
}