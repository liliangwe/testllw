# Code Review Report

> **Change** 内核升级-对话创建任务 · **分支/Commit** `AI/task-DEV-966dcd0a` / `e500958` · **日期** 2026-10-20 · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**。本变更不包含 Java 文件（Python 项目），已按 SDD 范式适配审查流程。问题须含 `path:line`。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| Python 源文件数 | 2（conversation.py 新增, main.py 修改） |
| 测试文件数 | 1（test_conversation.py 新增） |
| 变更行数 | `+658 / -13` |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| `IntentParser` | `src/todo_cli/conversation.py:40` | 基于关键词的意图识别 |
| `EntityExtractor` | `src/todo_cli/conversation.py:88` | 基于正则的实体提取 |
| `ConversationService` | `src/todo_cli/conversation.py:123` | 对话服务门面 |
| `build_parser` / `main` | `src/todo_cli/main.py:80,107` | CLI 入口（chat 子命令注册） |

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 2 | 3 |

---

## 3. Step 2 — 功能（REQ）

### REQ-F01: 对话创建任务 — 从自然语言中提取名称和描述并创建待办事项

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 正常创建（仅名称） | ✅ | design.md L45: "从自然语言中提取名称和描述并创建待办事项" | `conversation.py:134-163` → `add_task` | `create_from_text("添加写周报")` 正确返回 `{name, description, created_at}` |
| 正常创建（名称+描述） | ✅ | design.md L277-281 响应示例 | `test_conversation.py:197-204` | `"帮我记一下 写周报，描述：周五前完成"` 正确提取并创建 |
| 默认数据文件路径 | ✅ | design.md L255 默认 `DEFAULT_DATA_FILE` | `conversation.py:162` → `add_task(data_file=None)` → `main.py:65-66` | 未传 `data_file` 时使用 `~/.todo/tasks.json` |
| 复用已有 `validate_name` | ✅ | design.md L56 A04 假设 | `conversation.py:14` import + L159 调用 | 创建前调用 `add_task` 内部触发 `validate_name` |
| 复用已有 `add_task` | ✅ | design.md L56 A04 假设 | `conversation.py:159-163` | 完全委托给已有函数 |

### REQ-F02: 意图识别 — 区分"创建任务"与"非任务对话"

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 正例：检测创建意图 | ✅ | design.md L46: "区分创建任务与非任务对话" | `conversation.py:47-83` 最早匹配启发式 | 7 个正例关键词全部通过测试 |
| 反例：拒绝随机文本 | ✅ | design.md L351 异常场景 | `conversation.py:62-63` 空文本检查 + 关键词扫描 | "今天天气怎么样" 正确拒绝 |
| 排除词优先 | ✅ | design.md L287 "不包含 NON_INTENT_INDICATORS 中明确排除词" | `conversation.py:76-81` 最早匹配排除词时拒绝 | 6 个排除词全部通过测试 |
| ⚠️ 创建词与排除词共存（创建词在前） | ⚠️ | design.md L287 原始 AND 逻辑：「包含创建词 AND NOT 包含排除词」 | `conversation.py:47-83` 最早匹配 → 接受 | 实现改用「最早匹配」启发式，与原始 spec 存在行为偏差。`"创建待办事项，完成后查看"` 在实现中返回 `True`，原始 spec 会返回 `False`。impl.md L56 已记录此决策。 |

---

## 4. Step 3 — 可读性检查

> 适配自 Java 可读性清单（A1–A7），针对 Python 项目调整。

