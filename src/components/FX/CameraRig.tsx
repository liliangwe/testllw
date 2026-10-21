/**
 * CameraRig.tsx · 镜头机位（推 / 移 / 拉）
 *
 * 对 children 施加 2D 机位变换（位移 + 缩放 + 旋转），
 * 由外部在每帧计算 transform 参数从而驱动镜头语言。
 */
import type { CSSProperties } from "react";
import React from "react";

export interface CameraRigProps {
  x?: number;
  y?: number;
  scale?: number;
  rotate?: number;
  children: React.ReactNode;
}

export const CameraRig: React.FC<CameraRigProps> = ({
  x = 0,
  y = 0,
  scale = 1,
  rotate = 0,
  children,
}) => {
  const style: CSSProperties = {
    position: "absolute",
    top: -300,
    left: -300,
    width: "calc(100% + 600px)",
    height: "calc(100% + 600px)",
    transform: `translate(${x}px, ${y}px) scale(${scale}) rotate(${rotate}deg)`,
    transformOrigin: "center",
  };
  return <div style={style}>{children}</div>;
};