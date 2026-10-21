/**
 * LeafLayer.tsx · 枝叶虚化前景
 *
 * 大面积柔焦叶片剪影，作为视差前景掠过画面，强化跟拍速度感。
 * speedPxPerFrame 控制横向掠过速度（可为 0）。
 */
import React from "react";
import { useCurrentFrame } from "remotion";

export interface LeafLayerProps {
  speed?: number;
  blurPx?: number;
  color?: string;
  opacity?: number;
}

export const LeafLayer: React.FC<LeafLayerProps> = ({
  speed = 60,
  blurPx = 9,
  color = "#1E2A2E",
  opacity = 0.55,
}) => {
  const frame = useCurrentFrame();
  const shift = (frame * speed) % 3840;
  return (
    <div
      style={{
        position: "absolute",
        inset: 0,
        opacity,
        overflow: "hidden",
        filter: `blur(${blurPx}px)`,
      }}
    >
      <svg
        width="3840"
        height="1080"
        viewBox="0 0 3840 1080"
        preserveAspectRatio="none"
        style={{ position: "absolute", left: -shift, top: 0, height: "100%" }}
      >
        {/* 左上垂枝 */}
        <path d="M120,0 C140,260 60,420 220,620 C150,420 250,260 360,40 Z" fill={color} opacity={0.85} />
        <path d="M320,0 C330,220 300,360 420,520 C360,360 420,220 560,0 Z" fill={color} opacity={0.7} />
        {/* 上缘垂叶 */}
        <path d="M1180,0 C1250,180 1180,320 1320,470 C1240,320 1340,150 1520,0 Z" fill={color} opacity={0.6} />
        {/* 中景独立叶片 */}
        <path d="M2300,120 C2400,300 2350,480 2640,640 C2460,430 2560,220 2780,60 Z" fill={color} opacity={0.8} />
        <path d="M3250,40 C3380,240 3300,440 3560,600 C3380,400 3520,180 3780,0 Z" fill={color} opacity={0.72} />
      </svg>
    </div>
  );
};