/**
 * Bokeh.tsx · 光斑层
 *
 * 多层圆形柔光（saturate + blur），作为贯穿全片的记忆点。
 * speedPxPerFrame 让光斑流横向掠过；driftSeed 打散纵向漂移。
 */
import React from "react";
import { useCurrentFrame, useVideoConfig } from "remotion";
import { MOTION } from "../../config";

export interface BokehProps {
  speed?: number;
  accent?: string;
  maxAlpha?: number;
  sizeBase?: number;
}

/** 预生成光斑（大/中/小三层，位置散点化） */
const SPOTS = [
  { x: 0.06, y: 0.18, r: 52, a: 0.5 },
  { x: 0.22, y: 0.72, r: 92, a: 0.42 },
  { x: 0.34, y: 0.3, r: 30, a: 0.6 },
  { x: 0.46, y: 0.6, r: 66, a: 0.45 },
  { x: 0.58, y: 0.12, r: 40, a: 0.55 },
  { x: 0.68, y: 0.78, r: 110, a: 0.38 },
  { x: 0.78, y: 0.36, r: 34, a: 0.6 },
  { x: 0.87, y: 0.62, r: 76, a: 0.42 },
  { x: 0.94, y: 0.16, r: 46, a: 0.52 },
  { x: 0.14, y: 0.46, r: 58, a: 0.4 },
  { x: 0.52, y: 0.9, r: 44, a: 0.48 },
  { x: 0.74, y: 0.5, r: 28, a: 0.5 },
];

export const Bokeh: React.FC<BokehProps> = ({
  speed = 46,
  accent = "#FFE9C4",
  maxAlpha = 0.5,
  sizeBase = 1,
}) => {
  const frame = useCurrentFrame();
  const { width, height } = useVideoConfig();
  const sat = MOTION.bokeh.saturation;
  const blurPx = MOTION.bokeh.blurPx;

  return (
    <div
      style={{
        position: "absolute",
        inset: 0,
        overflow: "hidden",
        filter: `saturate(${sat}) blur(${blurPx}px)`,
        mixBlendMode: "screen",
      }}
    >
      {SPOTS.map((s, i) => {
        const drift = (i % 3 - 1) * 6;
        const x = ((s.x * width - frame * speed + i * 37) % (width * 1.25) + width * 0.2);
        const y = (s.y * height + frame * drift) % height;
        const r = s.r * sizeBase;
        return (
          <div
            key={i}
            style={{
              position: "absolute",
              left: x,
              top: y,
              width: r * 2,
              height: r * 2,
              transform: "translate(-50%,-50%)",
              borderRadius: "50%",
              background: `radial-gradient(circle at 35% 35%, ${accent} 0%, ${accent}88 40%, ${accent}00 72%)`,
              opacity: s.a,
            }}
          />
        );
      })}
    </div>
  );
};