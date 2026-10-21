# Task 1: 创建待办事项（Todo 最小闭环：仅创建）

## 需求原文
目标：帮助用户记录日常待办事项。
- 核心功能：新增待办事项
- 任务信息：事项名称和描述
- 目标用户：内部用户
- 最小闭环：仅创建

## 技术决策（控制器已定，不得更改）
- 语言：Java（与仓库现有 `HelloWorld.java` 一致，无 package，默认包）
- 环境无 JDK（无 java/javac），验证降级为静态代码审查，禁止尝试编译或运行
- 不新增任何测试文件（仓库现无测试，且无 JDK）
- Git 只读：禁止 commit/add 等任何 Git 写操作，文件保持未提交状态

## 交付文件（相对仓库根目录，仅这两个文件，禁止其他文件）
### 1. TodoItem.java — 待办事项实体（不可变值对象）
- 字段：`long id`、`String name`、`String description`
- 构造器：`TodoItem(long id, String name, String description)`
- getter：`getId()` / `getName()` / `getDescription()`
- 覆写 `toString()`

### 2. TodoService.java — 待办事项服务（内存存储）
- 私有字段：`List<TodoItem> todos = new ArrayList<>()`；`long nextId = 1L`
- 唯一公开方法：`public TodoItem create(String name, String description)`
  - `name` 为 null 或 trim 后为空 → 抛 `IllegalArgumentException`
  - `description` 允许为 null/空，原样保存
  - 使用 `nextId` 自增分配 id，将新 TodoItem 加入列表，返回该 TodoItem

## 代码风格（遵循现有 HelloWorld.java）
- K&R 大括号、4 空格缩进、行宽 ≤120
- public 类与方法均带 Javadoc（含 `@param` / `@return` 顺序正确）
- 类名 UpperCamelCase、方法名 lowerCamelCase、UTF-8、无 Tab

## 验收标准
- 仅「创建」能力，无多余功能（不实现列表查询/删除/更新/编辑等）
- `create` 返回的 `TodoItem` 具备 id/name/description，且已写入内存列表
- name 缺失（null 或空白）时抛 IllegalArgumentException