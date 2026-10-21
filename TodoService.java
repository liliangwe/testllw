import java.util.ArrayList;
import java.util.List;

/**
 * TodoService 待办事项服务
 *
 * <p>基于内存存储的待办事项服务，当前仅支持创建能力。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoService {

    /** 内存存储的待办事项列表 */
    private final List<TodoItem> todos = new ArrayList<>();

    /** 下一个待分配的唯一标识 */
    private long nextId = 1L;

    /**
     * 创建待办事项
     *
     * <p>使用自增 id 分配唯一标识，将新事项加入内存列表并返回。name 为
     * null 或去除两端空白后为空时抛出 IllegalArgumentException；description
     * 允许为 null 或空，原样保存。</p>
     *
     * @param name        事项名称
     * @param description 事项描述
     * @return 创建的待办事项
     * @throws IllegalArgumentException name 为 null 或去除两端空白后为空
     */
    public TodoItem create(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name 不能为 null 或空白");
        }
        TodoItem item = new TodoItem(nextId, name, description);
        nextId++;
        todos.add(item);
        return item;
    }
}