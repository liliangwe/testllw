/**
 * Subtitle.tsx · 字幕 / 金句
 *
 * 金句 56px 衬线 / 辅助小字 28px 黑体；支持透明度与渐入产生器。
 * 用于 S5 金句浮现与辅助文案。
 */
import React from "react";
import { TYPO } from "../../config";

export type SubtitleKind = "quote" | "caption";

export interface SubtitleProps {
  text: string;
  kind?: SubtitleKind;
  fontSize?: number;
  color?: string;
  opacity?: number;
  lineHeight?: number;
}

export const Subtitle: React.FC<SubtitleProps> = ({
  text,
  kind = "quote",
  fontSize,
  color = "#1E2A2E",
  opacity = 1,
  lineHeight = 1.5,
}) => {
  const size = fontSize ?? (kind === "quote" ? TYPO.subtitle : TYPO.caption);
  const family = kind === "quote" ? TYPO.serifFamily : TYPO.sansFamily;
  const weight = kind === "quote" ? 600 : 400;
  return (
    <div
      style={{
        fontFamily: family,
        fontSize: size,
        color,
        opacity,
        lineHeight,
        fontWeight: weight,
        textAlign: "center",
        letterSpacing: kind === "quote" ? "0.04em" : "0.02em",
      }}
    >
      {text}
    </div>
  );
};