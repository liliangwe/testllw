/**
 * TodoItem 待办事项实体
 *
 * <p>不可变值对象，表示一条待办事项，包含唯一标识、名称与描述。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoItem {

    /** 待办事项唯一标识 */
    private final long id;

    /** 待办事项名称 */
    private final String name;

    /** 待办事项描述 */
    private final String description;

    /**
     * 构造待办事项
     *
     * @param id          唯一标识
     * @param name        事项名称
     * @param description 事项描述
     */
    public TodoItem(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * 获取唯一标识
     *
     * @return 唯一标识
     */
    public long getId() {
        return id;
    }

    /**
     * 获取事项名称
     *
     * @return 事项名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取事项描述
     *
     * @return 事项描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 返回待办事项的字符串表示
     *
     * @return 待办事项的字符串表示
     */
    @Override
    public String toString() {
        return "TodoItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}