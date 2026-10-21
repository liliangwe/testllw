/**
 * Sky.tsx · 天空渐变环境层
 *
 * 依据色板生成多段竖向渐变天空；支持不同氛围（晨雾 / 高空 / 夕照）。
 */
import React from "react";

export type Mood = "mist" | "forest" | "high" | "dusk";

export interface SkyProps {
  mood?: Mood;
  top?: string;
  middle?: string;
  bottom?: string;
}

const PRESETS: Record<Mood, { top: string; middle: string; bottom: string }> = {
  // 晨雾冷青
  mist: { top: "#6E8BA3", middle: "#3E6B7A", bottom: "#E8D8BC" },
  // 林间绿
  forest: { top: "#5A7A6A", middle: "#3E6B54", bottom: "#1E3A2E" },
  // 高空湛蓝
  high: { top: "#9CC0D8", middle: "#7AA3B8", bottom: "#4F7D97" },
  // 暖金夕照
  dusk: { top: "#B4785A", middle: "#D9975E", bottom: "#E8A85C" },
};

export const Sky: React.FC<SkyProps> = ({ mood = "mist", top, middle, bottom }) => {
  const p = PRESETS[mood];
  return (
    <div
      style={{
        position: "absolute",
        inset: 0,
        background: `linear-gradient(180deg, ${top ?? p.top} 0%, ${middle ?? p.middle} 52%, ${bottom ?? p.bottom} 100%)`,
      }}
    />
  );
};