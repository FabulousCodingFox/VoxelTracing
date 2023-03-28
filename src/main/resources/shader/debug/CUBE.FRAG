#version 330 core

layout (location = 0) out vec4 gBufferALBEDO;
layout (location = 1) out vec3 gBufferNORMAL;
layout (location = 2) out vec4 gBufferMATERIAL;
layout (location = 3) out vec4 gBufferPosition;
layout (location = 4) out vec3 gBufferLIGHTING;

in vec3 uvData;
in vec3 fragPos;

bool isNear(float a, float b){
    return abs(a-b) < .01;
}

void main(){
    float normal = uvData.z;
    vec4 col = vec4(1., 0., 0., 1.);

    if (isNear(normal, 1.)){
        col = vec4(1., 0., 0., 1.);
    }

    if (isNear(normal, 0.)){
        col = vec4(0., 1., 0., 1.);
    }

    if (isNear(normal, 3.)){
        col = vec4(0., 0., 1., 1.);
    }

    if (isNear(normal, 2.)){
        col = vec4(1., 1., 0., 1.);
    }

    if (isNear(normal, 5.)){
        col = vec4(0., 1., 1., 1.);
    }

    if (isNear(normal, 4.)){
        col = vec4(1., 0., 1., 1.);
    }

    gBufferALBEDO = col;
    gBufferNORMAL = col.xyz;
    gBufferMATERIAL = vec4(1., 1., 1., 0.);
    gBufferPosition = vec4(0., 0., 0., 0.);
    gBufferLIGHTING = col.xyz;
}