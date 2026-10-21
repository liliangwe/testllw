/**
 * S6 · 呼吸留白（帧 1681–1800，4s）
 *
 * 节奏缓冲，无新增叙事信息。低饱和雾面渐变，微尘粒子持续漂浮；节奏曲线末端回落。
 */
import React from "react";
import { interpolate, useCurrentFrame, useVideoConfig } from "remotion";
import { PALETTE } from "../config";
import { Sky } from "../components/Background/Sky";
import { Forest } from "../components/Background/Forest";
import { Mist } from "../components/Background/Mist";
import { Particles } from "../components/Foreground/Particles";

export const Scene6_Breath: React.FC = () => {
  const frame = useCurrentFrame();
  const { durationInFrames } = useVideoConfig();

  // 雾面浓度缓慢呼吸（低饱和）
  const breathe = 0.5 + Math.sin(frame * 0.03) * 0.1;

  // 轻柔淡入
  const fadeIn = interpolate(frame, [0, 24], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <div
      style={{
        position: "absolute",
        inset: 0,
        opacity: fadeIn,
        background: `linear-gradient(180deg, #2E4A62 0%, #3E6B7A 55%, #E8D8BC 100%)`,
      }}
    >
      <Sky mood="mist" />
      <Forest baseY={1080} far="#23465C" near="#142C3A" opacity={0.55} />
      <Mist opacity={breathe} />
      <Particles count={26} color="#FFF3DC" drift={8} opacity={0.4} />

      {/* 极淡的点题留白（无新增叙事） */}
      <div
        style={{
          position: "absolute",
          inset: 0,
          display: "flex",
          justifyContent: "center",
          alignItems: "flex-end",
          paddingBottom: 120,
          color: PALETTE.featherHighlight,
          fontSize: 28,
          letterSpacing: "0.4em",
          opacity: 0.35,
        }}
      >
        余 韵 留 白
      </div>
    </div>
  );
};