precision mediump float;

uniform sampler2D u_TextureUnit;
uniform vec4 u_Color;

varying vec2 v_TextureCoordinates;

void main() {

  vec4 color = texture2D(u_TextureUnit, v_TextureCoordinates);
  vec4 resultColor = u_Color * color;
  vec4 blackColor = vec4(0.1, 0.1, 0.1, 1.0); // almost black for fix mipmap blending noise
  float alpha;
  if(resultColor.r <= blackColor.r &&
   resultColor.g <= blackColor.g &&
   resultColor.b <= blackColor.b) {
    alpha = 1.0;
  } else {
    alpha = 0.6;
  }
  gl_FragColor = vec4((1.0 - (color.a-resultColor.r)), // god damn glsl, spent 2 hours and found
   (1.0 - (color.a-resultColor.g)), (1.0 - (color.a-resultColor.b)), alpha); // 1 -> 1.0
}