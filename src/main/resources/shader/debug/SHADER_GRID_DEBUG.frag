#version 330 core
out vec4 FragColor;

in vec3 uvData;
in vec3 fragPos;

uniform sampler2D dataContainer;

uniform int sizeX;
uniform int sizeY;
uniform int sizeZ;
uniform int textureSizeX;
uniform int textureSizeY;
uniform float voxelSize;
uniform int MAX_TEXTURE_SIZE;

uniform vec2 iResolution;
uniform float iTime;

uniform vec3 position;
uniform vec3 direction;

const int RAY_STEPS = 10;

bool isNear(float a, float b){
    return abs(a-b) < .01;
}

vec4 getVoxelAtXYZ(int x, int y, int z){
    if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) return vec4(1., 1., 1., 0.);

    y = sizeY - y - 1;

    int overflows = 0;
    int xp = z;
    int yp = x + y * sizeX;

    while (yp >= MAX_TEXTURE_SIZE) {
        yp -= MAX_TEXTURE_SIZE;
        xp += sizeZ;
        overflows+=1;
    }

    return texture(dataContainer, vec2(float(xp) / float(textureSizeX), float(yp) / float(textureSizeY)));

    /*//int posX = z + sizeZ * int(floor(sizeX * sizeY / 4000.));
    //int posY = Math.min(4000,image.getHeight() - (voxel.getPosition().x + voxel.getPosition().z * sizeX));

    vec2 coords = vec2(
        float(z) / float(sizeZ),
        float(x + y * sizeX) / float(sizeY * sizeX)
    );
    vec4 t = texture(dataContainer, coords);
    return t;*/
}

vec4 raycast(vec3 rayPos, vec3 rayDir){
    rayPos += rayDir / RAY_STEPS;
    for(int i=0;i<RAY_STEPS*(sizeX + sizeY + sizeZ);++i)
    {
        ivec3 mapPos = ivec3(floor(rayPos + 0.));
        vec4 voxel = getVoxelAtXYZ(mapPos.x, mapPos.y, mapPos.z);
        if(voxel.a > 0.9) return voxel;
        rayPos += rayDir / RAY_STEPS;
    }

    return vec4(0., 1., 0., 0.);
}

void main(){
    vec2 uv = uvData.xy;
    float normal = uvData.z;
    vec4 col = vec4(1., 0., 0., 1.);

    vec3 dir = normalize(fragPos - position); //vec3(0., 0., 1.);
    dir.z = -dir.z;
    vec3 start;

    if(isNear(normal, 0.)){
        float pixelX = uv.x * float(sizeX);
        float pixelY = uv.y * float(sizeY);
        start = vec3(pixelX, pixelY, 0.);
    }

    if(isNear(normal, 1.)){
        float pixelX = uv.x * float(sizeX);
        float pixelY = uv.y * float(sizeY);
        start = vec3(pixelX, pixelY, sizeZ);
    }

    if(isNear(normal, 2.)){
        float pixelZ = uv.x * float(sizeZ);
        float pixelY = uv.y * float(sizeY);
        start = vec3(sizeX, sizeY - pixelY, pixelZ);
    }

    if(isNear(normal, 3.)){
        float pixelZ = uv.x * float(sizeZ);
        float pixelY = uv.y * float(sizeY);
        start = vec3(0., sizeY - pixelY, sizeZ - pixelZ);
    }

    if(isNear(normal, 4.)){
        float pixelX = uv.x * float(sizeX);
        float pixelZ = uv.y * float(sizeZ);
        start = vec3(pixelX, sizeY, pixelZ);
    }

    if(isNear(normal, 5.)){
        float pixelX = uv.x * float(sizeX);
        float pixelZ = uv.y * float(sizeZ);
        start = vec3(pixelX, 0., pixelZ);
    }

    vec4 c = raycast(start, dir);
    //if(c.a < .5) discard;
    FragColor = c;
}