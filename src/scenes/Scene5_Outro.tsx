/**
 * S5 · 片尾落版（帧 1501–1680，6s）
 *
 * 金句字幕浮现（56px 衬线）→ 落版暖白底 + 墨色文案 → 末尾整体淡出。
 * 转场：淡出收尾。
 */
import React from "react";
import { interpolate, useCurrentFrame, useVideoConfig } from "remotion";
import { PALETTE, STORY } from "../config";
import { Title } from "../components/UI/Title";
import { Subtitle } from "../components/UI/Subtitle";
import { Mist } from "../components/Background/Mist";
import { Particles } from "../components/Foreground/Particles";

export const Scene5_Outro: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps, durationInFrames } = useVideoConfig();

  // 金句浮现（0.8–2.4s）
  const quoteOpacity = interpolate(frame, [fps * 0.8, fps * 2.0], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const quoteY = interpolate(frame, [fps * 0.8, fps * 2.2], [24, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // 落版片名（2.8–4.2s）
  const titleOpacity = interpolate(frame, [fps * 2.8, fps * 4.2], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // 末尾整体淡出（最后 24 帧）
  const fadeOut = interpolate(frame, [durationInFrames - fps * 0.8, durationInFrames], [1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <div
      style={{
        position: "absolute",
        inset: 0,
        background: PALETTE.paper,
        opacity: fadeOut,
      }}
    >
      <Mist opacity={0.3} />
      <Particles count={18} color="#5A4A38" drift={6} opacity={0.3} />

      <div
        style={{
          position: "absolute",
          inset: 0,
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
          padding: "0 160px",
        }}
      >
        <div style={{ transform: `translateY(${quoteY}px)`, opacity: quoteOpacity }}>
          <Subtitle text={STORY.quote} kind="quote" color={PALETTE.ink} />
        </div>

        <div style={{ marginTop: 56, opacity: titleOpacity }}>
          <div
            style={{
              width: 100,
              height: 2,
              margin: "0 auto 24px",
              background: PALETTE.gold,
            }}
          />
          <Title text={STORY.title} color={PALETTE.ink} />
          <Subtitle text="晨雾 · 林间 · 光与风" kind="caption" color="#4A5A5E" />
        </div>
      </div>
    </div>
  );
};