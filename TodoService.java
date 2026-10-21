import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 待办事项创建服务。
 *
 * <p>在进程内存中创建并保存待办事项，提供「新增待办事项」能力，
 * 并可通过 {@link #list()} 查询全部已创建的待办事项，用于验证「仅创建」闭环。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoService {

    /** 内存中的待办事项列表 */
    private final List<TodoItem> items = new ArrayList<>();

    /**
     * 以名称和描述创建一条待办事项并保存到内存。
     *
     * @param name        事项名称，不可为 null 或 trim 后为空
     * @param description 事项描述，不可为 null 或 trim 后为空
     * @return 创建后的待办事项对象
     * @throws IllegalArgumentException 当 name 或 description 为 null 或 trim 后为空时
     */
    public TodoItem create(String name, String description) {
        TodoItem item = new TodoItem(name, description);
        items.add(item);
        return item;
    }

    /**
     * 查询全部已创建的待办事项。
     *
     * @return 已创建待办事项的只读视图列表
     */
    public List<TodoItem> list() {
        return Collections.unmodifiableList(items);
    }
}