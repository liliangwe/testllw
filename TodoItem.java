import java.time.Instant;

/**
 * 待办事项实体
 *
 * <p>表示一条待办事项，包含全局自增编号（id）、事项名称（name）、
 * 事项描述（description）与创建时间（createdAt）。</p>
 *
 * @author AiWork
 */
public class TodoItem {

    /** 全局自增序号生成器 */
    private static long nextId = 1L;

    /** 待办事项编号 */
    private final long id;

    /** 事项名称（必填） */
    private final String name;

    /** 事项描述（允许为空，统一 trim） */
    private final String description;

    /** 创建时间 */
    private final Instant createdAt;

    /**
     * 构造一条待办事项
     *
     * @param name        事项名称，null 或空白将抛出 IllegalArgumentException
     * @param description 事项描述，允许为 null 或空白，统一 trim
     */
    TodoItem(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name 不能为 null 或空白");
        }
        this.id = nextId++;
        this.name = name;
        this.description = description == null ? "" : description.trim();
        this.createdAt = Instant.now();
    }

    /**
     * 获取待办事项编号
     *
     * @return 编号
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
     * @return 事项描述（可为空字符串）
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 返回待办事项的字符串表示
     *
     * @return 待办事项字符串
     */
    @Override
    public String toString() {
        return "TodoItem{id=" + id + ", name=\"" + name + "\", description=\"" + description + "\"}";
    }
}
