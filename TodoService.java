import java.util.ArrayList;
import java.util.List;

/**
 * 新增待办事项能力。
 *
 * <p>使用内存列表存储待办事项，提供创建能力。</p>
 *
 * @author AiWork
 */
public class TodoService {

    /** 内存存储的待办事项列表 */
    private final List<Todo> todos = new ArrayList<Todo>();

    /**
     * 创建一条待办事项并加入存储列表。
     *
     * @param title       事项名称；为 null、空串或纯空白时抛出异常
     * @param description 事项描述；为 null 时归一为空串
     * @return 创建成功的待办事项
     * @throws IllegalArgumentException 当 title 为 null 或去除首尾空白后为空时抛出
     */
    public Todo create(String title, String description) {
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
    }
}