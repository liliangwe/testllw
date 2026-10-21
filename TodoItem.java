/**
 * 待办事项实体
 *
 * <p>表示一条待办事项，包含唯一标识 id、事项名称 name 与描述 description。
 * 对象创建后不可变（字段均为 final）。</p>
 *
 * @author AiWork
 */
public class TodoItem {

    /** 待办事项唯一标识 */
    private final long id;

    /** 事项名称（存储前已去除首尾空白） */
    private final String name;

    /** 事项描述（允许为 null 或空字符串） */
    private final String description;

    /**
     * 全参构造器
     *
     * @param id          待办事项唯一标识
     * @param name        事项名称
     * @param description 事项描述
     */
    public TodoItem(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * 获取待办事项唯一标识
     *
     * @return 待办事项唯一标识
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
     * 返回待办事项的可读字符串描述
     *
     * @return 形如 "TodoItem{id=1, name='示例事项', description='这是一个待办事项描述'}" 的字符串
     */
    @Override
    public String toString() {
        return "TodoItem{id=" + id + ", name='" + name + "', description='" + description + "'}";
    }
}
