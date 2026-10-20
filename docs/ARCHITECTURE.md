# Todo CLI 架构文档

## 项目概述

日常待办事项管理工具，Python 3.9+ CLI 应用，零第三方依赖。

## 模块列表

| 模块 | 路径 | 职责 |
|------|------|------|
| 任务模块 | `src/todo_cli/main.py` | 任务名称校验、创建、持久化、CLI 入口 |
| 对话模块 | `src/todo_cli/conversation.py` | 自然语言意图识别、实体提取、对话创建任务 |

## 分层架构

```
交互层
  ├── todo add   (CLI 结构化参数)
  └── todo chat  (CLI 自然语言文本)
        │
核心服务层
  ├── conversation 模块 ──→ task 模块
  │     IntentParser       validate_name
  │     EntityExtractor    add_task
  │     ConversationService
  │
数据层
  └── ~/.todo/tasks.json (JSON 文件)
```

## 技术约束

- Python 3.9+
- 零第三方依赖（标准库 only）
- 数据存储：`~/.todo/tasks.json`
- 测试框架：unittest