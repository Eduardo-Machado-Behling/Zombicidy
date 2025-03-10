#version 330 core

in vec2 texCoords;
in vec3 normal;

out vec4 fragColor;

void main() {
  fragColor = vec4(1.0, 1.0, 1.0, 1.0);
  // fragColor = texture(textureSampler, texCoords);
}