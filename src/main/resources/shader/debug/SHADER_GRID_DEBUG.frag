#version 330 core
out vec4 FragColor;

in vec3 uvData;

uniform sampler2D dataContainer;

uniform int sizeX;
uniform int sizeY;
uniform int sizeZ;

uniform float voxelSize;

uniform vec3 position;
uniform vec3 direction;

const int MAX_RAY_STEPS = 128;

bool isNear(float a, float b){
    return abs(a-b) < .01;
}

vec3 getVoxel(int x, int y, int z){
    vec2 coords = vec2(float(x) / float(sizeX), float(z + y * sizeZ) / float(sizeY * sizeZ));
    return texture(dataContainer,coords).rgb;
}

vec3 raycast(vec3 start, vec3 rayDir){

    ivec3 mapPos = ivec3(floor(start + 0.));
    vec3 deltaDist = abs(vec3(length(rayDir)) / rayDir);
    ivec3 rayStep = ivec3(sign(rayDir));
    vec3 sideDist = (sign(rayDir) * (vec3(mapPos) - start) + (sign(rayDir) * 0.5) + 0.5) * deltaDist;

    bvec3 mask;

    for (int i = 0; i < MAX_RAY_STEPS; i++){
        if(getVoxel(mapPos.x, mapPos.y, mapPos.z).x > 0.){
            return vec3(1., 1., 1.);
            //return getVoxel(mapPos.x, mapPos.y, mapPos.z);
        }
        if(mapPos.x < 0 || mapPos.x >= sizeX || mapPos.y < 0 || mapPos.y >= sizeY || mapPos.z < 0 || mapPos.z >= sizeZ){
            return vec3(0., 0., 0.);
        }

        mask = lessThanEqual(sideDist.xyz, min(sideDist.yzx, sideDist.zxy));
        sideDist += vec3(mask) * deltaDist;
        mapPos += ivec3(vec3(mask)) * rayStep;
    }

    return vec3(0., 0., 0.);
}

void main()
{
    vec2 uv = uvData.xy;
    float normal = uvData.z;

    vec4 col = vec4(0., 0., 0., 1.);

    if(isNear(normal, 0.)){


        int pixelX = int(uv.x * float(sizeX));
        int pixelY = int(uv.y * float(sizeY));

        //col = vec4(float(pixelX) / float(sizeX), float(pixelY) / float(sizeY), 0., 1.);

        //vec3 start = vec3(float(pixelX), float(pixelY), 0.);
        //vec3 dir = vec3(0., 0., 1.);
        //col = vec4(raycast(start, dir), 1.);

        col = vec4(getVoxel(pixelX, pixelY, 20), 1.);


        //col = texture(dataContainer, uv);

    }
    if(isNear(normal, 1.)) col = vec4(0., 1., 0., 1.);
    if(isNear(normal, 2.)) col = vec4(0., 0., 1., 1.);
    if(isNear(normal, 3.)) col = vec4(1., 1., 0., 1.);
    if(isNear(normal, 4.)) col = vec4(1., 0., 1., 1.);
    if(isNear(normal, 5.)) col = vec4(0., 1., 1., 1.);
    if(isNear(normal, 6.)) col = vec4(1., 1., 1., 1.);
    FragColor = col;






}