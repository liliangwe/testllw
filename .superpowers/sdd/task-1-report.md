# Task 1 报告：待办事项（Todo）创建功能

## 状态

DONE_WITH_CONCERNS（唯一 concern：环境无 javac/java，无法编译验证，已按 brief 降级为静态审查）

## 实现内容

按 brief 要求实现「仅创建」最小闭环，共两个文件，沿用现有仓库无 package 声明的风格。

### TodoItem.java（领域模型）

- 字段：`name`（事项名称）、`description`（事项描述），均 `private final`（不可变）。
- 构造器：对 `name`、`description` 做 null / trim 后空白的校验，不合法抛 `IllegalArgumentException`。
- getter：`getName()`、`getDescription()`。
- 为使后续断言可行，提供 `equals` / `hashCode` / `toString`（brief 明确允许）。
- public 类与方法均写 Javadoc。

### TodoService.java（创建服务）

- 内存存储：`private final List<TodoItem> items = new ArrayList<>()`。
- `TodoItem create(String name, String description)`：委托模型校验，校验通过后写入内存并返回该对象。
- `List<TodoItem> list()`：返回 `Collections.unmodifiableList(items)` 只读视图，用于验证「仅创建」闭环。

## 文件变更

- 新建 `.superpowers/sdd/task-1-report.md`（本报告）
- 新建 `TodoItem.java`
- 新建 `TodoService.java`

## 边界情况处理

- `name == null` → 抛 `IllegalArgumentException`。
- `description == null` → 抛 `IllegalArgumentException`。
- `name` 为 `""` 或全空白（如 `"   "`）→ trim 后为空，抛 `IllegalArgumentException`。
- `description` 为 `""` 或全空白 → 同上。
- `name` / `description` 前后带空格但其余非空 → trim 后非空，校验通过（按 brief「trim 后为空视为空白」字面要求）。
- `list()` 返回不可修改视图，防止外部破坏内部存储（YAGNI 范围内仍保持最小安全，未引入并发结构）。
- `equals` 覆盖 `instanceof` 判断，避免与无关对象比较时抛 `ClassCastException`。

## 静态自审查（因无 javac 编译器）

1. 类型正确性：两个文件仅在无 package 的文件间互引，`TodoService` 内 `new TodoItem(...)`、`List<TodoItem>` 与 `TodoItem` 的 getter/构造器签名一致，无类型不匹配。
2. 语法：逐行核对，无缺括号、缺分号、多余的 token；`import java.util.*` 均被使用（`Objects`、`ArrayList`、`Collections`、`List`）。
3. public 类与 public 方法均有 Javadoc。
4. 命名与格式：类名 UpperCamelCase、方法/字段 lowerCamelCase、K&R 大括号、4 空格缩进。
5. 无越界 YAGNI：未实现 delete / update / complete / 持久化 / REST / 并发结构 / 自定义异常 / 依赖注入框架。

## 验证命令与结果

- `command -v javac; command -v java` → 退出码 1，均未找到，确认环境无 JDK 编译/运行时。
- 按 brief 硬性约束「环境无 javac：编译验证降级为静态审查」，因此未阻塞交付，仅以以上静态审查作为替代验证。**这是本任务唯一的验证方式，未能进行真实编译/运行级别验证**，请父级知悉这一局限。

## 关注事项（concerns）

1. 无 javac/java：无法编译与运行，类型/语法正确性仅基于静态人工审查，未经编译器确认。
2. `list()` 返回只读视图而非新副本；在单线程、仅创建的场景下正确，但若未来调用方持旧引用试图写会导致 `UnsupportedOperationException`。这与 YAGNI 目标一致，此处记录以备后续扩展。
3. 校验消息为中文文案，写死在模型与服务层；校验本身强一致（模型为唯一校验点），未引入自定义异常体系。

## 未做事项

- 未执行任何 git 写命令（commit/push/merge/reset/rebase/checkout 等均禁止）。
- 未创建测试文件或 main，brief 明确禁止在无测试框架的仓库中添加测试。