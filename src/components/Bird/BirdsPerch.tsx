/**
 * BirdsPerch.tsx · 枝头山鸟（站立 → 振翅离枝起跳）
 *
 * 生命线（以场景本地帧计）：
 *   0–0.5s  静止站立（微呼吸起伏，双翼收拢）
 *   0.5–1s  蓄势（体态下压，羽翼微张）
 *   1–1.5s  起跳（快速下蹲后向上跃出，羽翼高频扇动）
 *   1.5s–   离枝飞行（略向右上、渐远渐小）
 */
import React from "react";
import { interpolate, useCurrentFrame } from "remotion";
import { MOTION, PALETTE } from "../../config";
import { Wing } from "./Wing";

export interface BirdsPerchProps {
  fps: number;
}

export const BirdsPerch: React.FC<BirdsPerchProps> = ({ fps }) => {
  const frame = useCurrentFrame();
  const t = frame / fps;

  const TAKE_OFF = 1.0;
  const ASCEND = 1.6;

  // 阶段插值
  const crouch = interpolate(frame, [fps * 0.5, fps * TAKE_OFF], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const airborneRaw = interpolate(frame, [fps * TAKE_OFF, fps * ASCEND], [0, 1], {
    easing: MOTION.easing.easeOutCubic,
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const airborne = frame >= fps * TAKE_OFF ? airborneRaw : 0;

  // 起跳后持续扇动（4–6Hz 高频端）
  const flap = interpolate(frame, [fps * TAKE_OFF, fps * TAKE_OFF + 6], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // 位置：蹲伏时下移 10px；起跳后向右上飞离（渐远缩小 + 上移）
  const px = 980 + airborne * 150;
  const py = 560 + crouch * 12 - airborne * 320;
  const scale = 1 - airborne * 0.35;
  const tilt = -0.08 - airborne * 0.22; // 跃起后躯干前倾（右倾）

  const wingAngle =
    flap > 0.02
      ? 10 + Math.sin((frame - fps * TAKE_OFF) * 0.9) * 46 * flap
      : 4 + Math.sin(frame * 0.05) * 2;

  return (
    <div
      style={{
        position: "absolute",
        left: px,
        top: py,
        transform: `translate(-50%,-50%) scale(${scale}) rotate(${tilt}rad)`,
        transformOrigin: "center",
      }}
    >
      <svg width={280} height={200} viewBox="0 0 280 200" style={{ overflow: "visible" }}>
        {/* 尾羽 */}
        <path d="M70,112 L22,66 L38,142 Z" fill={PALETTE.mistBlue} opacity={0.9} />
        <path d="M68,110 L28,74 L40,136 Z" fill="#1E2A2E" opacity={0.55} />

        {/* 腿爪（站立，抓住枝干） */}
        {airborne < 0.6 && (
          <g opacity={1 - airborne * 0.4}>
            <path d="M118,142 L116,172 M132,142 L136,170" stroke="#1E2A2E" strokeWidth="4" strokeLinecap="round" />
            <path d="M116,172 L104,180 M136,170 L148,178" stroke="#1E2A2E" strokeWidth="3" strokeLinecap="round" />
          </g>
        )}

        {/* 身体 */}
        <ellipse cx={128} cy={118} rx={72} ry={38} fill="#1E2A2E" />
        <ellipse cx={128} cy={118} rx={72} ry={38} fill="none" stroke="rgba(255,255,255,.18)" strokeWidth={2} />
        {/* 腹部/逆光侧高光 */}
        <ellipse cx={124} cy={132} rx={50} ry={20} fill={PALETTE.featherHighlight} opacity={0.16} />

        {/* 头 */}
        <circle cx={206} cy={100} r={26} fill="#1E2A2E" />
        <circle cx={206} cy={100} r={26} fill="none" stroke="rgba(255,255,255,.18)" strokeWidth={1.6} />
        {/* 喙 */}
        <path d="M226,94 L260,104 L226,114 Z" fill={PALETTE.mistBlue} />
        {/* 眼 */}
        <circle cx={216} cy={95} r={3.4} fill={PALETTE.featherHighlight} />
        <circle cx={217} cy={94} r={1.3} fill="#1E2A2E" />

        {/* 远侧翅（收拢 / 扇动，被身体遮去一半） */}
        <g transform="translate(96,102) rotate(-16)">
          <g transform={`rotate(${-wingAngle * 0.8} 8 45) translate(-8,-45)`}>
            <Wing fill="#26353F" rim="rgba(30,42,46,.25)" darker opacity={0.9} />
          </g>
        </g>

        {/* 近侧翅（主翼） */}
        <g transform="translate(80,112) rotate(6)">
          <g transform={`rotate(${wingAngle} 8 45) translate(-8,-45)`}>
            <Wing fill="#1E2A2E" rim="rgba(30,42,46,.16)" />
          </g>
        </g>
      </svg>
    </div>
  );
};