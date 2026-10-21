/**
 * config.ts · 单一真相源（Single Source of Truth）
 *
 * 依据 brief.md / workbench.html 收敛：
 *   - 画幅与帧率
 *   - 场景时长与帧区间（30fps）
 *   - 品牌/氛围色板
 *   - 字体基准
 *   - 动效节拍（缓动、羽翼扇动、光斑、粒子）
 *   - 资源路径
 *
 * 所有场景（src/scenes/*.tsx）与组件（src/components/*）必须引用本文件，
 * 禁止在场景内硬编码上述常量。
 */
import { Easing } from "remotion";

/* ─────────────────────────── 画幅 · 帧率 ─────────────────────────── */
export const VIDEO = {
  width: 1920,
  height: 1080,
  fps: 30,
  totalDurationInSeconds: 60,
} as const;

export const TOTAL_FRAMES = VIDEO.fps * VIDEO.totalDurationInSeconds; // 1800

/* ─── 场景时长（秒）与帧区间（30fps）─── */
export const SCENE_SECONDS = {
  S0_HOOK: 4,
  S1_TAKEOFF: 8,
  S2_WEAVE: 14,
  S3_GLIDE: 12,
  S4_RETURN: 12,
  S5_OUTRO: 6,
  S6_BREATH: 4, // 呼吸留白（压尾）
} as const;

// 帧区间：首尾相接、跨度=秒数×30（对照 verified_data C4 校验）
export const SCENE_FRAMES = {
  S0_HOOK: { start: 1, end: 120 },
  S1_TAKEOFF: { start: 121, end: 360 },
  S2_WEAVE: { start: 361, end: 780 },
  S3_GLIDE: { start: 781, end: 1140 },
  S4_RETURN: { start: 1141, end: 1500 },
  S5_OUTRO: { start: 1501, end: 1680 },
  S6_BREATH: { start: 1681, end: 1800 },
} as const;

/* 节奏标定：慢（0–12s）→ 中快（12–38s）→ 慢（38–60s） */
export const PACE = {
  slowEndSec: 12,
  fastEndSec: 38,
} as const;

/* 安全边距（标题安全框，≥96px） */
export const SAFE_MARGIN = 96;

/* ─────────── 叙事文案（片名 / 金句）─────────── */
export const STORY = {
  title: "山鸟在林间飞翔",
  // 金句文案：brief D5 待用户定稿；当前为拟稿占位，可替换
  quote: "穿过林间光影，重拾轻盈与自由。",
} as const;

/* ─────────────────────────── 色板（品牌/氛围统一）─────────────────── */
export const PALETTE = {
  mistBlue: "#2E4A62", // 晨雾冷青 · S0/S1 主调
  forestGreen: "#3E6B4F", // 林间绿 · S1/S2 环境
  gold: "#E8A85C", // 晨曦暖金 · S0 光束 / S4 夕阳
  featherHighlight: "#F4EBDD", // 羽色高亮 / 逆光轮廓
  ink: "#1E2A2E", // 文案墨色
  paper: "#F7EFE6", // 落版暖白 · S5 背景
  skyBlue: "#4F7D97", // 高空蓝 · S3 主调（workbench 衍生）
  canopyGreen: "#5A8A63", // 树冠绿（workbench 衍生）
} as const;

/* ─────────────────────────── 字体 ─────────────────────────── */
export const TYPO = {
  // 标题/金句：思源宋体（衬线）
  serifFamily:
    '"Noto Serif SC", "Source Han Serif SC", "STSong", "SimSun", Georgia, serif',
  // 正文/英文：思源黑体（无衬线）
  sansFamily:
    '"Noto Sans SC", "Source Han Sans SC", "PingFang SC", "Microsoft YaHei", -apple-system, sans-serif',
  title: 72,
  subtitle: 56,
  caption: 28,
} as const;

/* ─────────────────────────── 动效节拍 ─────────────────────────── */
export const MOTION = {
  // 缓动函数（Remotion Easing）
  easing: {
    easeInOutCubic: Easing.inOut(Easing.cubic),
    easeInOutQuad: Easing.inOut(Easing.quad),
    easeOutCubic: Easing.out(Easing.cubic),
    easeInCubic: Easing.in(Easing.cubic),
    // 贝塞尔平滑飞行轨迹
    flight: Easing.bezier(0.42, 0, 0.58, 1),
  },
  // 羽翼扇动 4–6 Hz（设计参考值，见 verified_data E1：不作事实性宣称）
  wing: {
    flapHzLow: 4,
    flapHzHigh: 6,
    flapHzDefault: 5,
    wingSpan: 0.92, // 滑翔翼展比例
  },
  // 光斑（Bokeh）记忆点
  bokeh: {
    layerCount: 3,
    saturation: 1.25,
    blurPx: 12,
  },
  // 粒子（微尘/花粉）
  particles: {
    count: 36,
  },
} as const;

/* ─────────────────────────── 资源路径 ─────────────────────────── */
export const ASSETS = {
  data: {
    // 数据类场景引用入口（当前为氛围片，无数据场景；校验结论 has_benchmark=false）
    verifiedDataFile: "src/data/verified_data.json",
    hasBenchmarkOrComparisonTable: false,
  },
  fonts: {
    // 字体资源：中文裁剪后回填 woff/woff2（brief D7）
    notoSerifSC: "public/fonts/NotoSerifSC-SemiBold.woff2",
    notoSansSC: "public/fonts/NotoSansSC-Regular.woff2",
  },
  audio: {
    // 声音指引层，交付音效环节
    soundGuide: "src/audio/sound_guide.md",
  },
} as const;

/* ─────────────────────────── 对外导出汇总 ─────────────────────────── */
export default {
  VIDEO,
  TOTAL_FRAMES,
  SCENE_SECONDS,
  SCENE_FRAMES,
  PACE,
  PALETTE,
  TYPO,
  MOTION,
  ASSETS,
};
