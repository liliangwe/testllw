/**
 * S2 · 穿枝过叶（帧 361–780，14s）
 *
 * 跟拍横移：鸟在林间穿梭，Bokeh 光斑扫过，枝叶虚化前景视差，微尘粒子随镜头运动。
 * 转场：速度线衔接（横向掠过的告示式光斑，营造提速）。
 */
import React from "react";
import { interpolate, useCurrentFrame, useVideoConfig } from "remotion";
import { PALETTE } from "../config";
import { Sky } from "../components/Background/Sky";
import { Forest } from "../components/Background/Forest";
import { Bird } from "../components/Bird/Bird";
import { LeafLayer } from "../components/Foreground/LeafLayer";
import { Bokeh } from "../components/Foreground/Bokeh";
import { Particles } from "../components/Foreground/Particles";
import { CameraRig } from "../components/FX/CameraRig";

export const Scene2_Weave: React.FC = () => {
  const frame = useCurrentFrame();
  const { width, height, durationInFrames } = useVideoConfig();

  const p = frame / durationInFrames; // 0 → 1

  // 鸟：从左下向右上穿梭，正弦起伏 + 轻微纵深（缩放）
  const birdX = interpolate(p, [0, 1], [width * 0.12, width * 0.82], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const birdY = height * 0.42 - Math.sin(p * Math.PI * 2.6) * height * 0.1;
  const birdScale = interpolate(Math.sin(p * Math.PI), [0, 1], [0.82, 1.06]);
  const birdTilt = -0.06 + Math.sin(p * Math.PI * 2.6) * 0.06;

  // 跟拍横移：环境向反方向流动，营造速度线（限制最大平移避免穿帮）
  const pan = interpolate(p, [0, 1], [0, -260], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <div style={{ position: "absolute", inset: 0, background: "#3E6B54" }}>
      <CameraRig x={pan}>
        <Sky mood="forest" />
        <Forest baseY={height} far="#3B6B54" near="#1E3A2E" opacity={0.9} />
        {/* 远处点缀小鸟（增添林间层次） */}
        <Bird x={width * 0.28} y={height * 0.32} scale={0.34} facing={-1} flapHz={6} phaseSec={0.4} opacity={0.7} />
        <Bird x={width * 0.62} y={height * 0.26} scale={0.26} facing={1} flapHz={5.5} phaseSec={1.1} opacity={0.6} />
      </CameraRig>

      {/* 主体鸟（跟拍目标，保持画面内运动） */}
      <Bird
        x={birdX}
        y={birdY}
        scale={birdScale}
        facing={1}
        flapHz={5}
        mode="flap"
        tiltRad={birdTilt}
      />

      {/* 前景视差：枝叶 + 光斑 + 粒子 */}
      <LeafLayer speed={140} blurPx={10} color="#12241C" opacity={0.6} />
      <Bokeh speed={90} accent="#FFE9C4" maxAlpha={0.5} />
      <Particles count={40} color="#FFF3DC" drift={60} opacity={0.75} />

      {/* 速度线提示（开场 60 帧 & 尾声 40 帧的告示式流光） */}
      <SpeedStreaks fadeIn={frame < 60 ? frame / 60 : 1} />
    </div>
  );
};

/** 速度线：横贯画面的水平流光 */
const SpeedStreaks: React.FC<{ fadeIn: number }> = ({ fadeIn }) => {
  const frame = useCurrentFrame();
  return (
    <div style={{ position: "absolute", inset: 0, opacity: fadeIn * 0.5, overflow: "hidden", mixBlendMode: "screen" }}>
      {[0, 1, 2, 3].map((i) => {
        const y = 0.2 + i * 0.18;
        const off = (frame * (120 + i * 40)) % 2600;
        return (
          <div
            key={i}
            style={{
              position: "absolute",
              top: `${y * 100}%`,
              left: -off,
              width: 900,
              height: 3,
              transform: "skewX(-18deg)",
              background: `linear-gradient(90deg, transparent, rgba(255,255,255,.9), transparent)`,
              opacity: 0.5 + i * 0.1,
            }}
          />
        );
      })}
    </div>
  );
};