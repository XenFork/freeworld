#version 330

in vec3 Position;
in vec4 Color;
in vec2 UV;

out vec4 vertexColor;
out vec2 texCoord;

uniform mat4 ProjectionViewMatrix;
uniform mat4 ModelMatrix;

void main() {
    gl_Position = ProjectionViewMatrix * ModelMatrix * vec4(Position, 1.0);
    vertexColor = Color;
    texCoord = UV;
}
