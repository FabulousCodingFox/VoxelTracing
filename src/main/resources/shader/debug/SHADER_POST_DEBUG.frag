#version 330 core

out vec4 FragColor;

in vec2 uv;

uniform vec2 iResolution;
uniform sampler2D screenTexture;

void main(){
    vec2 uv = gl_FragCoord.xy/iResolution.xy;
    FragColor = vec4(texture(screenTexture, uv), 1.0);
}