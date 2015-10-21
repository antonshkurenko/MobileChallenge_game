precision mediump float;

uniform sampler2D u_TextureUnit;
uniform vec4 u_Color;

varying vec2 v_TextureCoordinates;

void main() {

  vec4 color = texture2D(u_TextureUnit, v_TextureCoordinates);
  vec4 resultColor = u_Color * color;
  gl_FragColor = vec4((1.0 - (color.a-resultColor.r)), // god damn glsl, spent 2 hours and found
   (1.0 - (color.a-resultColor.g)), (1.0 - (color.a-resultColor.b)), 1.0); // 1 -> 1.0
}