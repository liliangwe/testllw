import java.util.ArrayList;
import java.util.List;

/**
 * 待办事项服务。
 * <p>支持新增待办事项（最小闭环：仅创建）。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoService {

    private final List<TodoItem> items = new ArrayList<TodoItem>();
    private long nextId = 1L;

    /**
     * 新增待办事项。
     *
     * @param name        事项名称（必填，不能为 blank）
     * @param description 事项描述（可为空，空值归一化为空串）
     * @return 新建的待办事项
     */
    public TodoItem add(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("事项名称不能为空");
        }
        TodoItem item = new TodoItem(
                nextId++,
                name,
                description == null ? "" : description,
                System.currentTimeMillis());
        items.add(item);
        return item;
    }

    /**
     * 返回当前全部待办事项的快照（按创建顺序）。
     */
    public List<TodoItem> list() {
        return new ArrayList<TodoItem>(items);
    }
}
