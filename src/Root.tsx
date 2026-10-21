/**
 * Root.tsx · 组合注册
 *
 * 注册 <Composition>（整片剪辑），默认组件为 render-frame（占位帧）。转场编排见 Main.tsx。
 */
import React from "react";
import { Composition } from "remotion";
import { VIDEO } from "./config";
import { Main } from "./Main";

export const RemotionRoot: React.FC = () => {
  return (
    <>
      <Composition
        id="MountainBirdFlight"
        component={Main}
        durationInFrames={VIDEO.fps * VIDEO.totalDurationInSeconds}
        fps={VIDEO.fps}
        width={VIDEO.width}
        height={VIDEO.height}
        defaultProps={{}}
      />
      {/* 占位帧：供无渲染环境下的冒烟验证（无需渲染依赖） */}
      <Composition
        id="RenderFrame"
        component={() => (
          <div style={{ width: 1920, height: 1080, background: "#2E4A62" }} />
        )}
        durationInFrames={VIDEO.fps}
        fps={VIDEO.fps}
        width={VIDEO.width}
        height={VIDEO.height}
      />
    </>
  );
};
