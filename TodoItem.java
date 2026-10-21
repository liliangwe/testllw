/**
 * 待办事项模型。
 *
 * <p>表示一条待办事项，仅含名称与描述两个字段，用于「创建待办事项」的最小闭环。</p>
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
     * @param name        事项名称
     * @param description 事项描述
     */
    public TodoItem(String name, String description) {
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
     * @return 事项描述
     */
    public String getDescription() {
        return description;
    }
}
