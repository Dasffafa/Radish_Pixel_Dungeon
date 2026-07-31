#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uTime;
uniform float uAlpha;
uniform vec4 uCutLine;  // xy=start(归一化), zw=direction(归一化)
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
  
  // 用归一化纹理坐标做切割计算
  vec2 lineStart = uCutLine.xy;
  vec2 lineEnd = uCutLine.xy + (uCutLine.zw * uCutAlpha);
  vec2 point = vUV;
  float dst = DistToLine(lineStart, lineEnd, point);
  
  // 归一化坐标下的距离阈值（贴图尺寸的百分比）
  float threshold = 0.02 + float(uDisappear) * 0.01;
  float tmp = dst - threshold;
  if (tmp < 0.0) {
    col.a = 0.0;
  } else if (tmp < 0.05) {
    col.rgb = vec3(0.7);
  } else if (tmp < 0.15) {
    col.r += (0.15 - tmp) * 2.0;
    col.g += (0.15 - tmp) * 0.2;
  }
  
  // 判断在切割线的哪一侧
  vec2 v0 = vUV - uCutLine.xy;
  vec2 v1 = uCutLine.zw;
  float cross = v0.x * v1.y - v0.y * v1.x;
  col.a = (cross * float(uCutSide)) <= 0.0 ? col.a : 0.0;
  
  col.a *= uAlpha;
  
  gl_FragColor = col;
}