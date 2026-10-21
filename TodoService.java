/**
 * 待办事项业务逻辑。
 *
 * <p>提供创建待办事项的能力，负责入参校验与规范化。</p>
 *
 * @author AiWork
 */
public class TodoService {

    /** 待办事项仓库。 */
    private final TodoRepository repository;

    /**
     * 构造业务逻辑对象。
     *
     * @param repository 待办事项仓库
     */
    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    /**
     * 创建待办事项。
     *
     * <p>事项名称去除首尾空白后必须非空，否则抛出
     * {@link IllegalArgumentException}；描述为 null 时规范化为空字符串。</p>
     *
     * @param name        事项名称
     * @param description 描述，可为 null
     * @return 已创建（含已分配 id）的待办事项
     * @throws IllegalArgumentException 事项名称为 null 或去除首尾空白后为空
     */
    public Todo createTodo(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("事项名称不能为空");
        }
        String normalizedDescription = description == null ? "" : description;
        Todo todo = new Todo(name.trim(), normalizedDescription, System.currentTimeMillis());
        return repository.save(todo);
    }
}
