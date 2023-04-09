#version 330 core
layout (location = 0) in vec3 inputPosition;
layout (location = 1) in vec3 inputUV;

out vec3 uvData;
out vec3 fragPos;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform int sizeX;
uniform int sizeY;
uniform int sizeZ;

uniform float voxelSize;

void main()
{
    vec3 vertPosition = inputPosition;

    if (vertPosition.x > 0.5) vertPosition.x = voxelSize * sizeX;
    if (vertPosition.y > 0.5) vertPosition.y = voxelSize * sizeY;
    if (vertPosition.z > 0.5) vertPosition.z = voxelSize * sizeZ;

    gl_Position = projection * view * model * vec4(vertPosition, 1.0);
    fragPos = vec4(model * vec4(vertPosition, 1.0)).xyz;
    uvData = inputUV;
}