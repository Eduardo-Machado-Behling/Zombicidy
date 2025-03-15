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
  vec3 ambient;
  vec3 diffuse;
  vec3 specular;
};

struct PointLight {
  vec3 position;

  Light light;

  float constant;
  float linear;
  float quadratic;
};

struct DirectionalLight {
  vec3 direction;
  Light light;
};

in vec2 texCoords;
in vec3 normal;
in vec3 fragPos;
flat in int materialIndex;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform vec3 viewPos;

uniform vec3 solidColor;
uniform float blendFactor;

uniform Material materials[10];

uniform int pLightCount;
uniform int isTextured;

uniform PointLight pLights[10];
uniform DirectionalLight dLight;

vec3 CalcDirLight(DirectionalLight light, vec3 normal, vec3 viewDir,
                  Material material);
vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir,
                    Material material);
void main() {
  // properties
  vec3 norm = normalize(normal);
  vec3 viewDir = normalize(viewPos - fragPos);

  vec3 result = CalcDirLight(dLight, norm, viewDir, materials[materialIndex]);

  for (int i = 0; i < pLightCount; i++)
    result += CalcPointLight(pLights[i], norm, fragPos, viewDir,
                             materials[materialIndex]);

  // phase 3: Spot light
  // result += CalcSpotLight(spotLight, norm, FragPos, viewDir);

  vec4 objectColor = vec4(solidColor, 1.0);
  if (isTextured == 1) {
    vec4 textureColor = texture(textureSampler, texCoords);
    objectColor = mix(textureColor, objectColor, blendFactor);
  }

  result *= objectColor.xyz;
  fragColor = vec4(result, 1.0);
  // fragColor = vec4(1.0, 1.0, 1.0, 1.0);
  // fragColor = texture(textureSampler, texCoords);
}

vec3 CalcDirLight(DirectionalLight light, vec3 normal, vec3 viewDir,
                  Material material) {
  vec3 lightDir = normalize(-light.direction);
  // diffuse shading
  float diff = max(dot(normal, lightDir), 0.0);
  // specular shading
  vec3 reflectDir = reflect(-lightDir, normal);
  float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
  // combine results
  vec3 ambient = light.light.ambient * material.ambient;
  vec3 diffuse = light.light.diffuse * diff * material.diffuse;
  vec3 specular = light.light.specular * spec * material.specular;
  return (ambient + diffuse + specular);
}

vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir,
                    Material material) {
  vec3 lightDir = normalize(light.position - fragPos);
  // diffuse shading
  float diff = max(dot(normal, lightDir), 0.0);
  // specular shading
  vec3 reflectDir = reflect(-lightDir, normal);
  float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
  // attenuation
  float distance = length(light.position - fragPos);
  float attenuation = 1.0 / (light.constant + light.linear * distance +
                             light.quadratic * (distance * distance));
  // combine results
  vec3 ambient = light.light.ambient * material.diffuse;
  vec3 diffuse = light.light.diffuse * diff * material.diffuse;
  vec3 specular = light.light.specular * spec * material.specular;
  ambient *= attenuation;
  diffuse *= attenuation;
  specular *= attenuation;
  return (ambient + diffuse + specular);
}