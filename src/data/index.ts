/**
 * data/index.ts · 数据接入层
 *
 * 统一从 verified_data.json 导入校验后的数据。
 * 当前为氛围/意境片（has_benchmark_or_comparison_table = false），无数据类场景；
 * 该模块保留供后续若需加入数据场景（如迁徙里程/生态数据）时直接引用。
 */
import verifiedData from "./verified_data.json";

export const DATA = verifiedData;

/** 是否存在需要数据展示的 benchmark / 对比表（当前为 false） */
export const hasBenchmarkData = DATA.conclusion.has_benchmark_or_comparison_table;

export default verifiedData;
