#ifdef GL_ES
  precision highp float;
#endif

// 顶点是世界坐标（怪物精灵矩形），由相机矩阵变换。
uniform mat4 uCamera;
attribute vec4 aXYZW;
attribute vec2 aUV;
varying vec2 vUV;

void main() {
  gl_Position = uCamera * aXYZW;
  vUV = aUV;
}
