#version 460 core

layout (location = 0) out vec3 lightingFramebufferOUTPUT;

uniform sampler2D gBufferALBEDO;
uniform sampler2D gBufferNORMAL;
uniform sampler2D gBufferPOSITION;

uniform vec2 iResolution;

struct lightSourceInfo{
    vec3 position;
    float strength;
    vec3 color;
};

const int MAX_LIGHTS = 32;
uniform lightSourceInfo lights[MAX_LIGHTS];

void main(){
    vec2 uv = gl_FragCoord.xy/iResolution.xy;

    vec3 gAlbedo = texture(gBufferALBEDO, uv).rgb;
    vec3 gNormal = texture(gBufferNORMAL, uv).rgb;
    vec3 gPosition = texture(gBufferPOSITION, uv).rgb;

    vec3 lighting = vec3(0);

    for(int i = 0; i < MAX_LIGHTS; i++){
        float strength = lights[i].strength;
        if(strength == 0) continue;
        vec3 lightPosition = lights[i].position;
        vec3 lightColor = lights[i].color;
        float dist = distance(lightPosition, gPosition);
        if(dist > strength) continue;
        float value = 1.0 - (dist/strength);
        vec3 col = lightColor * value;
        lighting += col;
    }

    if (gNormal.x > .9) lighting *= 0.5;
    if (gNormal.y > .9) lighting *= 1.0;
    if (gNormal.z > .9) lighting *= 0.75;

    lightingFramebufferOUTPUT = lighting;
}