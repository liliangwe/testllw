/**
 * S3 · 滑翔俯瞰（帧 781–1140，12s）
 *
 * 拉升俯瞰：群山林海，鸟呈优雅滑翔姿态；翼展稳定、留白构图。
 * 转场：拉开全景（远景山体放大 → 鸟弧线滑过）。
 */
import React from "react";
import { interpolate, useCurrentFrame, useVideoConfig } from "remotion";
import { PALETTE, MOTION } from "../config";
import { Sky } from "../components/Background/Sky";
import { Forest } from "../components/Background/Forest";
import { Bird } from "../components/Bird/Bird";
import { Bokeh } from "../components/Foreground/Bokeh";
import { Particles } from "../components/Foreground/Particles";

export const Scene3_Glide: React.FC = () => {
  const frame = useCurrentFrame();
  const { width, height, durationInFrames, fps } = useVideoConfig();
  const p = frame / durationInFrames;

  // 飞行轨迹：贝塞尔平滑曲线（起点 → 控制点 → 终点）+ 平滑变速
  const t = MOTION.easing.flight(p);
  // 二次贝塞尔：从右下 (x0,y0) 经 (cx,cy) 到左上 (x1,y1)
  const x0 = width * 0.1;
  const y0 = height * 0.78;
  const cx = width * 0.55;
  const cy = height * 0.1;
  const x1 = width * 0.86;
  const y1 = height * 0.2;
  const bx = (1 - t) * (1 - t) * x0 + 2 * (1 - t) * t * cx + t * t * x1;
  const by = (1 - t) * (1 - t) * y0 + 2 * (1 - t) * t * cy + t * t * y1;

  // 滑翔俯冲角度（沿轨迹切线近似）
  const birdTilt = -0.32 + p * 0.28;

  // 鸟身姿态随上升稳定；裁剪出景前后淡入淡出
  const enter = interpolate(frame, [0, fps * 0.6], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const exit = interpolate(frame, [durationInFrames - fps * 0.8, durationInFrames], [1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <div style={{ position: "absolute", inset: 0, background: PALETTE.skyBlue }}>
      <Sky mood="high" />
      {/* 多组远山林海，营造俯瞰纵深 */}
      <Forest baseY={height * 0.9} far="#7AA3B8" near="#3E6B7A" opacity={0.55} farScale={1.15} />
      <Forest baseY={height * 0.7} far="#A7C4D6" near="#6E9BB0" opacity={0.4} />

      {/* 留白构图：主体滑翔鸟 */}
      <Bird
        x={bx}
        y={by}
        scale={0.9}
        facing={1}
        mode="glide"
        tiltRad={birdTilt}
        bodyColor="#1E2A2E"
        wingColor="#3E6B7A"
        opacity={enter * exit}
      />

      <Bokeh speed={22} accent="#EAF4FA" maxAlpha={0.35} />
      <Particles count={26} color="#EAF4FA" drift={14} opacity={0.5} />
    </div>
  );
};