#version 330 core

out vec4 FragColor;

uniform int debugDisplayMode;

uniform vec2 iResolution;

uniform sampler2D gBufferALBEDO;
uniform sampler2D gBufferNORMAL;
uniform sampler2D gBufferPosition;
uniform sampler2D bufferSSAONoise;

uniform vec3 samples[64];
uniform mat4 projection;

const int kernelSize = 64;
const float radius   = 0.5;
const float bias     = 0.05;

void main(){
    vec2 noiseScale = vec2(iResolution.x/4.0, iResolution.y/4.0);
    vec2 uv = gl_FragCoord.xy/iResolution.xy;

    vec3 fragPos   = texture(gBufferPosition, uv).xyz;
    vec3 normal    = texture(gBufferNORMAL, uv).rgb;
    vec3 randomVec = texture(bufferSSAONoise, uv * noiseScale).xyz;

    vec3 tangent   = normalize(randomVec - normal * dot(randomVec, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 TBN       = mat3(tangent, bitangent, normal);

    float occlusion = 0.0;
    for(int i = 0; i < kernelSize; ++i)
    {
        // get sample position
        vec3 samplePos = TBN * samples[i]; // from tangent to view-space
        samplePos = fragPos + samplePos * radius;

        vec4 offset = vec4(samplePos, 1.0);
        offset      = projection * offset;    // from view to clip-space
        offset.xyz /= offset.w;               // perspective divide
        offset.xyz  = offset.xyz * 0.5 + 0.5; // transform to range 0.0 - 1.0

        float sampleDepth = texture(gBufferPosition, offset.xy).z;

        float rangeCheck = smoothstep(0.0, 1.0, radius / abs(fragPos.z - sampleDepth));
        occlusion += (sampleDepth >= samplePos.z + bias ? 1.0 : 0.0) * rangeCheck;
    }

    occlusion = 1.0 - (occlusion / kernelSize);
    FragColor = vec4(occlusion, occlusion, occlusion, 1.0);
}