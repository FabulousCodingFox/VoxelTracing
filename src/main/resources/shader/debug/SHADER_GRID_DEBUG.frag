#version 330 core
out vec4 FragColor;

in vec2 uvData;

uniform sampler3D dataContainer;

void main()
{
    FragColor = vec4(uvData.x, uvData.y, 0., 1.);
}