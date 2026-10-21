/**
 * 待办事项实体。
 *
 * <p>表示一条待办事项，包含事项名称与描述。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoItem {

    /** 事项名称 */
    private final String name;

    /** 事项描述 */
    private final String description;

    /**
     * 构造一条待办事项。
     *
     * @param name        事项名称（必填，非空）
     * @param description 事项描述（可为空）
     */
    public TodoItem(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("事项名称不能为空");
        }
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
    }

    /** 获取事项名称 */
    public String getName() {
        return name;
    }

    /** 获取事项描述 */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "TodoItem{name='" + name + "', description='" + description + "'}";
    }
}
