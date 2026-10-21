import java.util.ArrayList;
import java.util.List;

/**
 * 待办事项服务
 *
 * <p>基于内存 {@link List} 存储待办事项。最小闭环仅提供新增能力，
 * 不提供 list/update/delete 接口。</p>
 *
 * @author AiWork
 */
public class TodoService {

    /** 内存中的待办事项存储 */
    private final List<TodoItem> items = new ArrayList<TodoItem>();

    /** 自增 id 计数器，从 1 开始 */
    private long nextId = 1L;

    /**
     * 新增一条待办事项
     *
     * @param name        事项名称，不能为 null 或去除首尾空白后为空
     * @param description 事项描述，允许为 null 或空字符串
     * @return 创建完成的待办事项
     * @throws IllegalArgumentException 当 name 为 null 或 trim 后为空时抛出，消息为「事项名称不能为空」
     */
    public TodoItem create(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("事项名称不能为空");
        }
        TodoItem item = new TodoItem(nextId++, name.trim(), description);
        items.add(item);
        return item;
    }
}
