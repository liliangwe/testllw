/**
 * LightRay.tsx · 光束层（S0 斜扫晨光）
 *
 * 从右上角斜射的多条柔光光束，宽度/角度/亮度随 frame 缓慢呼吸，
 * 叠加在雾层之上，营造晨曦穿透林雾的斜扫效果。
 */
import React from "react";
import { useCurrentFrame } from "remotion";

export interface LightRayProps {
  color?: string;
  opacity?: number;
  /** 光束数量 */
  count?: number;
  /** 从顶部向右下倾斜的角度（deg） */
  angleDeg?: number;
}

export const LightRay: React.FC<LightRayProps> = ({
  color = "#FFE9C4",
  opacity = 0.34,
  count = 5,
  angleDeg = 22,
}) => {
  const frame = useCurrentFrame();
  const breathe = 0.82 + Math.sin(frame * 0.03) * 0.18;
  return (
    <div
      style={{
        position: "absolute",
        inset: 0,
        overflow: "hidden",
        opacity: opacity * breathe,
        transform: `rotate(${angleDeg}deg) scaleX(1.4)`,
        transformOrigin: "top right",
        mixBlendMode: "screen",
      }}
    >
      {Array.from({ length: count }).map((_, i) => {
        const left = 14 + i * 17;
        const w = 6 + ((i * 31) % 22);
        return (
          <div
            key={i}
            style={{
              position: "absolute",
              top: -120,
              left: `${left}%`,
              width: `${w}%`,
              height: "150%",
              background: `linear-gradient(90deg, ${color}00 0%, ${color}cc 45%, ${color}00 100%)`,
              filter: "blur(6px)",
            }}
          />
        );
      })}
      {/* 覆盖层模糊整束光 */}
      <div
        style={{
          position: "absolute",
          inset: 0,
          background: `radial-gradient(1200px 700px at 78% -6%, ${color}66 0%, transparent 62%)`,
        }}
      />
    </div>
  );
};