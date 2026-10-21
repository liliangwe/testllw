# Task 1 报告：创建待办事项（Todo 最小闭环：仅创建）

## 实现了什么

按 brief 生成两个 Java 文件（默认包、UTF-8、K&R、4 空格缩进、行宽 ≤120、无 Tab）：

1. `TodoItem.java` — 不可变值对象：`long id` / `String name` / `String description`
   三字段（均 `final`）、构造器 `TodoItem(long id, String name, String description)`、
   三个 getter、覆写 `toString()`。
2. `TodoService.java` — 内存存储：`List<TodoItem> todos = new ArrayList<>()`、
   `long nextId = 1L`；唯一公开方法 `create(String name, String description)`。
   - `name` 为 null 或 trim 后为空 → 抛 `IllegalArgumentException`
   - `description` 允许 null/空，原样保存
   - `nextId` 先取当前值作为 id，再自增，将新 TodoItem 加入列表并返回。

仅实现「创建」，未添加查询/删除/更新/编辑等任何多余功能（YAGNI）。

## 静态自审结果

- 环境无 JDK（无 java/javac），按要求**未编译、未运行**；无测试文件，**未写测试**；**未执行任何 Git 写操作**。
- 自审方式：逐条对照 brief 与逐行阅读源码。
- 结论：**通过**。语法、命名、可见性、返回值、异常语义均与验收标准一致；无多余成员、无 package 声明、无 import 缺失。

## 文件清单（相对仓库根目录）

- `TodoItem.java`（新建）
- `TodoService.java`（新建）

## 验收标准对照

1. **仅「创建」能力，无多余功能**
   - `TodoService` 仅公开 `create`，无 list/get/delete/update/edit 方法；`TodoItem` 仅 getter + toString，无 setter/业务方法。→ 符合。
2. **`create` 返回的 TodoItem 具备 id/name/description，且已写入内存列表**
   - 构造 `TodoItem(nextId, name, description)`，`todos.add(item)` 后 `return item`；getter 覆盖三个字段。→ 符合。
3. **name 缺失（null 或空白）抛 IllegalArgumentException**
   - `if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException(...)`。→ 符合。

## 顾虑

- 无 JDK 且无测试文件，无法以编译/运行/测试验证，验收仅靠静态代码审查（与 brief 要求一致）。
- `todos` 与 `nextId` 除 `final todos` 引用外为普通私有字段，未做显式线程安全处理，brief 未要求并发场景，故未处理。