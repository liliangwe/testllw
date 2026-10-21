/**
 * Particles.tsx · 微尘/花粉粒子
 *
 * 程序化漂浮粒子，随镜头共同漂移（横向缓移 + 上下微晃），
 * count 与 palette 可配，贯穿 S2 / S6。
 */
import React from "react";
import { useCurrentFrame, useVideoConfig } from "remotion";
import { MOTION } from "../../config";

export interface ParticlesProps {
  count?: number;
  color?: string;
  sizeMin?: number;
  sizeMax?: number;
  drift?: number;
  opacity?: number;
}

export const Particles: React.FC<ParticlesProps> = ({
  count = MOTION.particles.count,
  color = "#FFF3DC",
  sizeMin = 1.5,
  sizeMax = 4.5,
  drift = 18,
  opacity = 0.7,
}) => {
  const frame = useCurrentFrame();
  const { width, height } = useVideoConfig();
  const slots = Array.from({ length: count });
  return (
    <div style={{ position: "absolute", inset: 0, overflow: "hidden", opacity }}>
      {slots.map((_, i) => {
        const seedX = Math.abs(Math.sin(i * 12.9898) * 43758.5453) % 1;
        const seedY = Math.abs(Math.sin(i * 78.233) * 12543.21) % 1;
        const r = ((seedX * 19 + seedY * 7) % 1) * (sizeMax - sizeMin) + sizeMin;
        const wobbleAmp = 10 + ((i * 37) % 22);
        const x = ((seedX * width - frame * drift * (0.6 + ((i % 5) * 0.2))) % (width * 1.2) + width * 0.1);
        const y = seedY * height + Math.sin(frame * 0.02 + i) * wobbleAmp;
        return (
          <div
            key={i}
            style={{
              position: "absolute",
              left: x,
              top: y,
              width: r,
              height: r,
              borderRadius: "50%",
              background: color,
              boxShadow: `0 0 ${r * 2}px ${color}`,
              opacity: 0.35 + ((seedY * 131) % 0.55),
            }}
          />
        );
      })}
    </div>
  );
};