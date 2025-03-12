#version 330 core

struct Material {
  vec3 ambient;
  vec3 diffuse;
  vec3 specular;

  float shininess;
  float transparency;
  float specularExp;
};

struct Light {
  vec3 position;

  vec3 ambient;
  vec3 diffuse;
  vec3 specular;
};

in vec2 texCoords;
in vec3 normal;
in vec3 fragPos;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform vec3 lightPos;
uniform vec3 viewPos;

uniform vec3 solidColor;
uniform float blendFactor;

uniform Material materials[10];
uniform Light light;

void main() {
  vec4 textureColor = texture(textureSampler, texCoords);
  vec4 solid = vec4(solidColor, 1.0);
  vec4 objectColor = mix(textureColor, solid, blendFactor);

  vec3 ambient = light.ambient * materials[0].ambient;

  vec3 norm = normalize(normal);
  vec3 lightDir = normalize(lightPos - fragPos);
  float diff = max(dot(norm, lightDir), 0.0);
  vec3 diffuse = light.diffuse * (diff * materials[0].diffuse);

  vec3 viewDir = normalize(viewPos - fragPos);
  vec3 reflectDir = reflect(-lightDir, norm);
  float spec =
      pow(max(dot(viewDir, reflectDir), 0.0), materials[0].specularExp);
  vec3 specular = light.specular * (spec * materials[0].specular);

  // vec4 objectColor = vec4(1.0, 0.5, 0.6, 1.0);
  vec3 result = (ambient + diffuse + specular) * objectColor.xyz;
  fragColor = vec4(result, 1.0);
  // fragColor = texture(textureSampler, texCoords);
}