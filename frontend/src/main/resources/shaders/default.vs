#version 330 core

layout(location = 0) in vec3 iPosition;
layout(location = 1) in vec2 iTextCoord;
layout(location = 2) in vec3 iNormal;

uniform mat4 m_model;
uniform mat4 m_view;
uniform mat4 m_projection;

out vec2 texCoords;
out vec3 normal;

void main() {
  gl_Position = m_projection * m_view * m_model * vec4(iPosition, 1.0);
  texCoords = iTextCoord;
  normal = iNormal;
}
