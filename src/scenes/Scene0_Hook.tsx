/**
 * S0 · 片头 Hook（帧 1–120，4s）
 * 晨曦微光穿透林雾、片名浮现、一声鸟鸣（音频提示）。
 * 占位实现：待「并行场景开发」填充完整视觉。
 */
import React from "react";
import { AbsoluteFill } from "remotion";
import { PALETTE, TYPO, SAFE_MARGIN } from "../config";

export const Scene0_Hook: React.FC = () => {
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
      <div style={{ fontSize: TYPO.title }}>片头 Hook · 山鸟在林间飞翔</div>
    </AbsoluteFill>
  );
};
