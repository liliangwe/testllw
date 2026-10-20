# ARCHITECTURE.md

## 项目概述

基于 JDK 标准库的 Java 算法工具集，包路径 `com.example`。

## 模块列表

| 模块 | 职责 |
|------|------|
| quicksort | 快速排序工具类，提供 int 数组原地升序排序 |

## 分层结构

```
src/main/java/com/example/   → 工具类源码
src/test/java/com/example/   → 单元测试
```

## AI 编码约束

- 改动前先读本文件
- 工具类必须为 `final class` + 私有构造方法
- 所有 public 方法必须有完整 Javadoc
- 测试先于实现（TDD）