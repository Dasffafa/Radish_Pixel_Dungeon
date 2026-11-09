#ifdef GL_ES
  precision highp float;
#endif

/*
 * 斩舰刀击杀：怪物被「次元斩」从左上到右下斜着切成两半，上半片沿刀口滑走。
 *
 * 只作用于这一只怪的精灵矩形（怪物图层），地图 / 场景 / HUD 一概不动：
 *   1. 刀口是过精灵中心的斜线，方向由 Java 端固定给出（左上 → 右下，绝不做镜像）；
 *   2. 刀口上侧那半片像刚体一样沿刀口滑出去（视觉朝 -dir），它让出的地方透明，
 *      露出后面的地板 —— 这是精灵图层被移走，不是地图被挖空；
 *   3. 刀口带热白细芯 + 橙金外晕（次元斩刀光），随时间收窄。
 *
 * 输出为预乘 alpha，调用方必须用 (GL_ONE, GL_ONE_MINUS_SRC_ALPHA) 混合。
 * uFrame 由基类提供（图集中的帧范围），uSize 是精灵像素尺寸。
 */

varying vec2 vUV;            // 精灵局部 uv：0..1，y 向下
uniform sampler2D uTex;
uniform vec4  uFrame;        // 帧在图集中的 uv 范围
uniform vec2  uSize;         // 精灵像素尺寸
uniform float uPad;          // quad 相对精灵四边外扩的像素（滑出去的部分才不会被裁掉）
uniform vec4  uLine;         // xy = 刀口上一点（精灵局部像素），zw = 方向（左上→右下）
uniform float uSlide;        // 上半片滑出距离（像素，视觉方向 -dir）
uniform float uFall;         // 上半片自由落体位移（像素，向下）
uniform float uSilhouette;   // 1 = 怪物画成纯黑剪影
uniform float uFade;         // 整体淡出
uniform float uCrack;        // 刀光显现进度 0..1
uniform float uGlow;         // 刀光强度
uniform float uSeed;         // 每次击杀的抖动种子
uniform float uGlowOnly;     // 1 = 只画刀光（第二遍绘制用的刀光层）

float hash11(float n) {
  return fract(sin(n * 12.9898 + uSeed * 78.233) * 43758.5453);
}

vec4 grab(vec2 luv) {
  if (luv.x < 0.0 || luv.x > 1.0 || luv.y < 0.0 || luv.y > 1.0) return vec4(0.0);
  return texture2D(uTex, uFrame.xy + luv * uFrame.zw);
}

void main() {
  // quad 比精灵大一圈：p 的原点对齐精灵左上角，所以滑出去的那半片不会被 quad 边界裁掉
  vec2 quad = uSize + vec2(uPad * 2.0);
  vec2 p   = vUV * quad - vec2(uPad);
  vec2 c   = uLine.xy;
  vec2 dir = normalize(uLine.zw);
  vec2 nrm = vec2(-dir.y, dir.x);

  float a = dot(p - c, dir);
  float b = dot(p - c, nrm);        // b < 0 = 刀口上侧那半片

  // 刀口不是完美直线：沿刀口做亚像素抖动
  float jag = (hash11(floor(a * 1.6)) - 0.5) * 1.2 * uCrack;
  float bb = b + jag;

  bool pieceHere = bb <= 0.0;

  // 刚体平移：视觉朝 -dir ⇒ 采样点朝 +dir 走
  vec2 src = p - dir * uSlide - vec2(0.0, uFall);
  bool pieceSrc = dot(src - c, nrm) + jag <= 0.0;

  float ad = abs(bb);
  float core = 1.0 - smoothstep(0.0, 0.9, ad);
  float halo = 1.0 - smoothstep(0.0, 4.0, ad);
  halo = halo * halo;
  vec3 glowRGB = vec3(1.0, 0.32, 0.26) * core * 1.6
               + vec3(0.78, 0.03, 0.03) * halo * 1.1;

  // 刀光层：不采样贴图，只画这道血红刀光
  if (uGlowOnly > 0.5) {
    float halfLen = max(uSize.x * 0.5, 1.0);
    float an = clamp(a / halfLen, -1.0, 1.0);
    float dd = abs(b);                                    // 笔直：不做弧

    float core  = 1.0 - smoothstep(0.0, 0.75, dd);         // 白热刀身
    float bloom = 1.0 - smoothstep(0.0, 4.5, dd);
    bloom = bloom * bloom;                                 // 血红光晕

    float taper = 1.0 - smoothstep(0.45, 1.0, abs(an));    // 两端收成锋尖

    // 炸裂：沿刀光随机布满横向短刺
    float seg   = floor((an * 0.5 + 0.5) * 11.0);
    float spike = step(0.55, hash11(seg)) * (1.0 - smoothstep(1.6, 3.6, dd));

    // 中心爆闪
    float flash = 1.0 - smoothstep(0.0, 0.22, abs(an));

    vec3 g = vec3(1.00, 0.14, 0.09) * core * 2.2
           + vec3(0.62, 0.02, 0.02) * bloom * 1.15
           + vec3(1.00, 0.45, 0.25) * spike * 0.9
           + vec3(1.00, 0.88, 0.78) * flash * core * 1.2;
    g *= uGlow * taper;                                    // 强度 + 收尖

    float ga = clamp(max(g.r, max(g.g, g.b)), 0.0, 1.0);
    if (ga < 0.004) {
      discard;
    }
    gl_FragColor = vec4(g, ga);
    return;
  }

  vec4 col;
  if (pieceSrc) {
    col = grab(src / uSize);      // 上半片本体
  } else if (pieceHere) {
    col = vec4(0.0);              // 它已经滑走，露出后面的地板
  } else {
    col = grab(p / uSize);        // 下半片原地不动
  }

  vec3 body = mix(col.rgb, vec3(0.0), uSilhouette);   // 剪影：只留轮廓，颜色抹掉
  vec3 rgb = body * col.a * uFade + glowRGB * uGlow;
  float alpha = col.a * uFade;

  // 被切开的细缝（次元斩的"切口"）
  float cut = (1.0 - smoothstep(0.0, 0.7, ad)) * uCrack * uFade;
  rgb *= (1.0 - cut);
  alpha *= (1.0 - cut);

  if (alpha < 0.004) {
    discard;
  }

  gl_FragColor = vec4(rgb, min(alpha, 1.0));
}
