/**
 * index.ts · Remotion 入口
 *
 * 注册 Composition 到 Remotion，并在渲染前加载 Google Fonts（思源宋体/黑体）。
 */
import { registerRoot } from "remotion";
import { RemotionRoot } from "./Root";
import { loadFont } from "@remotion/google-fonts/NotoSerifSC";
import { loadFont as loadFontSans } from "@remotion/google-fonts/NotoSansSC";

loadFont("normal", { weights: ["600", "700", "900"], subsets: ["chinese-simplified"] });
loadFontSans("normal", { weights: ["400"], subsets: ["chinese-simplified"] });

registerRoot(RemotionRoot);
