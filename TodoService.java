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
