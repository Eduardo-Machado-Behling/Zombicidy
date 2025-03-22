#version 330 core

in vec2 texCoords;
out vec4 fragColor;

uniform vec3 solidColor;
uniform float blendFactor;
uniform sampler2D textureSampler;
uniform uint isTextured;

uniform float progress;

void main() {
  vec4 textureColor = texture(textureSampler, texCoords);
  vec4 objectColor;

  if (isTextured == 1u) {
    objectColor =
        vec4(mix(textureColor.xyz, solidColor, blendFactor), textureColor.w);
    if (texCoords.x < progress) {
      fragColor = objectColor;
    } else {
      fragColor = vec4(0.0, 0.0, 0.0, textureColor.w);
    }
  } else {
    fragColor = vec4(0.0, 0.0, 0.0, 0.0);
  }
}