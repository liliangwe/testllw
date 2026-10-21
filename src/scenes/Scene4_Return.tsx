/**
 * S4 · 群鸟归林（帧 1141–1500，12s）
 * 多鸟结伴归林、掠过树冠、暖金夕照光晕、光斑收束。
 * 占位实现：待「并行场景开发」填充完整视觉。
 */
import React from "react";
import { AbsoluteFill } from "remotion";
import { PALETTE, TYPO, SAFE_MARGIN } from "../config";

export const Scene4_Return: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        backgroundColor: PALETTE.gold,
        justifyContent: "center",
        alignItems: "center",
        fontFamily: TYPO.serifFamily,
        color: PALETTE.ink,
        padding: SAFE_MARGIN,
      }}
    >
      <div style={{ fontSize: 56 }}>群鸟归林 · 暖金夕照</div>
    </AbsoluteFill>
  );
};
