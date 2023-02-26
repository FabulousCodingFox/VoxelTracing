#version 330 core

layout (location = 0) out vec4 gBufferALBEDO;
layout (location = 1) out vec3 gBufferNORMAL;
layout (location = 2) out float gBufferLinearDepth;
layout (location = 3) out vec4 gBufferPosition;

in vec3 uvData;
in vec3 fragPos;

uniform sampler3D dataContainer;

uniform int sizeX;
uniform int sizeY;
uniform int sizeZ;
uniform vec3 modelPosition;

uniform float voxelSize;

uniform vec3 position;
uniform vec3 direction;

uniform float zNear;
//uniform float zFar;
const int zFar = 2147483647;

const int accuracy = 3;
const int raycastExpandHitbox = 3;

struct DDAResult{
    vec4 color;
    bvec3 normal;
    float distance;
    vec3 position;
};

bool isNear(float a, float b){
    return abs(a-b) < .01;
}

vec4 getVoxelAtXYZ(int x, int y, int z){
    if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) return vec4(0., 1., 0., 0.);
    return texture(
        dataContainer,
        vec3(
            float(x) / float(sizeX),
            float(y) / float(sizeY),
            float(z) / float(sizeZ)
        )
    );
}

DDAResult raycastDDA(vec3 rayPos, vec3 rayDir){
    ivec3 mapPos = ivec3(floor(rayPos + 0.));
    vec3 deltaDist = abs(vec3(length(rayDir)) / rayDir);
    ivec3 rayStep = ivec3(sign(rayDir));
    vec3 sideDist = (sign(rayDir) * (vec3(mapPos) - rayPos) + (sign(rayDir) * 0.5) + 0.5) * deltaDist;
    vec4 voxel = vec4(0., 1., 0., 0.);
    bvec3 mask;

    for (int i = 0; i < sizeX + sizeY + sizeZ; i++){
        if(mapPos.x < -raycastExpandHitbox || mapPos.x >= sizeX + raycastExpandHitbox || mapPos.y < -raycastExpandHitbox || mapPos.y >= sizeY + raycastExpandHitbox || mapPos.z < -raycastExpandHitbox || mapPos.z >= sizeZ + raycastExpandHitbox) break;

        voxel = getVoxelAtXYZ(mapPos.x, mapPos.y, mapPos.z);
        if(voxel.a > 0.999) break;

        mask = lessThanEqual(sideDist.xyz, min(sideDist.yzx, sideDist.zxy));
        sideDist += deltaDist * vec3(mask);
        mapPos += rayStep * ivec3(mask);
    }

    vec3 pos = modelPosition + (vec3(mapPos.x * 2, mapPos.y * 2, sizeZ) - vec3(mapPos)) * voxelSize;
    return DDAResult(voxel, mask, length(position - pos), pos);
}


void main(){
    vec3 playerOffset = ((position - modelPosition) / (vec3(sizeX, sizeY, sizeZ) * voxelSize)) * vec3(sizeX, sizeY, sizeZ);
    bool playerOutsideOfBox = playerOffset.x < -accuracy || playerOffset.x > sizeX + accuracy || playerOffset.y < -accuracy || playerOffset.y > sizeY + accuracy || playerOffset.z < -accuracy || playerOffset.z > sizeZ + accuracy;
    if(playerOutsideOfBox != gl_FrontFacing) discard;

    vec2 uv = uvData.xy;
    float normal = uvData.z;
    vec4 col = vec4(1., 0., 0., 1.);

    vec3 dir = normalize(fragPos - position); //vec3(0., 0., 1.);
    dir.z = -dir.z;
    vec3 start;

    if(playerOutsideOfBox){
        if (isNear(normal, 0.)){
            float pixelX = uv.x * float(sizeX);
            float pixelY = uv.y * float(sizeY);
            start = vec3(pixelX, pixelY, 0.);
        }

        if (isNear(normal, 1.)){
            float pixelX = uv.x * float(sizeX);
            float pixelY = uv.y * float(sizeY);
            start = vec3(pixelX, pixelY, sizeZ);
        }

        if (isNear(normal, 2.)){
            float pixelZ = uv.x * float(sizeZ);
            float pixelY = uv.y * float(sizeY);
            start = vec3(sizeX, sizeY - pixelY, pixelZ);
        }

        if (isNear(normal, 3.)){
            float pixelZ = uv.x * float(sizeZ);
            float pixelY = uv.y * float(sizeY);
            start = vec3(0., sizeY - pixelY, sizeZ - pixelZ);
        }

        if (isNear(normal, 4.)){
            float pixelX = uv.x * float(sizeX);
            float pixelZ = uv.y * float(sizeZ);
            start = vec3(pixelX, sizeY, pixelZ);
        }

        if (isNear(normal, 5.)){
            float pixelX = uv.x * float(sizeX);
            float pixelZ = uv.y * float(sizeZ);
            start = vec3(pixelX, 0., pixelZ);
        }
    }else{
        start = playerOffset;
        start.z = sizeZ - start.z;
    }

    DDAResult c = raycastDDA(start, dir);
    if(c.color.a < .5) discard;

    gBufferALBEDO = c.color;
    gBufferNORMAL = vec3(c.normal);

    float hyperbolicDepth = ((1. / c.distance) - (1. / zNear)) / ((1. / float(zFar)) - (1. / zNear));
    gBufferPosition = vec4(c.position, hyperbolicDepth);
    gl_FragDepth = hyperbolicDepth;

    gBufferLinearDepth = (c.distance - zNear) / (float(zFar) - zNear);
}