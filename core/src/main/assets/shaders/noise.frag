#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform sampler2D uNoiseTex;
uniform float uTime;
uniform float uAlpha;
uniform vec4 uNoiseBounds;

// 从噪声纹理采样
float noise(vec2 loc) {
  vec2 bigLoc = mod(loc, uNoiseBounds.ba * 2.0);
  vec2 smallLoc = mod(loc, uNoiseBounds.ba * 1.0);
  vec2 quadrants = bigLoc / uNoiseBounds.ba;
  int index = int(quadrants.x) + int(quadrants.y) * 2;
  vec4 col = texture2D(uNoiseTex, uNoiseBounds.xy + smallLoc.xy / uNoiseBounds.ba);
  return col[index];
}

float noise(vec2 loc, int octaves) {
  float val = 0.5;
  for (int i = 0; i < octaves; i++) {
    float power = pow(2.0, float(i));
    val += (noise(loc * power) - 0.5) / power;
  }
  return val;
}

void main() {
  vec4 col = texture2D(uTex, vUV);
  float n = noise(gl_FragCoord.xy * 0.001 + uTime * 0.04, int(uAlpha * 10.0 + 1.0));
  
  if (n < uAlpha) {
    col.a = 0.0;
  }
  
  gl_FragColor = col;
}