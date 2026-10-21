import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 待办事项服务
 *
 * <p>负责待办事项的新增与查询。当前采用内存存储，仅提供「新增」这一最小闭环能力。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public final class TodoService {

    /** 内存存储的待办事项列表 */
    private final List<TodoItem> items = new ArrayList<>();

    /** 自增 id 序列，从 1 开始 */
    private long nextId = 1L;

    /**
     * 新增一条待办事项。
     *
     * @param name        事项名称（必填，非空）
     * @param description 事项描述（选填，可为 null）
     * @return 创建成功的待办事项（含生成的唯一 id 与创建时间）
     * @throws IllegalArgumentException 当事项名称为 null 或空白时抛出
     */
    public TodoItem create(String name, String description) {
        TodoItem item = new TodoItem(nextId++, name, description, System.currentTimeMillis());
        items.add(item);
        return item;
    }

    /**
     * 查询全部待办事项。
     *
     * @return 待办事项的只读视图（按创建顺序）
     */
    public List<TodoItem> listAll() {
        return Collections.unmodifiableList(items);
    }
}