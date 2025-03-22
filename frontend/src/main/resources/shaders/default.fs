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

uniform int pLightCount;
uniform int spotLightCount;
uniform int isTextured;

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
  vec3 normal = normalize(normal);
  vec3 viewDir = normalize(viewPos - fragPos);
  // vec3 result = vec3(1.0, 1.0, 1.0);
  vec4 textureColor;
  vec4 objectColor = vec4(solidColor, 1.0);
  Material mat = materials[materialIndex];
  vec3 result = vec3(1.0, 1.0, 1.0);

  // vec3 result = CalcDirLight(dLight, normal, viewDir, mat);

  // for (int i = 0; i < pLightCount; i++) {
  //   result += CalcPointLight(pLights[i], normal, fragPos, viewDir,
  //                            mat);
  // }

  // for (int j = 0; j < spotLightCount; j++) {
  //   result += CalcSpotLight(spotLights[j], normal, fragPos, viewDir,
  //                           materials[materialIndex]);
  // }

  // phase 3: Spot light
  // result += CalcSpotLight(spotLight, normal, FragPos, viewDir);

  if (isTextured == 1) {
    // fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    textureColor = texture(textureSampler, texCoords);
    objectColor = vec4(mix(textureColor.xyz, objectColor.xyz, blendFactor), 1.0);
  }
  objectColor *= vec4(result, 1.0);
  // fragColor = textureColor;
  // result *= objectColor.xyz;
  // fragColor = vec4(0.0, 1.0, 0.0, 1.0);
  fragColor = vec4(objectColor.xyz, 1.0);
  // fragColor = vec4(norm, 1.0);
  // fragColor = texture(textureSampler, texCoords);
}

vec3 CalcDirLight(DirectionalLight light, vec3 normal, vec3 viewDir, Material material) {
    vec3 lightDir = normalize(-light.direction);

    // Ensure normal is normalized (redundant if already normalized before calling)
    normal = normalize(normal);

    // Diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);

    // Specular shading using the Blinn-Phong model (better for realism)
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), material.shininess); 

    // Combine results
    vec3 ambient = light.light.ambient * material.ambient;
    vec3 diffuse = light.light.diffuse * diff * material.diffuse;
    vec3 specular = light.light.specular * spec * material.specular;

    return ambient + diffuse + specular;
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