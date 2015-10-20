precision mediump float;

uniform sampler2D u_TextureUnit;
varying vec2 v_TextureCoordinates;

void main() {

  vec4 color = texture2D(u_TextureUnit, v_TextureCoordinates);
  vec4 customColor = vec4(vec3(1.0, 0.0, 0.0), 1.0);
  vec4 resultColor = customColor * color;
  gl_FragColor = vec4((1.0 - (color.a-resultColor.r)), // god damn glsl, spent 2 hours and found
   (1.0 - (color.a-resultColor.g)), (1.0 - (color.a-resultColor.b)), 1.0); // 1 -> 1.0
}