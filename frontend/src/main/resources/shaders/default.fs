#version 330 core

in vec2 texCoords;
in vec3 normal;

out vec4 fragColor;

uniform sampler2D textureSampler;

void main() { fragColor = texture(textureSampler, texCoords); }