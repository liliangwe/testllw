/**
 * S0 · 片头 Hook（帧 1–120，4s）
 *
 * 晨曦微光穿透林雾 → 片名浮现 → 一声清脆鸟鸣（视觉脉冲提示）→ 环境层冷青基调。
 * 转场：淡入（首帧暗底渐显）；镜头微推近。
 */
import React from "react";
import {
  AbsoluteFill,
  useCurrentFrame,
  useVideoConfig,
  interpolate,
} from "remotion";
import { PALETTE, SAFE_MARGIN, STORY } from "../config";
import { Sky } from "../components/Background/Sky";
import { Forest } from "../components/Background/Forest";
import { Mist } from "../components/Background/Mist";
import { LightRay } from "../components/FX/LightRay";
import { Bokeh } from "../components/Foreground/Bokeh";
import { Title } from "../components/UI/Title";

export const Scene0_Hook: React.FC = () => {
  const frame = useCurrentFrame();
  const { durationInFrames } = useVideoConfig();

  // 整体淡入（0–30 帧）
  const fadeIn = interpolate(frame, [0, 30], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // 镜头微推近（1.0 → 1.08）
  const push = interpolate(frame, [0, durationInFrames], [1.0, 1.08]);

  // 片名浮现（淡入 + 轻微上移）
  const titleOpacity = interpolate(frame, [24, 56], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const titleY = interpolate(frame, [24, 70], [30, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // 鸟鸣视觉提示：帧 58–80 一次清脆脉冲
  const chirp = interpolate(frame, [58, 66, 80], [0, 1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <AbsoluteFill style={{ backgroundColor: PALETTE.ink }}>
      <div
        style={{
          position: "absolute",
          inset: 0,
          transform: `scale(${push})`,
          transformOrigin: "center",
          opacity: fadeIn,
        }}
      >
        <Sky mood="mist" />
        <Forest baseY={1080} far="#23465C" near="#142C3A" opacity={0.85} />
        <Mist opacity={0.8} />
        <LightRay opacity={0.3} />
        <Bokeh speed={20} accent="#FFE9C4" maxAlpha={0.35} />

        {/* 片名 + 装饰线 */}
        <div
          style={{
            position: "absolute",
            inset: 0,
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "center",
            padding: SAFE_MARGIN,
            transform: `translateY(${titleY}px)`,
          }}
        >
          <Title text={STORY.title} opacity={titleOpacity} color="#FFF9F0" />
          <div
            style={{
              marginTop: 28,
              width: 120,
              height: 2,
              background: PALETTE.gold,
              opacity: titleOpacity * 0.8,
            }}
          />
        </div>

        {/* 鸟鸣提示脉冲 */}
        {chirp > 0.01 && (
          <div
            style={{
              position: "absolute",
              top: "22%",
              left: "50%",
              width: 26,
              height: 26,
              marginLeft: -13,
              marginTop: -13,
              borderRadius: "50%",
              border: `2px solid ${PALETTE.gold}`,
              boxShadow: `0 0 ${18 * chirp}px ${PALETTE.gold}`,
              transform: `scale(${0.4 + chirp * 1.4})`,
              opacity: chirp,
            }}
          />
        )}
      </div>
    </AbsoluteFill>
  );
};