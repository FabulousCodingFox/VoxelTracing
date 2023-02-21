#version 330 core

out vec4 FragColor;

uniform vec2 iResolution;
uniform sampler2D screenTexture;

void main(){
    vec2 uv = gl_FragCoord.xy/iResolution.xy;
    FragColor = texture(screenTexture, uv);
}