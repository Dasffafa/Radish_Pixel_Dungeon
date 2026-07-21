#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uAlpha;
uniform float uScaleX;
uniform float uScaleY;
uniform vec4 uBounds;

void main() {
  vec4 col = texture2D(uTex, vUV);
  vec2 ratioPos = (gl_FragCoord.xy - uBounds.xy) / uBounds.zw;
  
  if (0.5 - abs(0.5 - ratioPos.x) < uAlpha) {
    col.a = 0.0;
  } else if (0.5 - abs(0.5 - ratioPos.y) < uScaleY / 2.1) {
    col.a = 0.0;
  }
  
  float factor = pow(uScaleY, 7.0);
  col.r += factor * 0.6;
  col.g += factor * 0.7;
  col.b += factor * 0.9;
  
  gl_FragColor = col;
}