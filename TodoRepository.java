import java.util.ArrayList;
import java.util.List;

/**
 * 待办事项的内存仓库。
 *
 * <p>仅提供创建（保存）能力，使用单调递增的 id 计数器为待办事项分配唯一标识。</p>
 *
 * @author AiWork
 */
public class TodoRepository {

    /** 内存中的待办事项列表。 */
    private final List<Todo> todos = new ArrayList<>();

    /** 单调递增的 id 计数器，从 1 开始。 */
    private long nextId = 1L;

    /**
     * 保存待办事项：分配下一个 id，加入列表并返回存储的待办事项。
     *
     * @param todo 待保存的待办事项
     * @return 已分配 id 的待办事项
     */
    public Todo save(Todo todo) {
        todo.setId(nextId);
        nextId++;
        todos.add(todo);
        return todo;
    }
}
