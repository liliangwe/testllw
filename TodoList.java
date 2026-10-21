import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 待办列表。
 *
 * <p>持有待办事项集合，提供「新增」能力与只读查询方法，用于支撑最小闭环的验证。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoList {

    /** 内部存储的待办事项 */
    private final List<TodoItem> items = new ArrayList<TodoItem>();

    /**
     * 新增一条待办事项。
     *
     * @param item 待添加的待办事项
     */
    public void add(TodoItem item) {
        items.add(item);
    }

    /**
     * 便捷新增：按名称与描述创建并添加一条待办事项。
     *
     * @param name        事项名称
     * @param description 事项描述
     */
    public void add(String name, String description) {
        add(new TodoItem(name, description));
    }

    /**
     * 获取待办事项个数。
     *
     * @return 待办事项个数
     */
    public int size() {
        return items.size();
    }

    /**
     * 获取全部待办事项的只读视图。
     *
     * @return 待办事项的不可变列表
     */
    public List<TodoItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
