/**
 * Feather.tsx · 羽毛细节（standalone SVG）
 *
 * 用于 S1 逆光轮廓中的羽毛特写 / 飘落羽毛等细节元素。
 */
import React from "react";

export interface FeatherProps {
  width?: number;
  height?: number;
  fill?: string;
  secondary?: string;
  opacity?: number;
  style?: React.CSSProperties;
}

export const Feather: React.FC<FeatherProps> = ({
  width = 160,
  height = 36,
  fill = "#F4EBDD",
  secondary = "#E8A85C",
  opacity = 1,
  style,
}) => (
  <svg
    width={width}
    height={height}
    viewBox="0 0 200 44"
    style={style}
    opacity={opacity}
    fill="none"
  >
    {/* 羽片（朝右） */}
    <path
      d="M6,22 C40,4 122,2 180,13 L196,21 L180,29 C122,40 40,40 6,22 Z"
      fill={fill}
      opacity={0.9}
    />
    <path
      d="M6,22 C40,9 118,5 174,13"
      stroke={secondary}
      strokeWidth="2"
    />
    <path
      d="M6,22 C40,35 118,39 174,31"
      stroke="rgba(30,42,46,.14)"
      strokeWidth="1.4"
    />
    {/* 羽轴 */}
    <path d="M6,22 L196,21" stroke={secondary} strokeWidth="2.4" strokeLinecap="round" />
  </svg>
);