/**
 * S1 · 晨雾起飞（帧 121–360，8s）
 *
 * 枝头特写：山鸟立于枝头 → 振翅离枝起跳 → 逆光轮廓，羽翼 4–6Hz 循环扇动。
 * 转场：镜头推近 → 起跳（蓄势跃出）；缓动 easeInOutCubic。
 */
import React from "react";
import {
  AbsoluteFill,
  useCurrentFrame,
  useVideoConfig,
  interpolate,
} from "remotion";
import { PALETTE, MOTION } from "../config";
import { Sky } from "../components/Background/Sky";
import { Forest } from "../components/Background/Forest";
import { Mist } from "../components/Background/Mist";
import { Bokeh } from "../components/Foreground/Bokeh";
import { BirdsPerch } from "../components/Bird/BirdsPerch";
import { Feather } from "../components/Bird/Feather";

export const Scene1_Takeoff: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps, durationInFrames } = useVideoConfig();

  // 整体进入淡入（首 15 帧）
  const fadeIn = interpolate(frame, [0, 15], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // 镜头推近：小幅放大聚焦主体
  const zoom = interpolate(frame, [0, durationInFrames * 0.6], [1.0, 1.14], {
    easing: MOTION.easing.easeInOutCubic,
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <AbsoluteFill style={{ backgroundColor: PALETTE.mistBlue }}>
      <div
        style={{
          position: "absolute",
          inset: 0,
          opacity: fadeIn,
          transform: `scale(${zoom})`,
          transformOrigin: "68% 62%",
        }}
      >
        <Sky mood="mist" />
        <Branches />
        <Forest baseY={1080} far="#23465C" near="#1E3A2E" opacity={0.7} />
        <Mist opacity={0.55} />

        {/* 主体：站立 → 起跳的山鸟 */}
        <BirdsPerch fps={fps} />

        <FallingFeathers fps={fps} />
        <Bokeh speed={26} accent="#FFE9C4" maxAlpha={0.4} />
      </div>
    </AbsoluteFill>
  );
};

/** 远近枝桠剪影（起跳落点） */
const Branches: React.FC = () => {
  const frame = useCurrentFrame();
  const sway = Math.sin(frame * 0.02) * 2;
  return (
    <svg
      width="1920"
      height="1080"
      viewBox="0 0 1920 1080"
      style={{ position: "absolute", inset: 0 }}
      fill="none"
    >
      <g transform={`translate(0 ${sway})`}>
        <path d="M1560,120 C1420,300 1180,470 980,560" stroke="#5A6E52" strokeWidth="26" strokeLinecap="round" />
        <path d="M1560,120 C1500,260 1430,410 1320,540" stroke="#4A5E45" strokeWidth="16" strokeLinecap="round" />
        <path d="M1180,470 C1100,540 1020,600 940,640" stroke="#3E4F38" strokeWidth="9" strokeLinecap="round" />
        <path d="M980,560 C900,600 830,620 780,600" stroke="#3E4F38" strokeWidth="7" strokeLinecap="round" />
      </g>
    </svg>
  );
};

/** 飘落羽毛（离枝瞬间细节） */
const FallingFeathers: React.FC<{ fps: number }> = ({ fps }) => {
  const frame = useCurrentFrame();
  return (
    <div style={{ position: "absolute", inset: 0, pointerEvents: "none" }}>
      {[0, 1, 2].map((i) => {
        const start = 30 + i * 26;
        const t = interpolate(frame, [start, start + fps * 4], [0, 1], {
          extrapolateLeft: "clamp",
          extrapolateRight: "clamp",
        });
        const visible = frame >= start ? Math.min(1, (frame - start) / 12) : 0;
        return (
          <div
            key={i}
            style={{
              position: "absolute",
              left: 1080 + i * 220,
              top: 260 + t * 520,
              transform: `rotate(${t * 160}deg)`,
              opacity: (1 - Math.abs(t - 0.5) * 1.4) * visible,
            }}
          >
            <Feather width={140} height={30} />
          </div>
        );
      })}
    </div>
  );
};