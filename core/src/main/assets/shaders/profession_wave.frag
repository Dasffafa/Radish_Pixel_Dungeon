#ifdef GL_ES
  precision mediump float;
#endif

uniform sampler2D uTex;
uniform float uTime;
varying vec2 vUV;

void main() {
  vec2 uv = vUV;
  uv.x += sin(uv.y * 40.0 + uTime * 3.5) * 0.0025;
  uv.y += cos(uv.x * 40.0 + uTime * 2.8) * 0.0025;

  vec4 tex = texture2D(uTex, uv);
  if (tex.a <= 0.0) {
    gl_FragColor = vec4(0.0);
    return;
  }

  float lum = dot(tex.rgb, vec3(0.299, 0.587, 0.114));
  float wave = 0.5 + 0.5 * sin(uv.y * 24.0 - uTime * 3.0 + uv.x * 12.0);
  vec3 darkPurple = vec3(0.14, 0.05, 0.24);
  vec3 brightPurple = vec3(0.45, 0.16, 0.68);
  vec3 tint = mix(darkPurple, brightPurple, wave * 0.65 + lum * 0.35);

  vec3 finalRgb = mix(tex.rgb, tint * (0.55 + 0.65 * lum), 0.72);
  gl_FragColor = vec4(finalRgb, tex.a);
}
