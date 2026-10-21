import java.util.ArrayList;
import java.util.List;

/**
 * 待办事项服务。
 *
 * <p>负责待办事项的创建与记录。最小闭环仅支持新增（内存存储）。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoService {

    /** 内存中的待办事项列表 */
    private final List<TodoItem> todos = new ArrayList<>();

    /**
     * 新增一条待办事项。
     *
     * @param name        事项名称（必填，非空）
     * @param description 事项描述（可为空）
     * @return 创建成功后的待办事项
     */
    public TodoItem create(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("事项名称不能为空");
        }
        TodoItem item = new TodoItem(name, description);
        todos.add(item);
        return item;
    }
}
