# Review Package — Task 1（创建待办事项）

Base: HEAD（无既有实现，两文件为全新未跟踪新增）
Head: 工作区未提交（Git 只读，未 commit）

以下为本次变更的完整文件内容（均为新增文件）。

================================================================
新增文件: Todo.java
================================================================
/**
 * Todo 待办事项实体
 *
 * <p>表示一条待办事项，包含标题与可选的描述信息。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class Todo {

    /** 待办事项标题（必填非空） */
    private final String title;

    /** 待办事项描述（允许为 null 或空） */
    private final String description;

    /**
     * 构造一条待办事项
     *
     * <p>title 为空（null、空字符串或纯空白）时抛出 {@link IllegalArgumentException}，
     * description 允许为 null 或空。</p>
     *
     * @param title       待办事项标题，必填非空
     * @param description 待办事项描述，可为空
     * @throws IllegalArgumentException 当 title 为空时抛出
     */
    public Todo(String title, String description) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title 不能为空");
        }
        this.title = title;
        this.description = description;
    }

    /**
     * 获取待办事项标题
     *
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取待办事项描述
     *
     * @return 描述，可能为 null
     */
    public String getDescription() {
        return description;
    }

    /**
     * 返回待办事项的字符串表示
     *
     * @return 形如 Todo{title='...', description='...'} 的字符串
     */
    @Override
    public String toString() {
        return "Todo{title='" + title + "', description='" + description + "'}";
    }
}

================================================================
新增文件: TodoService.java
================================================================
import java.util.ArrayList;
import java.util.List;

/**
 * TodoService 待办事项服务
 *
 * <p>帮助用户记录日常待办事项，当前仅提供新增（创建）能力。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoService {

    /** 待办事项列表 */
    private final List<Todo> todos = new ArrayList<>();

    /**
     * 新增一条待办事项
     *
     * <p>委托 Todo 构造器校验 title，创建后加入列表并返回。</p>
     *
     * @param title       待办事项标题，必填非空
     * @param description 待办事项描述，可为空
     * @return 新创建的 Todo
     */
    public Todo addTodo(String title, String description) {
        Todo todo = new Todo(title, description);
        todos.add(todo);
        return todo;
    }

    /**
     * 返回当前待办事项数量
     *
     * @return 待办事项数量
     */
    public int size() {
        return todos.size();
    }
}