| ID | 规则（Python 对应） | 结果 | 说明 |
|----|---------------------|------|------|
| A1.1 | 文件名 = 模块名 + `.py` | ✅ | `conversation.py` 符合 |
| A1.2 | UTF-8 编码 | ✅ | `from __future__ import annotations` + 中文常量正常 |
| A3.3 | 4 空格缩进 | ✅ | 全文件统一 |
| A3.4 | 行宽 ≤ 120 | ✅ | 最长行约 100 字符 |
| A3.7 | 运算符两侧空格 | ✅ | PEP 8 合规 |
| A4.2 | 类名 PascalCase | ✅ | `IntentParser`, `EntityExtractor`, `ConversationService` |
| A4.3 | 方法名 snake_case | ✅ | `is_create_intent`, `create_from_text`, `extract` |
| A4.4 | 常量 UPPER_SNAKE_CASE | ✅ | `CREATE_INTENT_KEYWORDS`, `NON_INTENT_INDICATORS` |
| A4.7 | 测试类 `Test`+被测类名 | ✅ | `TestIntentParser`, `TestEntityExtractor`, `TestConversationService` |
| A5.1 | 模块级 docstring | ✅ | `conversation.py:1-7` 模块文档 |
| A5.2 | 公开方法有 docstring | ✅ | 全部公开方法含 Args/Returns/Raises |
| A7.1 | public 方法有文档 | ✅ | 所有公开方法均有完整 docstring |
| E402 | 惰性导入打破 import 排序 | ⚠️ P2 | `main.py:131` 惰性导入 `ConversationService` 打破 PEP 8 import 顺序，已有 `# noqa: E402` 标注缓解 |

---

## 5. Step 4 — 可靠性检查

> 适配自蚂蚁编码军规 G1–G17 + 安全 S1–S10，针对 Python CLI 项目。

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| G1 并发控制 | `reliability-checklist.md` | N/A | — | 单用户 CLI 工具，无并发场景；设计已识别 read-modify-write 竞态（design.md L357-359） |
| G2 幂等拦截 | `reliability-checklist.md` | N/A | — | 无分布式系统 / MQ |
| G3 事务控制 | `reliability-checklist.md` | N/A | — | 无数据库事务 |
| G4 SQL与索引 | `reliability-checklist.md` | N/A | — | JSON 文件存储 |
| G5 消息（MQ） | `reliability-checklist.md` | N/A | — | 无消息队列 |
| G6 缓存 | `reliability-checklist.md` | N/A | — | 无缓存 |
| G7 调度任务 | `reliability-checklist.md` | N/A | — | 无定时任务 |
| G8 防御编程 | `reliability-checklist.md` | ✅ | — | G8.3: `save_tasks` 使用 `NamedTemporaryFile + os.replace` 原子写入 ✅；G8.1: 无吞异常 ✅；`conversation.py:152-156` 输入校验完整 |
| G9 网络调用 | `reliability-checklist.md` | N/A | — | 纯本地 CLI |
| G10 接口契约 | `reliability-checklist.md` | ✅ | — | 返回值统一为 `dict`，异常类型明确 |
| G11 开发自测 | `reliability-checklist.md` | ✅ | — | G11.1: 38 项新增测试覆盖全部公开接口 ✅；G11.2: 覆盖边界（空/空白/无名称/非法名称）✅；G11.3: `create_from_text` 入口有空值校验 ✅ |
| G12 资损防控 | `reliability-checklist.md` | N/A | — | 不涉及资金 |
| G13 监控核对 | `reliability-checklist.md` | N/A | — | CLI 工具，错误输出至 stderr |
| G14 国际化/多租户 | `reliability-checklist.md` | ✅ | — | G14.3: `created_at` 使用 UTC ISO8601 ✅（`main.py:71`） |
| G15 可灰度 | `reliability-checklist.md` | ✅ | — | `todo chat` 与 `todo add` 独立子命令，互不影响（design.md L447） |
| G16 可监控 | `reliability-checklist.md` | ✅ | — | 异常路径有日志输出（`main.py:121-125,136-139` stderr） |
| G17 可应急 | `reliability-checklist.md` | ✅ | — | 独立子命令可回退到 `todo add`（design.md L446-448） |
| S1–S10 安全 | `security-checklist.md` | N/A | — | 本地 CLI 工具，无 Web/网络暴露面 |

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | 未启用自定义规则 |

---

## 7. 结论

- **合并建议**：通过（修复 P1 项后合并更佳）
- **P0**：无
- **P1**：
  1. 意图识别实现与原始 design.md spec 存在行为偏差：改用「最早匹配」启发式代替原始 AND 逻辑。建议更新 design.md §5.1.1 的意图识别算法描述以保持 spec/impl 一致，或评估偏差带来的产品风险。
  2. `conversation.py` ↔ `main.py` 循环依赖通过惰性导入解决，但模块结构脆弱。建议将 `add_task`/`validate_name` 等共享函数抽取到独立模块（如 `src/todo_cli/task.py`）以根本消除循环依赖。
