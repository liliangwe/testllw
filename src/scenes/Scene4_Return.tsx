/**
 * S4 · 群鸟归林（帧 1141–1500，12s）
 *
 * 多鸟结伴归林、掠过树冠，暖金夕照光晕；光斑逐渐收束（全局记忆点）。
 * 转场：汇聚 → 归巢（众鸟由分散汇聚至树冠方向）。
 */
import React from "react";
import { interpolate, useCurrentFrame, useVideoConfig } from "remotion";
import { PALETTE, MOTION } from "../config";
import { Sky } from "../components/Background/Sky";
import { Forest } from "../components/Background/Forest";
import { Bird } from "../components/Bird/Bird";
import { Bokeh } from "../components/Foreground/Bokeh";
import { Particles } from "../components/Foreground/Particles";

export const Scene4_Return: React.FC = () => {
  const frame = useCurrentFrame();
  const { width, height, durationInFrames } = useVideoConfig();
  const p = frame / durationInFrames;

  // 夕照渐变天色（前段更暖 → 后段微降）
  const warmth = interpolate(p, [0, 0.7], [1.0, 0.82], {
    easing: MOTION.easing.easeInOutQuad,
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // 光斑收束：随归巢进度减弱
  const bokehAlpha = interpolate(p, [0.1, 0.95], [0.55, 0.08], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // 头鸟（领飞）
  const leadX = interpolate(p, [0, 1], [width * 0.2, width * 0.6]);
  const leadY = interpolate(p, [0, 1], [height * 0.5, height * 0.78]);
  const leadTilt = -0.1 + p * 0.16;

  // 群鸟成员（从分散汇聚到树冠收拢）
  const FLOCK = [
    { dx: 90, dy: -140, ph: 0.2, s: 0.62, f: -1 },
    { dx: -70, dy: -120, ph: 0.5, s: 0.56, f: 1 },
    { dx: 150, dy: -60, ph: 0.8, s: 0.48, f: -1 },
    { dx: -130, dy: -40, ph: 1.1, s: 0.48, f: 1 },
    { dx: 60, dy: 60, ph: 1.4, s: 0.42, f: -1 },
    { dx: -40, dy: 90, ph: 1.7, s: 0.4, f: 1 },
  ];

  return (
    <div style={{ position: "absolute", inset: 0, background: "#B4785A" }}>
      <Sky mood="dusk" bottom={`rgba(232,168,92,${warmth})`} />

      {/* 夕阳光晕 */}
      <div
        style={{
          position: "absolute",
          top: height * 0.16,
          left: width * 0.68,
          width: 380,
          height: 380,
          transform: "translate(-50%,-50%)",
          background: `radial-gradient(circle, rgba(255,220,150,${0.9 * warmth}) 0%, rgba(232,168,92,0.28) 42%, transparent 70%)`,
          mixBlendMode: "screen",
        }}
      />

      <Forest baseY={height} far="#7A4E34" near="#2E1E14" opacity={0.92} />

      {/* 群鸟汇聚 */}
      <Bird x={leadX} y={leadY} scale={0.72} facing={1} mode="flap" flapHz={5.2} tiltRad={leadTilt}
        bodyColor="#2E1E14" wingColor="#5A3A22" rimColor="#F4EBDD" />
      {FLOCK.map((b, i) => {
        // 由分散位置向头鸟后方收拢
        const fx = interpolate(p, [0, 1], [width * 0.2 + b.dx, leadX - 70 + b.dx * 0.4]);
        const fy = interpolate(p, [0, 1], [height * 0.5 + b.dy, leadY + b.dy * 0.4]);
        return (
          <Bird key={i} x={fx} y={fy} scale={b.s} facing={b.f === 1 ? 1 : -1} mode="flap"
            flapHz={5.5} phaseSec={b.ph} tiltRad={-0.06}
            bodyColor="#3A2A1C" wingColor="#6B4630" rimColor="#F4EBDD" />
        );
      })}

      <Bokeh speed={30} accent="#FFE9C4" maxAlpha={bokehAlpha} />
      <Particles count={30} color="#FFE9C4" drift={12} opacity={0.45} />
    </div>
  );
};