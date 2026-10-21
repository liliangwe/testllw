/**
 * 待办事项数据模型
 *
 * <p>表示一条待办事项，包含唯一标识、事项名称、事项描述与创建时间。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public final class TodoItem {

    /** 待办事项唯一标识 */
    private final long id;

    /** 事项名称（必填） */
    private final String name;

    /** 事项描述（选填，允许为空） */
    private final String description;

    /** 创建时间（epoch 毫秒） */
    private final long createdAt;

    /**
     * 构造一条待办事项。
     *
     * @param id          唯一标识
     * @param name        事项名称（必填，非空）
     * @param description 事项描述（可为 null 表示无描述）
     * @param createdAt   创建时间（epoch 毫秒）
     */
    public TodoItem(long id, String name, String description, long createdAt) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("事项名称不能为空");
        }
        this.id = id;
        this.name = name.trim();
        this.description = description;
        this.createdAt = createdAt;
    }

    /** @return 待办事项唯一标识 */
    public long getId() {
        return id;
    }

    /** @return 事项名称 */
    public String getName() {
        return name;
    }

    /** @return 事项描述（可能为 null） */
    public String getDescription() {
        return description;
    }

    /** @return 创建时间（epoch 毫秒） */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * 返回待办事项的可读字符串表示。
     *
     * @return 形如 "TodoItem{id=1, name='买菜', description='晚上买', createdAt=...}" 的字符串
     */
    @Override
    public String toString() {
        return "TodoItem{id=" + id
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", createdAt=" + createdAt + '}';
    }
}