#version 330 core

layout(location = 0) in vec3 iPosition;
layout(location = 1) in vec2 iTextCoord;
layout(location = 2) in vec3 iNormal;
layout(location = 3) in int iMaterial;

uniform mat4 m_model;
uniform mat4 m_view;
uniform mat4 m_projection;
uniform int UUID;

flat out int v_UUID; // Pass UUID to the fragment shader

void main() {
  v_UUID = UUID; // Assign UUID to pass-through variable
  gl_Position = m_projection * m_view * m_model * vec4(iPosition, 1.0);
}
