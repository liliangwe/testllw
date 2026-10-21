# Review Package — Task 1

Base: e0ef4ab  Head: e0ef4ab (新文件未提交，diff 以「空文件 → 新增」呈现)

## 文件清单（本次新增，均为未跟踪）
- TodoItem.java
- TodoList.java
- TodoApp.java
- .agents/sdd-todo/task-1-report.md

## 变更统计（新增行数）
  44 TodoItem.java
  53 TodoList.java
  40 TodoApp.java
 137 total

## 完整 diff（空 → 新增）

########## TodoItem.java ##########
diff --git a/TodoItem.java b/TodoItem.java
new file mode 100644
index 0000000..728325a
--- /dev/null
+++ b/TodoItem.java
@@ -0,0 +1,45 @@
+/**
+ * 待办事项模型。
+ *
+ * <p>表示一条待办事项，仅含名称与描述两个字段，用于「创建待办事项」的最小闭环。</p>
+ *
+ * @author AiWork
+ * @date 2026/10/21
+ */
+public class TodoItem {
+
+    /** 事项名称 */
+    private final String name;
+
+    /** 事项描述 */
+    private final String description;
+
+    /**
+     * 构造一条待办事项。
+     *
+     * @param name        事项名称
+     * @param description 事项描述
+     */
+    public TodoItem(String name, String description) {
+        this.name = name;
+        this.description = description;
+    }
+
+    /**
+     * 获取事项名称。
+     *
+     * @return 事项名称
+     */
+    public String getName() {
+        return name;
+    }
+
+    /**
+     * 获取事项描述。
+     *
+     * @return 事项描述
+     */
+    public String getDescription() {
+        return description;
+    }
+}
\ No newline at end of file

########## 完整内容 TodoItem.java ##########
/**
 * 待办事项模型。
 *
 * <p>表示一条待办事项，仅含名称与描述两个字段，用于「创建待办事项」的最小闭环。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoItem {

    /** 事项名称 */
    private final String name;

    /** 事项描述 */
    private final String description;

    /**
     * 构造一条待办事项。
     *
     * @param name        事项名称
     * @param description 事项描述
     */
    public TodoItem(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 获取事项名称。
     *
     * @return 事项名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取事项描述。
     *
     * @return 事项描述
     */
    public String getDescription() {
        return description;
    }
}
########## TodoList.java ##########
diff --git a/TodoList.java b/TodoList.java
new file mode 100644
index 0000000..c8241a0
--- /dev/null
+++ b/TodoList.java
@@ -0,0 +1,54 @@
+import java.util.ArrayList;
+import java.util.Collections;
+import java.util.List;
+
+/**
+ * 待办列表。
+ *
+ * <p>持有待办事项集合，提供「新增」能力与只读查询方法，用于支撑最小闭环的验证。</p>
+ *
+ * @author AiWork
+ * @date 2026/10/21
+ */
+public class TodoList {
+
+    /** 内部存储的待办事项 */
+    private final List<TodoItem> items = new ArrayList<TodoItem>();
+
+    /**
+     * 新增一条待办事项。
+     *
+     * @param item 待添加的待办事项
+     */
+    public void add(TodoItem item) {
+        items.add(item);
+    }
+
+    /**
+     * 便捷新增：按名称与描述创建并添加一条待办事项。
+     *
+     * @param name        事项名称
+     * @param description 事项描述
+     */
+    public void add(String name, String description) {
+        add(new TodoItem(name, description));
+    }
+
+    /**
+     * 获取待办事项个数。
+     *
+     * @return 待办事项个数
+     */
+    public int size() {
+        return items.size();
+    }
+
+    /**
+     * 获取全部待办事项的只读视图。
+     *
+     * @return 待办事项的不可变列表
+     */
+    public List<TodoItem> getItems() {
+        return Collections.unmodifiableList(items);
+    }
+}
\ No newline at end of file

########## 完整内容 TodoList.java ##########
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 待办列表。
 *
 * <p>持有待办事项集合，提供「新增」能力与只读查询方法，用于支撑最小闭环的验证。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoList {

    /** 内部存储的待办事项 */
    private final List<TodoItem> items = new ArrayList<TodoItem>();

    /**
     * 新增一条待办事项。
     *
     * @param item 待添加的待办事项
     */
    public void add(TodoItem item) {
        items.add(item);
    }

    /**
     * 便捷新增：按名称与描述创建并添加一条待办事项。
     *
     * @param name        事项名称
     * @param description 事项描述
     */
    public void add(String name, String description) {
        add(new TodoItem(name, description));
    }

    /**
     * 获取待办事项个数。
     *
     * @return 待办事项个数
     */
    public int size() {
        return items.size();
    }

    /**
     * 获取全部待办事项的只读视图。
     *
     * @return 待办事项的不可变列表
     */
    public List<TodoItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
########## TodoApp.java ##########
diff --git a/TodoApp.java b/TodoApp.java
new file mode 100644
index 0000000..6167d4c
--- /dev/null
+++ b/TodoApp.java
@@ -0,0 +1,41 @@
+/**
+ * 待办事项演示程序。
+ *
+ * <p>程序入口，演示「创建待办事项」的最小闭环：创建列表、新增两条事项、打印列表，
+ * 并在 main 中做简单断言。</p>
+ *
+ * @author AiWork
+ * @date 2026/10/21
+ */
+public class TodoApp {
+
+    /**
+     * 程序入口方法。
+     *
+     * @param args 命令行参数（未使用）
+     */
+    public static void main(String[] args) {
+        TodoList list = new TodoList();
+
+        list.add("购买牛奶", "下班后去超市购买一盒牛奶");
+        list.add("阅读文档", "阅读待办事项任务简报，理解验收标准");
+
+        // 断言 1：新增后的 size 符合预期
+        assert list.size() == 2 : "expected size 2, but was " + list.size();
+
+        // 断言 2：打印内容包含所加事项
+        StringBuilder printed = new StringBuilder();
+        for (TodoItem item : list.getItems()) {
+            printed.append(item.getName());
+            printed.append(":");
+            printed.append(item.getDescription());
+            printed.append(System.lineSeparator());
+        }
+        String content = printed.toString();
+        assert content.contains("购买牛奶") : "missing item 购买牛奶";
+        assert content.contains("阅读文档") : "missing item 阅读文档";
+
+        System.out.println("待办事项列表（共 " + list.size() + " 条）：");
+        System.out.print(content);
+    }
+}
\ No newline at end of file

########## 完整内容 TodoApp.java ##########
/**
 * 待办事项演示程序。
 *
 * <p>程序入口，演示「创建待办事项」的最小闭环：创建列表、新增两条事项、打印列表，
 * 并在 main 中做简单断言。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoApp {

    /**
     * 程序入口方法。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        TodoList list = new TodoList();

        list.add("购买牛奶", "下班后去超市购买一盒牛奶");
        list.add("阅读文档", "阅读待办事项任务简报，理解验收标准");

        // 断言 1：新增后的 size 符合预期
        assert list.size() == 2 : "expected size 2, but was " + list.size();

        // 断言 2：打印内容包含所加事项
        StringBuilder printed = new StringBuilder();
        for (TodoItem item : list.getItems()) {
            printed.append(item.getName());
            printed.append(":");
            printed.append(item.getDescription());
            printed.append(System.lineSeparator());
        }
        String content = printed.toString();
        assert content.contains("购买牛奶") : "missing item 购买牛奶";
        assert content.contains("阅读文档") : "missing item 阅读文档";

        System.out.println("待办事项列表（共 " + list.size() + " 条）：");
        System.out.print(content);
    }
}