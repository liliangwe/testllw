# Task 1: 待办事项（Todo）创建功能

## 需求原文（verbatim）

创建待办事项。目标：帮助用户记录日常待办事项。核心功能：新增待办事项。
任务信息：事项名称和描述。目标用户：内部用户。最小闭环：仅创建。

## 验收标准（精确，逐条满足，不得增减）

1. 待办事项须包含两个字段：
   - 事项名称（名称）
   - 事项描述（描述）
   两者均缺失校验：创建时名称与描述均不得为 null 或空白（trim 后为空视为空白）。
2. 提供「新增待办事项」能力：以名称 + 描述创建一条待办事项，返回创建后的待办事项对象。
3. 创建后的待办事项必须能够被查询到（内存存储即可，用于验证「仅创建」闭环）。
4. 技术约定（沿用现有仓库风格）：
   - Java 语言（仓库现有 HelloWorld.java，无构建工具，无 pom/gradle）。
   - 单一职责：领域模型类 + 服务类分文件。
   - 命名规范：UpperCamelCase 类名 / lowerCamelCase 方法与字段；K&R 大括号；4 空格缩进。
   - public 类与方法均写 Javadoc。
5. 范围外（YAGNI，禁止实现）：不做删除、不做修改、不做完成标记、不做持久化到磁盘/数据库、不做 Web/REST 接口、不做并发结构、不做自定义异常体系、不做依赖注入框架、不做任何未被本 brief 要求的东西。

## 建议文件组织（遵循，如因上下文需要调整须在报告中说明）

- `TodoItem.java` —— 待办事项领域模型：字段 name（名称）、description（描述）；构造器；getter；equals/hashCode/toString（便于测试断言，允许）。
- `TodoService.java` —— 创建服务：`TodoItem create(String name, String description)`，存储到内存 `List<TodoItem>`；提供 `List<TodoItem> list()` 查询全部；空/空白校验不合法时抛出 `IllegalArgumentException`。

## 关键决策（已由控制器定夺，勿再提问）

- 语言：Java（纯 JDK，无第三方依赖、无构建工具）。
- 存储：进程内存。
- 校验失败行为：抛 `IllegalArgumentException`（不引入自定义异常）。
- 不引入 package 声明（沿用现有 HelloWorld.java 无 package 的风格）。

## 约束（硬性）

- Git 只读：禁止 commit / push / merge / reset / rebase / checkout 等任何写操作。
- 禁止使用 grep / glob 命令。
- 产物必须写入 cwd（仓库根目录）下。
- 环境无 javac/maven/gradle：编译验证降级为静态审查（读代码核对类型、语法、边界），不得因无法编译而阻塞交付。