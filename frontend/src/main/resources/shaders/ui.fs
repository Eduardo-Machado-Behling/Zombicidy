#version 330 core

in vec2 texCoords;
out vec4 fragColor;

uniform vec3 solidColor;
uniform float blendFactor;
uniform sampler2D textureSampler;
uniform uint isTextured;

void main() {
  if (isTextured == 1u) {
    vec4 textureColor = texture(textureSampler, texCoords);
    vec3 objectColor = solidColor;
    objectColor = mix(textureColor.xyz, solidColor, blendFactor);
    fragColor = vec4(objectColor, textureColor.w);
  } else {
    fragColor = vec4(0.0, 0.0, 0.0, 0.0);
  }
}