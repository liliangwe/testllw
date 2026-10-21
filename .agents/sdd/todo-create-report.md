# 实施报告：创建待办事项（最小闭环）

## 一、状态

DONE

## 二、交付文件清单

| 文件（仓库根目录相对路径） | 说明 |
| --- | --- |
| `Todo.java` | 待办事项模型：`private final String title`、`private final String description`；构造器 `Todo(String title, String description)`；getter `getTitle()`、`getDescription()`；附带可选 `toString()`。 |
| `TodoService.java` | 新增待办事项能力：`private final List<Todo> todos = new ArrayList<Todo>()`；方法 `Todo create(String title, String description)`，校验 title、归一 description、构造 Todo、加入列表并返回。 |
| `TodoApp.java` | 程序入口：`main` 中新建 `TodoService`，`create("示例事项", "示例描述")`，向标准输出打印 title 与 description，正常退出（无 `System.exit`，自然返回码 0）。 |

未修改 `HelloWorld.java`、`README.md` 或任何既有文件；未创建清单外文件。

## 三、关键实现说明

### TodoService.create 校验逻辑（逐字遵循简报）

```java
if (title == null) {
    throw new IllegalArgumentException("待办事项名称（title）不能为 null");
}
if (title.trim().isEmpty()) {
    throw new IllegalArgumentException("待办事项名称（title）不能为空或纯空白");
}
String normalizedDescription = description == null ? "" : description;
Todo todo = new Todo(title, normalizedDescription);
todos.add(todo);
return todo;
```

- `title` 为 `null`：抛出 `IllegalArgumentException`。
- `title` 为空串 / 纯空白：`title.trim().isEmpty()` 为 true，抛出 `IllegalArgumentException`（覆盖空串与纯空白两类）。
- `description` 为 `null`：归一为 `""`。
- 创建成功：构造新 `Todo`，加入 `todos` 列表并返回该 `Todo`。

### 代码风格合规

- 单文件 public 类同名：`Todo` / `TodoService` / `TodoApp` 与文件名一致。
- K&R 大括号、4 空格缩进、无 Tab、UTF-8、行宽 ≤120。
- 类名 UpperCamelCase；方法与变量 lowerCamelCase。
- public 类与 public 方法均有 Javadoc。

## 四、静态自审结论

因环境无 JDK（`javac`、`java` 均不可用），无法编译及运行验证，改为静态自审，结论如下：

1. **括号/圆括号配平**：`Todo.java` `{=6 }=6 (=4 )=4`、`TodoService.java` `{=4 }=4 (=10 )=10`、`TodoApp.java` `{=2 }=2 (=7 )=7`，均配平。
2. **import 正确**：仅 `TodoService.java` 有 `import java.util.ArrayList;` 与 `import java.util.List;`，其余两文件无 import（均为同包引用，无需 import）。无未使用依赖、无第三方依赖。
3. **方法签名一致**：
   - `Todo`：`public Todo(String title, String description)`、`public String getTitle()`、`public String getDescription()`、`public String toString()`。
   - `TodoService`：`public Todo create(String title, String description)`。
   - `TodoApp`：`public static void main(String[] args)`。
4. **类型一致**：`title`、`description`、`getTitle()`、`getDescription()` 均为 `String`；`todos` 为 `List<Todo>`，`create` 返回 `Todo`；`todos.add(todo)` 与 `new Todo(title, normalizedDescription)` 参数类型匹配。
5. **public 类与文件名一致**：`public class Todo` ↔ `Todo.java`；`public class TodoService` ↔ `TodoService.java`；`public class TodoApp` ↔ `TodoApp.java`。
6. **无 Tab、行宽 ≤120**：`rg '\t'` 无匹配；`awk 'length($0)>120'` 无匹配。
7. **边界行为核对**（逐条对照代码）：
   - title 为 `null` → 抛出 `IllegalArgumentException` ✅
   - title 空串 / 纯空白 → `trim().isEmpty()` 为 true，抛出异常 ✅
   - description 为 `null` → 归一为 `""` ✅
   - create 成功 → 返回新 `Todo` 且已 `todos.add(todo)` ✅

## 五、测试验证结论

未能编译验证，原因：环境无 JDK，`javac` 与 `java` 命令均不存在（`command -v javac`、`command -v java` 均 NOT FOUND）。按简报要求未尝试编译或安装 JDK，仅做上述静态自审。

## 六、剩余风险 / 关切点

- 无法通过编译与运行消除潜在语法/类型错误，静态自审为唯一验证手段。
- `TodoApp.main` 未调用 `System.exit(0)`，依赖自然返回，符合「正常退出返回码 0」语义；如运行环境对显式退出码有强校验，可后续补充。