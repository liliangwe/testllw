/**
 * Mist.tsx · 雾霭层
 *
 * 多层径向渐变雾带，随 frame 缓慢横向漂移；低饱和，营造晨雾冷青基调。
 */
import React from "react";
import { useCurrentFrame, useVideoConfig } from "remotion";
import { PALETTE } from "../../config";

export interface MistProps {
  opacity?: number;
}

/** 一组雾带配置（横向位置 / 高度 / 半径等比） */
const BANDS = [
  { y: 0.86, r: 0.6, tone: "#CFE0EA", speed: 14, start: -0.2 },
  { y: 0.72, r: 0.5, tone: "#E8F0F4", speed: 22, start: 0.4 },
  { y: 0.95, r: 0.7, tone: PALETTE.mistBlue, speed: 9, start: 0.1 },
];

export const Mist: React.FC<MistProps> = ({ opacity = 0.85 }) => {
  const frame = useCurrentFrame();
  const { width, height } = useVideoConfig();
  return (
    <div style={{ position: "absolute", inset: 0, opacity, overflow: "hidden" }}>
      {BANDS.map((b, i) => {
        const x = ((b.start * width + frame * b.speed) % (width * 1.6)) - width * 0.3;
        const rx = width * b.r;
        const ry = height * 0.22;
        return (
          <div
            key={i}
            style={{
              position: "absolute",
              left: x,
              top: height * b.y,
              width: rx * 2,
              height: ry * 2,
              transform: "translate(-50%,-50%)",
              background: `radial-gradient(ellipse at center, ${b.tone}cc 0%, ${b.tone}52 42%, ${b.tone}00 72%)`,
              filter: "blur(14px)",
            }}
          />
        );
      })}
    </div>
  );
};