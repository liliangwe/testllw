/**
 * Title.tsx · 片名字幕（72px 衬线）
 *
 * 居中片名，可选逐字/整体淡入；用于 S0 片名浮现与 S5 落版。
 */
import React from "react";
import { TYPO } from "../../config";

export interface TitleProps {
  text: string;
  fontSize?: number;
  color?: string;
  opacity?: number;
  letterSpacing?: string;
}

export const Title: React.FC<TitleProps> = ({
  text,
  fontSize = TYPO.title,
  color = "#F4EBDD",
  opacity = 1,
  letterSpacing = "0.08em",
}) => (
  <div
    style={{
      fontFamily: TYPO.serifFamily,
      fontSize,
      color,
      opacity,
      letterSpacing,
      fontWeight: 700,
      textAlign: "center",
      lineHeight: 1.25,
    }}
  >
    {text}
  </div>
);