- **P2**：
  1. `EntityExtractor.extract` 正则仅捕获一个描述段，多段输入静默合并到描述字段。
  2. `ConversationService.create_from_text` 无 OSError 测试覆盖。
  3. `main.py:131` 惰性导入打破 PEP 8 E402（已有 `# noqa` 标注）。

---

## 7.1 问题片段

### P1-1: 意图识别偏差

- **P1** design.md §5.1.1 S02 — 实现与 spec 算法不一致：spec 要求「包含创建词 AND NOT 包含排除词」，实现使用「最早匹配」启发式。`"创建待办事项，完成后查看"` 在实现中返回 `True`，原始 spec 要求返回 `False`。

  片段范围：`src/todo_cli/conversation.py:47-83`

```python
L47|    def is_create_intent(self, text: str) -> bool:
L48|        """判断文本是否为创建任务意图。
L49|
L50|        策略：在文本中扫描所有关键词（创建词 + 排除词），
L51|        取出现位置最早的那个关键词：
L52|        - 若为创建词 → 接受为创建意图
L53|        - 若为排除词 → 拒绝
L54|        - 无匹配 → 拒绝
```

  对比 design.md L287 spec：`"基于关键词匹配：包含 CREATE_INTENT_KEYWORDS 任一关键词且不包含 NON_INTENT_INDICATORS 中明确排除词"`。实现选择「最早匹配」而非「AND 逻辑」，impl.md L56 已记录此决策。

### P1-2: 循环依赖

- **P1** `src/todo_cli/conversation.py:14` ↔ `src/todo_cli/main.py:131` — 模块间循环依赖，通过惰性导入规避。

  片段范围：`src/todo_cli/conversation.py:14`

```python
L14|from .main import add_task, validate_name
```

  片段范围：`src/todo_cli/main.py:129-131`

```python
L129|    if args.command == "chat":
L130|        text = " ".join(args.text)
L131|        from .conversation import ConversationService  # noqa: E402 惰性导入避免循环依赖
```

### P2-1: 多描述段合并

- **P2** `src/todo_cli/conversation.py:31-36` — 正则仅捕获单段描述，多段输入如 `"添加写周报，描述：周五前完成，备注：重要"` 会将 `"周五前完成，备注：重要"` 整体作为 description。

```python
L31|_NAME_PATTERN: str = (
L32|    r"(?:添加|创建|新增|记录|帮我记|代办|待办|todo|add|create|记一下|记录一下)"
L33|    r"\s*(?:一个|一下|个\s*)?"
L34|    r"(.+?)"
L35|    r"(?:\s*[，,]\s*(?:描述|备注|说明|内容)\s*[：:]\s*(.+))?$"
L36|)
```

### P2-2: OSError 路径未测

- **P2** `tests/test_conversation.py` — `TestConversationService` 8 项测试未覆盖 `OSError`（磁盘满）传播路径。

---

## 8. 修复任务列表

### P0

- 无待修复项。

### P1

- [ ] **P1** design.md §5.1.1 S02 — 更新意图识别算法描述，与实现「最早匹配」启发式对齐，或评估产品风险后决定是否修改实现。
- [ ] **P1** `src/todo_cli/conversation.py:14` / `src/todo_cli/main.py:131` — 将 `add_task`/`validate_name` 抽取到独立 `task.py` 模块，消除 conversation↔main 循环依赖。

### P2

- [ ] **P2** `src/todo_cli/conversation.py:31-36` — 评估多描述段需求；若需支持，扩展正则或改用分段解析。
- [ ] **P2** `tests/test_conversation.py` — 补充 `ConversationService.create_from_text` 的 `OSError` 传播测试（mock `save_tasks` 抛 `OSError`）。
- [ ] **P2** `src/todo_cli/main.py:131` — 循环依赖根除后移除 `# noqa: E402` 标注，恢复 import 至文件顶部。