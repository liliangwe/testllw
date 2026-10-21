/**
 * S5 · 片尾落版（帧 1501–1680，6s）
 * 金句字幕浮现、落版暖白底 + 墨色文案、整体淡出。
 * 占位实现：待「并行场景开发」填充完整视觉。
 */
import React from "react";
import { AbsoluteFill } from "remotion";
import { PALETTE, TYPO, SAFE_MARGIN } from "../config";

export const Scene5_Outro: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        backgroundColor: PALETTE.paper,
        justifyContent: "center",
        alignItems: "center",
        fontFamily: TYPO.serifFamily,
        color: PALETTE.ink,
        padding: SAFE_MARGIN,
      }}
    >
      <div style={{ fontSize: TYPO.subtitle }}>穿过林间光影，重拾轻盈与自由。</div>
      <div style={{ fontSize: TYPO.title, marginTop: 40 }}>山鸟在林间飞翔</div>
    </AbsoluteFill>
  );
};
