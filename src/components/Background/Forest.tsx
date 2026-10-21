/**
 * Forest.tsx · 林海剪影背景
 *
 * 用程序化 SVG 生成两层远近林线剪影，可随参数调整分层高度与色值。
 */
import React from "react";

export interface ForestProps {
  /** 底部对齐（通常 = 画布高） */
  baseY?: number;
  near?: string;
  far?: string;
  /** 远层放大（用于 S3 俯瞰的广袤感） */
  farScale?: number;
  opacity?: number;
}

export const Forest: React.FC<ForestProps> = ({
  baseY = 1080,
  near = "#1E2A2E",
  far = "#2E4A62",
  farScale = 1,
  opacity = 1,
}) => (
  <svg
    width="1920"
    height="1080"
    viewBox="0 0 1920 1080"
    preserveAspectRatio="none"
    style={{ position: "absolute", bottom: 0, left: 0, opacity, display: "block" }}
  >
    {/* 远层林线（更浅、更平缓） */}
    <g transform={`translate(0 ${1080 - baseY}) scale(1 ${farScale})`}>
      <path
        d={mountainRange(0)}
        fill={far}
        opacity={0.55}
        transform="translate(0,-8)"
      />
    </g>
    {/* 近层林线（更深、更密） */}
    <g transform={`translate(0 ${1080 - baseY})`}>
      <path d={mountainRange(1)} fill={near} opacity={0.92} />
    </g>
  </svg>
);

/** 生成一条林线路径（覆盖 1920 宽，含若干树形/山头起伏） */
function mountainRange(seed: number): string {
  const points: [number, number][] = [];
  const amp = seed === 0 ? 220 : 150;
  const base = seed === 0 ? 160 : 100;
  let x = 0;
  let i = 0;
  while (x < 1980) {
    const w = 120 + ((i * 131 + seed * 97) % 180);
    const h = base + ((i * 89 + seed * 41) % amp);
    points.push([x, 1080 - h]);
    points.push([x + w * 0.42, 1080 - h - (amp * 0.55)]);
    points.push([x + w * 0.7, 1080 - h - (amp * 0.25)]);
    points.push([x + w, 1080 - h]);
    x += w;
    i += 1;
  }
  const d = points.reduce((acc, [px, py], idx) => {
    if (idx === 0) return `M${px},${py}`;
    const [lx, ly] = points[idx - 1];
    const cx = (lx + px) / 2;
    return `${acc} Q${cx},${Math.min(ly, py) - 18} ${px},${py}`;
  }, "");
  return `${d} L1980,1080 L0,1080 Z`;
}