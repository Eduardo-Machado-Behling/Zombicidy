#version 330 core

flat in uint v_UUID; // The ID of the object
layout(location = 0) out uint
    fragColor; // Must specify "layout(location = 0)" for integer textures

void main() {
  fragColor = v_UUID; // Write the object ID
}
