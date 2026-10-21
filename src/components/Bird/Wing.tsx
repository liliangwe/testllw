/**
 * Wing.tsx · 羽翼组件（可循环扇动）
 *
 * 作为 <g> 片段嵌入 Bird 的 SVG 中，pivot 默认位于 (8, 45)，
 * 由 Bird 以 rotate(flapAngle 8 45) 驱动扇动。
 */
import React from "react";

export interface WingProps {
  fill: string;
  rim?: string;
  opacity?: number;
  /** 远侧翅膀（被身体遮挡，颜色更深、高光更弱） */
  darker?: boolean;
}

export const Wing: React.FC<WingProps> = ({
  fill,
  rim = "rgba(30,42,46,.18)",
  opacity = 1,
  darker = false,
}) => (
  <g opacity={opacity}>
    {/* 主翼：带两处羽毛缺口的水滴形 */}
    <path
      d="M8,45 C16,8 78,0 150,12 L150,20 C90,24 46,30 30,42 L30,48 C46,40 90,34 150,32 L150,42 C84,52 40,60 22,66 C14,62 10,54 8,45 Z"
      fill={fill}
      stroke={darker ? rim : "rgba(30,42,46,.12)"}
      strokeWidth={1.5}
    />
    {/* 羽干高光 */}
    <path
      d="M8,45 C16,12 80,4 150,15"
      stroke="rgba(255,255,255,.30)"
      strokeWidth="2"
      fill="none"
      opacity={darker ? 0.5 : 0.9}
    />
    {/* 初级飞羽分隔线 */}
    <path
      d="M34,44 L40,26 M60,48 L66,22 M104,50 L108,22"
      stroke={rim}
      strokeWidth="1.4"
      fill="none"
      opacity={darker ? 0.5 : 0.9}
    />
  </g>
);