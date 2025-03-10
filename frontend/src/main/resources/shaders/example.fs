#version 330 core

in vec2 texCoords; // <- Now it matches the vertex shader output
out vec4 fragColor;

uniform sampler2D textureSampler;

void main() {
  //   fragColor = vec4(1.0, 1.0, 1.0, 1.0);
  fragColor = texture(textureSampler, texCoords);
}