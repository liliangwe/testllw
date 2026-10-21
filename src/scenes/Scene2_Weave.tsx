/**
 * S2 · 穿枝过叶（帧 361–780，14s）
 * 跟拍横移、Bokeh 光斑扫过、枝叶虚化前景视差、微尘粒子。
 * 占位实现：待「并行场景开发」填充完整视觉。
 */
import React from "react";
import { AbsoluteFill } from "remotion";
import { PALETTE, TYPO, SAFE_MARGIN } from "../config";

export const Scene2_Weave: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        backgroundColor: PALETTE.canopyGreen,
        justifyContent: "center",
        alignItems: "center",
        fontFamily: TYPO.serifFamily,
        color: PALETTE.featherHighlight,
        padding: SAFE_MARGIN,
      }}
    >
      <div style={{ fontSize: 56 }}>穿枝过叶 · 光斑跟随</div>
    </AbsoluteFill>
  );
};
