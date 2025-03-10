#version 330 core

layout(location = 0) in vec2 position;
layout(location = 1) in vec2 iTextCoord;

uniform mat4 transform; // Transformation matrix

out vec2 texCoords; // <- Renamed to match fragment shader

void main() {
  gl_Position = transform * vec4(position, 0.0, 1.0);
  texCoords = iTextCoord;
}
