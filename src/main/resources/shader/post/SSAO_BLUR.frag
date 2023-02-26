#version 330 core

out vec4 FragColor;

uniform vec2 iResolution;

uniform sampler2D ssaoInput;

void main(){
    vec2 uv = gl_FragCoord.xy/iResolution.xy;

    vec2 texelSize = 1.0 / vec2(textureSize(ssaoInput, 0));
    float result = 0.0;
    for (int x = -2; x < 2; ++x)
    {
        for (int y = -2; y < 2; ++y)
        {
            vec2 offset = vec2(float(x), float(y)) * texelSize;
            result += texture(ssaoInput, uv + offset).r;
        }
    }
    FragColor = vec4(vec3(result / (4.0 * 4.0)), 1.);
}