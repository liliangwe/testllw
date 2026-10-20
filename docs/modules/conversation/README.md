# conversation 模块

## 模块职责

解析自然语言文本，识别创建任务意图，提取事项名称与描述，调用任务模块完成创建。

## 关键类

| 类 | 职责 | 核心方法 |
|---|------|----------|
| `IntentParser` | 基于关键词的意图识别 | `is_create_intent(text) -> bool` |
| `EntityExtractor` | 基于正则的实体提取 | `extract(text) -> dict` |
| `ConversationService` | 对话服务门面 | `create_from_text(text, data_file=None) -> dict` |

## 依赖关系

```
conversation → task (validate_name, add_task)
```

单向依赖，无循环。

## API 接口列表

### S01: create_from_text

```python
ConversationService().create_from_text(text: str, data_file: Path | None = None) -> dict
```

- 输入：自然语言文本
- 输出：`{name, description, created_at}`
- 异常：`ValueError`（空文本/非意图/无法提取名称/非法名称）、`OSError`（文件写入失败）

### S02: is_create_intent

```python
IntentParser().is_create_intent(text: str) -> bool
```

### S03: extract

```python
EntityExtractor().extract(text: str) -> dict
```

- 输出：`{name, description}`

## 常量

| 常量 | 说明 |
|------|------|
| `CREATE_INTENT_KEYWORDS` | 触发创建意图的关键词列表 |
| `NON_INTENT_INDICATORS` | 非创建意图的排除词列表 |
| 正则 `NAME_PATTERN` | 名称+描述提取规则 |