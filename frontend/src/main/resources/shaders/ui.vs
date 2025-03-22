#version 330 core

layout(location = 0) in vec3 iPosition;
layout(location = 1) in vec2 iTextCoord;

out vec2 texCoords;

uniform mat4 m_projection;

void main() {
  gl_Position = m_projection * vec4(iPosition, 1.0);
  texCoords = iTextCoord;
}
