/**
 * Main.tsx · 整片编排骨架
 *
 * 依据 brief.md / workbench.html 的帧区间，用 <Sequence> 编排各场景：
 *   S0 1–120 · S1 121–360 · S2 361–780 · S3 781–1140 · S4 1141–1500 · S5 1501–1680 · 留白 1681–1800
 *
 * 各场景内容在「并行场景开发」阶段填充；此处以占位场景保证骨架可渲染、可冒烟。
 */
import React from "react";
import { AbsoluteFill, Sequence } from "remotion";
import {
  SCENE_SECONDS,
  SCENE_FRAMES,
  VIDEO,
} from "./config";
import { Scene0_Hook } from "./scenes/Scene0_Hook";
import { Scene1_Takeoff } from "./scenes/Scene1_Takeoff";
import { Scene2_Weave } from "./scenes/Scene2_Weave";
import { Scene3_Glide } from "./scenes/Scene3_Glide";
import { Scene4_Return } from "./scenes/Scene4_Return";
import { Scene5_Outro } from "./scenes/Scene5_Outro";
import { Scene6_Breath } from "./scenes/Scene6_Breath";

export const Main: React.FC = () => {
  // 各场景时长（帧数）由 config 单一真相源推导
  const fps = VIDEO.fps;
  const d0 = SCENE_SECONDS.S0_HOOK * fps;
  const d1 = SCENE_SECONDS.S1_TAKEOFF * fps;
  const d2 = SCENE_SECONDS.S2_WEAVE * fps;
  const d3 = SCENE_SECONDS.S3_GLIDE * fps;
  const d4 = SCENE_SECONDS.S4_RETURN * fps;
  const d5 = SCENE_SECONDS.S5_OUTRO * fps;
  const d6 = SCENE_SECONDS.S6_BREATH * fps;

  return (
    <AbsoluteFill style={{ backgroundColor: "#0d1620" }}>
      <Sequence durationInFrames={d0} name="S0_Hook">
        <Scene0_Hook />
      </Sequence>
      <Sequence from={SCENE_FRAMES.S1_TAKEOFF.start - 1} durationInFrames={d1} name="S1_Takeoff">
        <Scene1_Takeoff />
      </Sequence>
      <Sequence from={SCENE_FRAMES.S2_WEAVE.start - 1} durationInFrames={d2} name="S2_Weave">
        <Scene2_Weave />
      </Sequence>
      <Sequence from={SCENE_FRAMES.S3_GLIDE.start - 1} durationInFrames={d3} name="S3_Glide">
        <Scene3_Glide />
      </Sequence>
      <Sequence from={SCENE_FRAMES.S4_RETURN.start - 1} durationInFrames={d4} name="S4_Return">
        <Scene4_Return />
      </Sequence>
      <Sequence from={SCENE_FRAMES.S5_OUTRO.start - 1} durationInFrames={d5} name="S5_Outro">
        <Scene5_Outro />
      </Sequence>
      <Sequence from={SCENE_FRAMES.S6_BREATH.start - 1} durationInFrames={d6} name="S6_Breath">
        <Scene6_Breath />
      </Sequence>
    </AbsoluteFill>
  );
};
