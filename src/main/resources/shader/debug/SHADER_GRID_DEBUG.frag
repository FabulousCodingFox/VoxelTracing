#version 330 core
out vec4 FragColor;

in vec3 uvData;
in vec3 fragPos;

uniform sampler2D dataContainer;

uniform int sizeX;
uniform int sizeY;
uniform int sizeZ;

uniform float voxelSize;

uniform vec2 iResolution;
uniform float iTime;

uniform vec3 position;
uniform vec3 direction;

const int RAY_STEPS = 1;

bool isNear(float a, float b){
    return abs(a-b) < .01;
}

vec4 getVoxelAtXYZ(int x, int y, int z){
    if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) return vec4(1., 1., 1., 0.);
    vec2 coords = vec2(
        float(z) / float(sizeZ),
        float(x + y * sizeX) / float(sizeY * sizeX)
    );
    vec4 t = texture(dataContainer, coords);
    return t;
}

vec4 raycast(vec3 origin, vec3 direction){
    vec3 rayDir = normalize(direction);
    vec3 rayPos = origin;
    rayPos += rayDir / RAY_STEPS;

    for(int i=0;i<RAY_STEPS*(sizeX + sizeY + sizeZ);++i)
    {
        ivec3 mapPos = ivec3(floor(rayPos + 0.));
        //if(mapPos.x < 0 || mapPos.x >= sizeX || mapPos.y < 0 || mapPos.y >= sizeY || mapPos.z < 0 || mapPos.z >= sizeZ) return vec4(1., 1., 1., 0.);
        vec4 voxel = getVoxelAtXYZ(mapPos.x, mapPos.y, mapPos.z);
        if(voxel.a > 0.999) return voxel;
        rayPos += rayDir / RAY_STEPS;
    }

    return vec4(0., 1., 0., 0.);
}

void main(){
    vec2 uv = uvData.xy;
    float normal = uvData.z;
    vec4 col = vec4(0., 0., 0., 1.);

    vec3 dir = normalize(fragPos - position); //vec3(0., 0., 1.);
    dir.z = -dir.z;

    if(isNear(normal, 0.)){
        float pixelX = uv.x * float(sizeX);
        float pixelY = uv.y * float(sizeY);
        vec3 start = vec3(pixelX, pixelY, 0.);
        col = vec4(raycast(start, dir));
    }

    if(isNear(normal, 1.)){
        float pixelX = uv.x * float(sizeX);
        float pixelY = uv.y * float(sizeY);
        vec3 start = vec3(pixelX, pixelY, sizeZ);
        col = vec4(raycast(start, dir));
    }

    if(isNear(normal, 2.)){
        float pixelZ = uv.x * float(sizeZ);
        float pixelY = uv.y * float(sizeY);
        vec3 start = vec3(sizeX, sizeY - pixelY, pixelZ);
        col = vec4(raycast(start, dir));
    }

    if(isNear(normal, 3.)){
        float pixelZ = uv.x * float(sizeZ);
        float pixelY = uv.y * float(sizeY);
        vec3 start = vec3(0., sizeY - pixelY, sizeZ - pixelZ);
        col = vec4(raycast(start, dir));
    }

    if(isNear(normal, 4.)){
        float pixelX = uv.x * float(sizeX);
        float pixelZ = uv.y * float(sizeZ);
        vec3 start = vec3(pixelX, sizeY, pixelZ);
        col = vec4(raycast(start, dir));
    }

    if(isNear(normal, 5.)){
        float pixelX = uv.x * float(sizeX);
        float pixelZ = uv.y * float(sizeZ);
        vec3 start = vec3(pixelX, 0., pixelZ);
        col = vec4(raycast(start, dir));
    }

    if(col.a < .5) discard;

    FragColor = col;
}