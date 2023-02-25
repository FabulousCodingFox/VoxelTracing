#version 330 core
out vec4 FragColor;

in vec3 uvData;
in vec3 fragPos;

uniform sampler3D dataContainer;

uniform int sizeX;
uniform int sizeY;
uniform int sizeZ;
uniform float voxelSize;

uniform vec3 position;
uniform vec3 direction;

bool isNear(float a, float b){
    return abs(a-b) < .01;
}

void main(){
    vec2 uv = uvData.xy;
    float normal = uvData.z;
    vec4 col = vec4(1., 0., 0., 1.);

    if(isNear(normal, 0.)){
        col = vec4(1., 0., 0., 1.);
    }

    if(isNear(normal, 1.)){
        col = vec4(0., 1., 0., 1.);
    }

    if(isNear(normal, 2.)){
        col = vec4(0., 0., 1., 1.);
    }

    if(isNear(normal, 3.)){
        col = vec4(1., 1., 0., 1.);
    }

    if(isNear(normal, 4.)){
        col = vec4(0., 1., 1., 1.);
    }

    if(isNear(normal, 5.)){
        col = vec4(1., 0., 1., 1.);
    }

    FragColor = col;
}