#version 460 core

layout (location = 0) out vec4 gBufferALBEDO;
layout (location = 1) out vec3 gBufferNORMAL;
layout (location = 2) out vec3 gBufferLIGHTING;

in vec3 uvData;
in vec3 fragPos;

layout (depth_less) out float gl_FragDepth;

uniform sampler3D dataContainer;

uniform vec2 iResolution;

uniform int sizeX;
uniform int sizeY;
uniform int sizeZ;
uniform vec3 modelPosition;

uniform float voxelSize;

uniform vec3 position;
uniform vec3 direction;

uniform float zNear;
const int zFar = 2000;
const float PI = 3.1415926535897932384626433832795;

const int accuracy = 3;
const int raycastExpandHitbox = 2000; //TODO: Remove this

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

float sum(vec3 v) { return dot(v, vec3(1.0)); }

vec4 getVoxelAtXYZ(int x, int y, int z){
    if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) return vec4(0., 1., 0., 0.);
    return texelFetch(dataContainer, ivec3(x, y, z), 0);
}

bool getIfVoxelAtXYZ(vec3 p){
    return getVoxelAtXYZ(int(p.x), int(p.y), int(p.z)).a > 0.9;
}

float calcVertexAo(vec2 side, float corner) {
    //if (side.x == 1.0 && side.y == 1.0) return 1.0;
    return (side.x + side.y + max(corner, side.x * side.y)) / 3.0;
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

float calculateHyperbolicDepth(float distance, float zn, int zf){
    return ((1. / distance) - (1. / zn)) / ((1. / float(zf)) - (1. / zn));
}

DDAResult raycastDDA(vec3 rayPos, vec3 rayDir){
    // DDA
    ivec3 mapPos = ivec3(floor(rayPos + 0.));
    vec3 deltaDist = abs(vec3(length(rayDir)) / rayDir);
    ivec3 rayStep = ivec3(sign(rayDir));
    vec3 sideDist = (sign(rayDir) * (vec3(mapPos) - rayPos) + (sign(rayDir) * 0.5) + 0.5) * deltaDist;
    vec4 voxel = vec4(0.);
    float ao = 1.;
    bvec3 mask = bvec3(false);

    for (int i = 0; i < sizeX + sizeY + sizeZ; i++){
        if(mapPos.x < -raycastExpandHitbox || mapPos.x >= sizeX + raycastExpandHitbox || mapPos.y < -raycastExpandHitbox || mapPos.y >= sizeY + raycastExpandHitbox || mapPos.z < -raycastExpandHitbox || mapPos.z >= sizeZ + raycastExpandHitbox) break;

        voxel = getVoxelAtXYZ(mapPos.x, mapPos.y, mapPos.z);
        if(voxel.a > 0.999){
            // AO
            vec3 intersectPlane = mapPos + vec3(lessThan(rayDir, vec3(0)));
            vec3 endRayPos;
            vec2 uv;
            vec4 ambient;
            ambient = calcVoxelAo(mapPos - rayStep * ivec3(mask), vec3(mask).zxy, vec3(mask).yzx);
            endRayPos = rayDir / sum(ivec3(mask) * rayDir) * sum(ivec3(mask) * (mapPos + vec3(lessThan(rayDir, vec3(0))) - rayPos)) + rayPos;
            vec2 aouv = mod(vec2(dot(ivec3(mask) * endRayPos.yzx, vec3(1.0)), dot(ivec3(mask) * endRayPos.zxy, vec3(1.0))), vec2(1.0));
            ao = mix(mix(ambient.z, ambient.w, aouv.x), mix(ambient.y, ambient.x, aouv.x), aouv.y);
            ao = pow(ao, 1.0 / 3.0);
            break;
        }

        mask = lessThanEqual(sideDist.xyz, min(sideDist.yzx, sideDist.zxy));
        sideDist += deltaDist * vec3(mask);
        mapPos += rayStep * ivec3(mask);
    }

    // DISTANCE
    vec3 pos = modelPosition + (vec3(mapPos.x * 2, mapPos.y * 2, sizeZ) - vec3(mapPos)) * voxelSize;

    return DDAResult(voxel, mask, length(position - pos), pos, ao);
}

/*struct faceCollission{
    bool hit;
    vec3 position;
};

faceCollission getFaceCollission(vec3 face, vec3 rayStart, vec3 rayDirection, vec3 boxSize){
    vec3 facePosition = vec3(face.x > 0 ? boxSize.x : 0, face.y > 0 ? boxSize.y : 0, face.z > 0 ? boxSize.z : 0);
    float t = dot(facePosition - rayStart, face) / dot(rayDirection, face);
    vec3 hitPoint = rayStart + rayDirection * t;
    if(t > 0. && hitPoint.x <= 0 + boxSize.x && hitPoint.y >= 0 && hitPoint.y <= 0 + boxSize.y && hitPoint.z >= 0 && hitPoint.z <= 0 + boxSize.z){
        return faceCollission(true, hitPoint);
    }
    return faceCollission(false, vec3(0));
}*/

void swap(float a, float b){
    float temp = a;
    a = b;
    b = temp;
}

void main(){
    //if(gl_FragCoord.z > texelFetch(gBufferDEPTH_READ, ivec2(gl_FragCoord.xy), 0).r) discard;

    // Box verts
    // 0, 0, 0: 0, 0, 0
    // 1, 0, 0: sizeX, 0, 0
    // 0, 1, 0: 0, sizeY, 0
    // 1, 1, 0: sizeX, sizeY, 0
    // 0, 0, 1: 0, 0, sizeZ
    // 1, 0, 1: sizeX, 0, sizeZ
    // 0, 1, 1: 0, sizeY, sizeZ
    // 1, 1, 1: sizeX, sizeY, sizeZ


    //vec3 totalOffset = (((fragPos - modelPosition) / (vec3(sizeX, sizeY, sizeZ) * voxelSize)) * vec3(sizeX, sizeY, sizeZ)) - playerOffset;

    //vec3 rayDir = normalize(position - fragPos);
    //vec3 fragmentOffset = ((fragPos - modelPosition) / (vec3(sizeX, sizeY, sizeZ) * voxelSize)) * vec3(sizeX, sizeY, sizeZ);
    //vec3 boxSize = vec3(sizeX, sizeY, sizeZ);

    // The fragmentOffset is inside a back face of the cube. We need to get the front face of the cube that the ray is pointing at
    // We can do this by casting a ray from the fragmentOffset to the player position and getting the first face that the ray hits

    //faceCollission coll;

    // Face +x
    /*coll = getFaceCollission(vec3(1, 0, 0), fragmentOffset, rayDir, boxSize);
    if(!coll.hit) coll = getFaceCollission(vec3(-1, 0, 0), fragmentOffset, rayDir, boxSize);
    if(!coll.hit) coll = getFaceCollission(vec3(0, 1, 0), fragmentOffset, rayDir, boxSize);
    if(!coll.hit) coll = getFaceCollission(vec3(0, -1, 0), fragmentOffset, rayDir, boxSize);
    if(!coll.hit) coll = getFaceCollission(vec3(0, 0, 1), fragmentOffset, rayDir, boxSize);
    if(!coll.hit) coll = getFaceCollission(vec3(0, 0, -1), fragmentOffset, rayDir, boxSize);

    if(coll.hit){
        gBufferALBEDO = vec4(coll.position, 1);
        gBufferNORMAL = vec3(coll.position);
        gBufferLIGHTING = vec3(1);
        return;
    }else{
        gBufferALBEDO = vec4(0);
        gBufferNORMAL = vec3(0);
        gBufferLIGHTING = vec3(1);
        return;
    }*/

    /*gBufferALBEDO = vec4(0);
    gBufferNORMAL = vec3(0);
    gBufferLIGHTING = vec3(0);
    return;

    // Get which face of the cube the ray with origin fragPos and the direction rayDir is pointing at


    gBufferALBEDO = vec4(face, 1);
    gBufferNORMAL = vec3(face);
    gBufferLIGHTING = vec3(1);


    // Get bvec3 face from ray direction

    // Yaw -90 to 90
    // Pitch 0 to 360



    // Get voxel
    DDAResult dda = raycastDDA(position, rayDir, mask);
    gBufferALBEDO = dda.color;
    gBufferNORMAL = vec3(dda.normal);
    gBufferLIGHTING = vec3(dda.ao);*/

    //bool playerOutsideOfBox = false;//playerOffset.x < -accuracy || playerOffset.x > sizeX + accuracy || playerOffset.y < -accuracy || playerOffset.y > sizeY + accuracy || playerOffset.z < -accuracy || playerOffset.z > sizeZ + accuracy;

    /*if(playerOutsideOfBox){
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
    }*/


    //gBufferMATERIAL = vec4(0., 0., 1., 0.);
    //if(isNear(gBufferALBEDO.y, .0)){
    //    gBufferMATERIAL.x = 1.;
    //}

    //gBufferALBEDO = vec4(vec3(texelFetch(gBufferDEPTH, ivec2(gl_FragCoord.xy), 0).r), 1.);


    //float hyperbolicDepth = calculateHyperbolicDepth(c.distance, zNear, zFar);
    // float linearDepth = c.distance / 10.;
    //if(hyperbolicDepth > texelFetch(gBufferDEPTH, ivec2(gl_FragCoord.xy), 0).r) discard;
    //gBufferPosition = vec4(c.position, hyperbolicDepth);
    //gBufferDEPTH_WRITE = linearDepth;
    //gBufferDEPTH_WRITE = hyperbolicDepth;


    //storeFloatImageDEPTH(gl_FragCoord.xy, .9);//hyperbolicDepth);

    //imageStore(gBufferDEPTH, ivec2(gl_FragCoord.xy), hyperbolicDepth);
    //storeFloatImage(gBufferDEPTH, ivec2(gl_FragCoord.xy), hyperbolicDepth);
    //imageAtomicExchange(gBufferDEPTH, ivec2(gl_FragCoord.xy), uint(intFromDecimalPlaces(hyperbolicDepth)));*/




    // Camera offset relative to the box origin (0, 0, 0)
    vec3 playerOffset = ((position - modelPosition) / (vec3(sizeX, sizeY, sizeZ) * voxelSize)) * vec3(sizeX, sizeY, sizeZ);
    vec3 fragOffset = ((fragPos - modelPosition) / (vec3(sizeX, sizeY, sizeZ) * voxelSize)) * vec3(sizeX, sizeY, sizeZ);
    bool playerOutsideOfBox = playerOffset.x < -accuracy || playerOffset.x > sizeX + accuracy || playerOffset.y < -accuracy || playerOffset.y > sizeY + accuracy || playerOffset.z < -accuracy || playerOffset.z > sizeZ + accuracy;
    vec3 dir = normalize(fragPos - position); //vec3(0., 0., 1.);
    vec3 invdir = 1. / dir;
    vec3 start = playerOffset;




    if(playerOutsideOfBox){
        // Shooting a ray from fragOffset to playerOffset
        // Get the position the ray collides with the box faces
        // Get the face the ray is pointing at

        float t1 = (0. - playerOffset.x) * invdir.x;
        float t2 = (sizeX - playerOffset.x) * invdir.x;
        float t3 = (0. - playerOffset.y) * invdir.y;
        float t4 = (sizeY - playerOffset.y) * invdir.y;
        float t5 = (0. - playerOffset.z) * invdir.z;
        float t6 = (sizeZ - playerOffset.z) * invdir.z;

        float tmin = max(max(min(t1, t2), min(t3, t4)), min(t5, t6));
        float tmax = min(min(max(t1, t2), max(t3, t4)), max(t5, t6));

        start = playerOffset + tmin * dir;
    }

    dir.z = -dir.z;
    start.z = sizeZ - start.z;
    DDAResult c = raycastDDA(start, dir);
    if(c.color.a < .5) discard;

    gBufferALBEDO = c.color;
    gBufferNORMAL = vec3(c.normal);
    gBufferLIGHTING = vec3(c.ao);

    float hyperbolicDepth = ((1. / c.distance) - (1. / zNear)) / ((1. / float(zFar)) - (1. / zNear));
    gl_FragDepth = hyperbolicDepth;
}