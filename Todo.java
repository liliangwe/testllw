/**
 * Todo 待办事项领域模型。
 *
 * <p>事项包含两个字段：事项名称（name）与事项描述（description）。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class Todo {

    /** 事项名称 */
    private final String name;

    /** 事项描述 */
    private final String description;

    /**
     * 构造一个待办事项。
     *
     * @param name        事项名称
     * @param description 事项描述（可为 null）
     */
    public Todo(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 获取事项名称。
     *
     * @return 事项名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取事项描述。
     *
     * @return 事项描述（可为 null）
     */
    public String getDescription() {
        return description;
    }
}
