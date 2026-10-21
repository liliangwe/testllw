/**
 * 待办事项实体。
 * <p>记录一条待办事项的名称与描述。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoItem {

    private final long id;
    private final String name;
    private final String description;
    private final long createdAtMillis;

    public TodoItem(long id, String name, String description, long createdAtMillis) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAtMillis = createdAtMillis;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }
}
