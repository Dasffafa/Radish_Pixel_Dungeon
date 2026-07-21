#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uTime;
uniform float uAlpha;
uniform vec4 uCutLine;
uniform float uCutAlpha;
uniform int uCutSide;
uniform int uDisappear;

float DistToLine(vec2 a, vec2 b, vec2 p) {
  vec2 v = a, w = b;
  float l2 = pow(distance(w, v), 2.0);
  if (l2 == 0.0) return distance(p, v);
  float t = clamp(dot(p - v, w - v) / l2, 0.0, 1.0);
  vec2 j = v + t * (w - v);
  return distance(p, j);
}

void main() {
  vec4 col = texture2D(uTex, vUV);
  
  vec2 lineStart = uCutLine.xy;
  vec2 lineEnd = uCutLine.xy + (uCutLine.zw * uCutAlpha);
  vec2 point = gl_FragCoord.xy;
  float dst = DistToLine(lineStart, lineEnd, point);
  
  float tmp = dst - (1.0 + float(uDisappear));
  if (tmp < 0.0) {
    col.a = 0.0;
  } else if (tmp < 1.0) {
    col.rgb = vec3(0.7);
  } else if (tmp < 5.0) {
    col.r += (5.0 - tmp) * 0.1;
    col.g += (5.0 - tmp) * 0.01;
  }
  
  vec2 v0 = gl_FragCoord.xy - uCutLine.xy;
  vec2 v1 = uCutLine.zw;
  float dot = v0.x * v1.y - v0.y * v1.x;
  col.a = (dot * float(uCutSide)) <= 0.0 ? col.a : 0.0;
  
  col.a *= uAlpha;
  
  gl_FragColor = col;
}