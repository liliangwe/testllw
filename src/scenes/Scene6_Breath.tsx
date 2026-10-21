/**
 * S6 · 呼吸留白（帧 1681–1800，4s）
 * 节奏缓冲，无新增叙事信息。低饱和雾面渐变，微尘粒子漂浮。
 * 占位实现：待「并行场景开发」填充完整视觉。
 */
import React from "react";
import { AbsoluteFill } from "remotion";
import { PALETTE, TYPO, SAFE_MARGIN } from "../config";

export const Scene6_Breath: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        backgroundColor: PALETTE.mistBlue,
        justifyContent: "center",
        alignItems: "center",
        fontFamily: TYPO.serifFamily,
        color: PALETTE.featherHighlight,
        padding: SAFE_MARGIN,
      }}
    >
      <div style={{ fontSize: 28, opacity: 0.6 }}>……</div>
    </AbsoluteFill>
  );
};
