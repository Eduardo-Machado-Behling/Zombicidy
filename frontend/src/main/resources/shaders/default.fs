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

struct SpotLight {
  vec3 position;
  vec3 direction;
  float cutout;

  Light light;
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

uniform uint pLightCount;
uniform uint spotLightCount;
uniform uint isTextured;

uniform PointLight pLights[10];
uniform SpotLight spotLights[10];
uniform DirectionalLight dLight;

vec3 CalcDirLight(DirectionalLight light, vec3 normal, vec3 viewDir,
                  Material material);
vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir,
                    Material material);
vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir,
                   Material material);
void main() {
  // properties
  vec3 norm = normalize(normal);
  vec3 viewDir = normalize(viewPos - fragPos);

  vec3 result = CalcDirLight(dLight, norm, viewDir, materials[materialIndex]);

  for (uint i = 0u; i < pLightCount; i++) {
    result += CalcPointLight(pLights[i], norm, fragPos, viewDir,
                             materials[materialIndex]);
  }

  for (uint j = 0u; j < spotLightCount; j++) {
    result += CalcSpotLight(spotLights[j], norm, fragPos, viewDir,
                            materials[materialIndex]);
  }

  // phase 3: Spot light
  // result += CalcSpotLight(spotLight, norm, FragPos, viewDir);

  vec4 objectColor = vec4(solidColor, 1.0);
  if (isTextured == 1u) {
    // fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    vec4 textureColor = texture(textureSampler, texCoords);
    objectColor = mix(textureColor, objectColor, blendFactor);
  } else {
    // fragColor = vec4(1.0, 1.0, 1.0, 1.0);
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
  vec3 ambient = light.light.ambient * material.ambient;
  vec3 diffuse = light.light.diffuse * diff * material.diffuse;
  vec3 specular = light.light.specular * spec * material.specular;
  ambient *= attenuation;
  diffuse *= attenuation;
  specular *= attenuation;
  return (ambient + diffuse + specular);
}

vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir,
                   Material material) {

  // Step 1: Calculate the direction from the fragment to the light source
  vec3 lightDirNormalized = normalize(light.direction);
  vec3 fragToLight =
      normalize(light.position - fragPos); // Vector from fragment to light

  // Step 2: Calculate the cosine of the angle between the fragment-to-light
  // vector and light direction
  float cosTheta = dot(fragToLight, lightDirNormalized);

  // Step 3: Calculate the spotlight intensity based on the angle
  float intensity = 0.0;

  if (cosTheta > light.cutout) {
    // If inside the cone, calculate the lighting intensity
    intensity = 1.0; // Full intensity inside the cutoff angle
  }

  vec3 ambient = 0.1 * light.light.ambient * material.ambient; // Ambient light
  vec3 diffuse = max(dot(normal, fragToLight), 0.0) * intensity *
                 light.light.diffuse; // Diffuse lighting

  vec3 finalColor = ambient + diffuse;

  return finalColor;
}