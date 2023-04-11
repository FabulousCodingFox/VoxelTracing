#version 460 core

layout (location = 0) out vec4 gBufferALBEDO;
layout (location = 1) out vec3 gBufferNORMAL;
layout (location = 2) out vec3 gBufferPOSITION;

in vec3 uvData;
in vec3 fragPos;

layout (depth_greater) out float gl_FragDepth;

uniform sampler3D dataContainer;

uniform vec2 iResolution;

uniform int sizeX;
uniform int sizeY;
uniform int sizeZ;
uniform vec3 modelPosition;

uniform float voxelSize;

uniform vec3 position;
uniform vec3 direction;

const float zNear = 0.1f;
const int zFar = 2000;
const float PI = 3.1415926535897932384626433832795;

const int accuracy = 1;
const int raycastExpandHitbox = 1;

struct DDAResult{
    vec4 color;
    bvec3 normal;
    float distance;
    vec3 position;
    float ao;
};

bool isNear(float a, float b){
    return abs(a-b) < .01;
}

float sum(vec3 v) {
    return dot(v, vec3(1.0));
}

vec4 getVoxelAtXYZ(int x, int y, int z){
    if (x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) return vec4(0., 1., 0., 0.);
    return texelFetch(dataContainer, ivec3(x, y, z), 0);
}

bool getIfVoxelAtXYZ(vec3 p){
    return getVoxelAtXYZ(int(p.x), int(p.y), int(p.z)).a > 0.9;
}

float calcVertexAo(vec2 side, float corner) {
    //if (side.x == 1.0 && side.y == 1.0) return 1.0;
    return (side.x + side.y + max(corner, side.x * side.y)) / 4.0;
}

vec4 calcVoxelAo(vec3 pos, vec3 d1, vec3 d2) {
    vec4 side = vec4(getIfVoxelAtXYZ(pos + d1), getIfVoxelAtXYZ(pos + d2), getIfVoxelAtXYZ(pos - d1), getIfVoxelAtXYZ(pos - d2));
    vec4 corner = vec4(getIfVoxelAtXYZ(pos + d1 + d2), getIfVoxelAtXYZ(pos - d1 + d2), getIfVoxelAtXYZ(pos - d1 - d2), getIfVoxelAtXYZ(pos + d1 - d2));
    vec4 ao;
    ao.x = calcVertexAo(side.xy, corner.x);
    ao.y = calcVertexAo(side.yz, corner.y);
    ao.z = calcVertexAo(side.zw, corner.z);
    ao.w = calcVertexAo(side.wx, corner.w);
    return 1.0 - ao;
}

DDAResult raycastDDA(vec3 rayPos, vec3 rayDir, bvec3 mask){
    // DDA
    ivec3 mapPos = ivec3(floor(rayPos + 0.));
    vec3 deltaDist = abs(vec3(length(rayDir)) / rayDir);
    ivec3 rayStep = ivec3(sign(rayDir));
    vec3 sideDist = (sign(rayDir) * (vec3(mapPos) - rayPos) + (sign(rayDir) * .5) + .5) * deltaDist;
    vec4 voxel = vec4(0.);
    float ao = 1.;

    for (int i = 0; i < sizeX + sizeY + sizeZ; i++){
        if (mapPos.x < -raycastExpandHitbox || mapPos.x >= sizeX + raycastExpandHitbox || mapPos.y < -raycastExpandHitbox || mapPos.y >= sizeY + raycastExpandHitbox || mapPos.z < -raycastExpandHitbox || mapPos.z >= sizeZ + raycastExpandHitbox) break;

        voxel = getVoxelAtXYZ(mapPos.x, mapPos.y, mapPos.z);
        if (voxel.a > 0.999){
            // AO
            vec4 ambient = calcVoxelAo(mapPos - rayStep * ivec3(mask), vec3(mask).zxy, vec3(mask).yzx);
            vec3 endRayPos = rayDir / sum(ivec3(mask) * rayDir) * sum(ivec3(mask) * (mapPos + vec3(lessThan(rayDir, vec3(0))) - rayPos)) + rayPos;
            vec2 aouv = mod(vec2(dot(ivec3(mask) * endRayPos.yzx, vec3(1.)), dot(ivec3(mask) * endRayPos.zxy, vec3(1.))), vec2(1.));
            ao = mix(mix(ambient.z, ambient.w, aouv.x), mix(ambient.y, ambient.x, aouv.x), aouv.y);
            ao = pow(ao, 1. / 3.);
            break;
        }

        mask = lessThanEqual(sideDist.xyz, min(sideDist.yzx, sideDist.zxy));
        sideDist += deltaDist * vec3(mask);
        mapPos += rayStep * ivec3(mask);
    }

    vec3 worldSpacePosition = modelPosition + voxelSize * (rayPos + rayDir * distance(rayPos, mapPos));
    return DDAResult(voxel, mask, distance(worldSpacePosition, position), worldSpacePosition, ao);
}

void main(){
    vec2 uv = uvData.xy;
    float normal = uvData.z;
    vec4 col = vec4(1., 0., 0., 1.);

    vec3 dir = normalize(fragPos - position);//vec3(0., 0., 1.);
    dir.z = -dir.z;
    vec3 start;
    bvec3 mask = bvec3(false);

    if (isNear(normal, 0.)){
        float pixelX = uv.x * float(sizeX);
        float pixelY = uv.y * float(sizeY);
        start = vec3(pixelX, pixelY, 0.);
        mask = bvec3(false, false, true);
    }

    if (isNear(normal, 1.)){
        float pixelX = uv.x * float(sizeX);
        float pixelY = uv.y * float(sizeY);
        start = vec3(pixelX, pixelY, sizeZ);
        mask = bvec3(false, false, true);
    }

    if (isNear(normal, 2.)){
        float pixelZ = uv.x * float(sizeZ);
        float pixelY = uv.y * float(sizeY);
        start = vec3(sizeX, sizeY - pixelY, pixelZ);
        mask = bvec3(true, false, false);
    }

    if (isNear(normal, 3.)){
        float pixelZ = uv.x * float(sizeZ);
        float pixelY = uv.y * float(sizeY);
        start = vec3(0., sizeY - pixelY, sizeZ - pixelZ);
        mask = bvec3(true, false, false);
    }

    if (isNear(normal, 4.)){
        float pixelX = uv.x * float(sizeX);
        float pixelZ = uv.y * float(sizeZ);
        start = vec3(pixelX, sizeY, pixelZ);
        mask = bvec3(false, true, false);
    }

    if (isNear(normal, 5.)){
        float pixelX = uv.x * float(sizeX);
        float pixelZ = uv.y * float(sizeZ);
        start = vec3(pixelX, 0., pixelZ);
        mask = bvec3(false, true, false);
    }

    DDAResult c = raycastDDA(start, dir, mask);

    if (c.color.a < .5) discard;

    gBufferALBEDO = c.color * c.ao;
    gBufferNORMAL = vec3(c.normal);
    gBufferPOSITION = c.position;
    gl_FragDepth = ((1. / c.distance) - (1. / zNear)) / ((1. / float(zFar)) - (1. / zNear));
}