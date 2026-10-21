import java.util.Objects;

/**
 * 待办事项。
 *
 * <p>一个待办事项包含唯一标识、事项名称、描述及创建时间。对象在保存前处于
 * 未持久化状态（id 为 0），保存时由仓库分配并回填 id。</p>
 *
 * @author AiWork
 */
public class Todo {

    /** 唯一标识，保存前为 0，由仓库分配。 */
    private long id;

    /** 事项名称。 */
    private final String name;

    /** 描述。 */
    private final String description;

    /** 创建时间（epoch 毫秒）。 */
    private final long createdAtMillis;

    /**
     * 构造待办事项。
     *
     * @param name            事项名称
     * @param description     描述
     * @param createdAtMillis 创建时间（epoch 毫秒）
     */
    public Todo(String name, String description, long createdAtMillis) {
        this.id = 0L;
        this.name = name;
        this.description = description;
        this.createdAtMillis = createdAtMillis;
    }

    /**
     * 返回唯一标识。
     *
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * 返回事项名称。
     *
     * @return 事项名称
     */
    public String getName() {
        return name;
    }

    /**
     * 返回描述。
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 返回创建时间（epoch 毫秒）。
     *
     * @return 创建时间
     */
    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    /**
     * 设置唯一标识（由仓库在保存时调用）。
     *
     * @param id 唯一标识
     */
    void setId(long id) {
        this.id = id;
    }

    /**
     * 基于所有字段判断是否相等。
     *
     * @param o 待比较对象
     * @return 相等返回 true，否则 false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Todo)) {
            return false;
        }
        Todo todo = (Todo) o;
        return id == todo.id
                && createdAtMillis == todo.createdAtMillis
                && Objects.equals(name, todo.name)
                && Objects.equals(description, todo.description);
    }

    /**
     * 基于所有字段计算哈希值。
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, createdAtMillis);
    }

    /**
     * 返回可读的字符串表示。
     *
     * @return 形如 Todo{id=..., name=..., description=...} 的字符串
     */
    @Override
    public String toString() {
        return "Todo{id=" + id + ", name=" + name + ", description=" + description + "}";
    }
}
