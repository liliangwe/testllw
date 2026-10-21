/**
 * Bird.tsx · 山鸟主体（身体 + 双翼，可循环扇动 / 滑翔姿态）
 *
 * 面向右方（beak 朝 +x），以 (x, y) 定位鸟体中心，
 * 通过 facing 镜像到 -1 表示朝左飞。羽翼扇动频率默认取 MOTION.wing.flapHzDefault。
 */
import React from "react";
import { useCurrentFrame, useVideoConfig } from "remotion";
import { Wing } from "./Wing";
import { MOTION } from "../../config";

export type FlapMode = "flap" | "glide";

export interface BirdProps {
  x: number;
  y: number;
  scale?: number;
  /** 1 = 朝右，-1 = 朝左 */
  facing?: 1 | -1;
  flapHz?: number;
  /** 扇动相位偏移（秒），用于多只鸟错开 */
  phaseSec?: number;
  mode?: FlapMode;
  /** 鸟体倾斜角度（弧度） */
  tiltRad?: number;
  bodyColor?: string;
  wingColor?: string;
  rimColor?: string;
  opacity?: number;
}

export const Bird: React.FC<BirdProps> = ({
  x,
  y,
  scale = 1,
  facing = 1,
  flapHz,
  phaseSec = 0,
  mode = "flap",
  tiltRad = 0,
  bodyColor = "#1E2A2E",
  wingColor = "#2E4A62",
  rimColor = "#F4EBDD",
  opacity = 1,
}) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const hz = flapHz ?? MOTION.wing.flapHzDefault;
  const t = frame / fps + phaseSec;
  const phase = Math.sin(2 * Math.PI * hz * t);
  const angleDeg =
    mode === "glide" ? 12 + phase * 6 : 14 + phase * 40;

  const bodyLift = mode === "glide" ? Math.sin(2 * Math.PI * 0.5 * t) * 3 : 0;

  return (
    <div
      style={{
        position: "absolute",
        left: x,
        top: y + bodyLift * scale,
        transform: `translate(-50%,-50%) scale(${scale}) scaleX(${facing}) rotate(${tiltRad}rad)`,
        transformOrigin: "center",
        opacity,
      }}
    >
      <svg
        width={220}
        height={150}
        viewBox="0 0 220 150"
        style={{ overflow: "visible", display: "block" }}
      >
        {/* 远侧翅（身体之后） */}
        <g transform="translate(80,52) rotate(-18) ">
          <g transform={`rotate(${-angleDeg * 0.85} 8 45) translate(-8,-45)`}>
            <Wing fill="#26353F" rim="rgba(30,42,46,.25)" darker opacity={0.92} />
          </g>
        </g>

        {/* 尾羽 */}
        <path
          d="M34,72 L-26,26 L-16,84 Z"
          fill={wingColor}
          opacity={0.9}
        />
        <path d="M30,70 L-22,30 L-14,80 Z" fill={bodyColor} opacity={0.55} />

        {/* 身体 */}
        <ellipse cx={82} cy={74} rx={74} ry={35} fill={bodyColor} />
        <ellipse
          cx={82}
          cy={74}
          rx={74}
          ry={35}
          fill="none"
          stroke="rgba(255,255,255,.18)"
          strokeWidth={2}
        />
        {/* 腹部高光 */}
        <ellipse
          cx={86}
          cy={86}
          rx={52}
          ry={20}
          fill={rimColor}
          opacity={0.18}
        />

        {/* 头部 */}
        <circle cx={168} cy={52} r={27} fill={bodyColor} />
        <circle
          cx={168}
          cy={52}
          r={27}
          fill="none"
          stroke="rgba(255,255,255,.18)"
          strokeWidth={1.8}
        />
        {/* 鸟喙 */}
        <path d="M191,46 L226,56 L191,67 Z" fill={wingColor} />
        {/* 眼睛 */}
        <circle cx={179} cy={46} r={3.6} fill={rimColor} />
        <circle cx={180} cy={45} r={1.4} fill="#1E2A2E" />

        {/* 逆光轮廓（rim light） */}
        <path
          d="M170,25 C126,20 66,22 26,38"
          stroke={rimColor}
          strokeWidth="2.2"
          strokeLinecap="round"
          fill="none"
          opacity={0.55}
        />

        {/* 近侧翅（身体之前，主翼） */}
        <g transform={`translate(64,66) rotate(8)`}>
          <g transform={`rotate(${angleDeg} 8 45) translate(-8,-45)`}>
            <Wing fill={bodyColor} rim="rgba(30,42,46,.16)" />
          </g>
        </g>
      </svg>
    </div>
  );
};