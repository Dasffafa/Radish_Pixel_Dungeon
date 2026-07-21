#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uAlpha;
uniform vec2 uDirection;
uniform vec4 uBounds;

void main() {
  vec4 col = texture2D(uTex, vUV);
  vec2 ratioPos = (gl_FragCoord.xy - uBounds.xy) / uBounds.zw;
  
  ratioPos = ratioPos * uDirection + max(vec2(0.0, 0.0), uDirection * -1.0);
  
  if (ratioPos.x < uAlpha && ratioPos.y < uAlpha) {
    col.a = 0.0;
  }
  
  gl_FragColor = col;
}