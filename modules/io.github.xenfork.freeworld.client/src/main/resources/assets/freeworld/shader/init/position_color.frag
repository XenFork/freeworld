#version 330

in vec4 vertexColor;

out vec4 FragColor;

uniform vec4 ColorModulator;

void main() {
    FragColor = ColorModulator * vertexColor;
}
