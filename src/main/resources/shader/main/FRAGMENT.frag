#version 330 core
out vec4 FragColor;

in vec3 uvData;
in vec3 fragPos;

uniform sampler3D dataContainer;

uniform int sizeX;
uniform int sizeY;
uniform int sizeZ;
uniform float voxelSize;
uniform vec3 modelPosition;

uniform vec3 position;
uniform vec3 direction;

const int accuracy = 3;
const int raycastExpandHitbox = 3;

struct DDAResult{
    vec4 color;
    bvec3 normal;
    float distance;
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

vec4 raycast(vec3 rayPos, vec3 rayDir){
    rayPos += rayDir / 10;
    for(int i=0;i<10*(sizeX + sizeY + sizeZ);++i)
    {
        ivec3 mapPos = ivec3(floor(rayPos + 0.));

        if(mapPos.x < -raycastExpandHitbox || mapPos.x >= sizeX + raycastExpandHitbox || mapPos.y < -raycastExpandHitbox || mapPos.y >= sizeY + raycastExpandHitbox || mapPos.z < -raycastExpandHitbox || mapPos.z >= sizeZ + raycastExpandHitbox) break;

        vec4 voxel = getVoxelAtXYZ(mapPos.x, mapPos.y, mapPos.z);
        if(voxel.a > 0.9) return voxel;
        rayPos += rayDir / 10;
    }
    return vec4(0., 1., 0., 0.);
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

    if (mask.x) {
        voxel.xyz *= 0.5;
    }
    if (mask.y) {
        voxel.xyz *= 1.0;
    }
    if (mask.z) {
        voxel.xyz *= 0.75;
    }

    return DDAResult(voxel, mask, 0.);
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
    FragColor = c.color;
}