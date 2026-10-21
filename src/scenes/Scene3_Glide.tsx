/**
 * S3 · 滑翔俯瞰（帧 781–1140，12s）
 * 拉升俯瞰群山林海，鸟呈优雅滑翔姿态，飞行轨迹用贝塞尔平滑曲线。
 * 占位实现：待「并行场景开发」填充完整视觉。
 */
import React from "react";
import { AbsoluteFill } from "remotion";
import { PALETTE, TYPO, SAFE_MARGIN } from "../config";

export const Scene3_Glide: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        backgroundColor: PALETTE.skyBlue,
        justifyContent: "center",
        alignItems: "center",
        fontFamily: TYPO.serifFamily,
        color: PALETTE.featherHighlight,
        padding: SAFE_MARGIN,
      }}
    >
      <div style={{ fontSize: 56 }}>滑翔俯瞰 · 群山林海</div>
    </AbsoluteFill>
  );
};
