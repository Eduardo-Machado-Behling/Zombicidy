#version 330 core

layout(location = 0) in vec3 iPosition;
layout(location = 1) in vec2 iTextCoord;
layout(location = 2) in vec3 iNormal;
layout(location = 3) in  int iMaterial;

uniform mat4 m_model;
uniform mat4 m_view;
uniform mat4 m_projection;

out vec2 texCoords;
out vec3 normal;
out vec3 fragPos;
flat out int materialIndex;

void main() {
  vec4 tempFragPos = m_view * m_model * vec4(iPosition, 1.0);
  gl_Position = m_projection * m_view * m_model * vec4(iPosition, 1.0);
  texCoords = iTextCoord;
  normal = iNormal;
  fragPos = vec3(tempFragPos);
  materialIndex = iMaterial;
}
