import java.util.ArrayList;
import java.util.List;

/**
 * TodoService 最小服务：仅提供「新增待办事项（create）」能力。
 *
 * <p>使用内存 List 存储已创建的待办事项，不做查询、更新、删除、持久化。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoService {

    /** 内存存储：仅记录已创建的待办事项 */
    private final List<Todo> todos = new ArrayList<Todo>();

    /**
     * 新增待办事项。
     *
     * <p>名称（name）为 null 或空白（trim 后为空）时拒绝创建并抛出
     * {@link IllegalArgumentException}；描述（description）允许为 null 或空。</p>
     *
     * @param name        事项名称（不可为 null/空白）
     * @param description 事项描述（允许为 null/空）
     * @return 包含入参 name、description 的新建 Todo 实例
     * @throws IllegalArgumentException 当 name 为 null 或空白时
     */
    public Todo create(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("事项名称不能为 null 或空白");
        }
        Todo todo = new Todo(name, description);
        todos.add(todo);
        return todo;
    }

    /**
     * 返回当前已创建的待办事项数量（便于验证）。
     *
     * @return 已创建事项数量
     */
    public int size() {
        return todos.size();
    }
}
