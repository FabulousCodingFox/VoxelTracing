#version 330 core
out vec4 FragColor;

in vec2 uvData;

void main()
{
    FragColor = vec4(uvData.x, uvData.y, 0., 1.);
}