#version 330

in vec4 vertexColor;
in vec2 texCoord;

out vec4 FragColor;

uniform vec4 ColorModulator;
uniform sampler2D Sampler0;

void main() {
    FragColor = ColorModulator * vertexColor * texture(Sampler0, texCoord);
}
