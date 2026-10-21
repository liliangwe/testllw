/**
 * S1 · 晨雾起飞（帧 121–360，8s）
 * 枝头特写 → 振翅离枝、逆光轮廓、羽翼循环扇动 4–6 Hz。
 * 占位实现：待「并行场景开发」填充完整视觉。
 */
import React from "react";
import { AbsoluteFill } from "remotion";
import { PALETTE, TYPO, SAFE_MARGIN } from "../config";

export const Scene1_Takeoff: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        backgroundColor: PALETTE.forestGreen,
        justifyContent: "center",
        alignItems: "center",
        fontFamily: TYPO.serifFamily,
        color: PALETTE.featherHighlight,
        padding: SAFE_MARGIN,
      }}
    >
      <div style={{ fontSize: 56 }}>晨雾起飞 · 振翅离枝</div>
    </AbsoluteFill>
  );
};
