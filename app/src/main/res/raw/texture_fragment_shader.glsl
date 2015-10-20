precision mediump float;

uniform sampler2D u_TextureUnit;
varying vec2 v_TextureCoordinates;

void main() {

  gl_FragColor = vec4(vec3(1.0, 0.0, 1.0), 1.0) *
          texture2D(u_TextureUnit, v_TextureCoordinates);
}