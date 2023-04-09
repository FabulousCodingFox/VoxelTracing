#version 330 core
out vec4 FragColor;

in vec3 uvData;

bool isNear(float a, float b){
    return abs(a-b) < .01;
}

void main(){
    float normal = uvData.z;
    vec4 col = vec4(1., 0., 0., 1.);

    if (gl_FrontFacing){
        if (isNear(normal, 0.)){
            col = vec4(1., 0., 0., 1.);
        }

        if (isNear(normal, 1.)){
            col = vec4(0., 1., 0., 1.);
        }

        if (isNear(normal, 2.)){
            col = vec4(0., 0., 1., 1.);
        }

        if (isNear(normal, 3.)){
            col = vec4(1., 1., 0., 1.);
        }

        if (isNear(normal, 4.)){
            col = vec4(0., 1., 1., 1.);
        }

        if (isNear(normal, 5.)){
            col = vec4(1., 0., 1., 1.);
        }
    } else {
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
    }

    FragColor = col;
}