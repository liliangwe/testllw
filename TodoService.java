import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 待办事项服务
 *
 * <p>提供待办事项的新增（create）与只读查询（listAll）能力。</p>
 *
 * @author AiWork
 */
public class TodoService {

    /** 当前全部待办事项 */
    private final List<TodoItem> items = new ArrayList<>();

    /**
     * 新增一条待办事项
     *
     * @param name        事项名称（必填，null 或空白将抛出 IllegalArgumentException）
     * @param description 事项描述（允许为空）
     * @return 新建的待办事项
     */
    public TodoItem create(String name, String description) {
        TodoItem item = new TodoItem(name, description);
        items.add(item);
        return item;
    }

    /**
     * 以只读视图返回当前全部待办事项
     *
     * @return 当前全部待办事项的只读列表
     */
    public List<TodoItem> listAll() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }
}
