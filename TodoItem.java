import java.util.Objects;

/**
 * 待办事项领域模型。
 *
 * <p>一个待办事项由事项名称（name）和事项描述（description）组成，
 * 两者均不可为 null 或空白。本类为不可变对象。</p>
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
     * 创建一条待办事项。
     *
     * @param name        事项名称，不可为 null 或 trim 后为空
     * @param description 事项描述，不可为 null 或 trim 后为空
     * @throws IllegalArgumentException 当 name 或 description 为 null 或 trim 后为空时
     */
    public TodoItem(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("事项名称不得为 null 或空白");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("事项描述不得为 null 或空白");
        }
        this.name = name;
        this.description = description;
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
     * 返回事项描述。
     *
     * @return 事项描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 判断两个待办事项是否相等（名称与描述均相同）。
     *
     * @param other 待比较的对象
     * @return 相等返回 true，否则返回 false
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TodoItem)) {
            return false;
        }
        TodoItem that = (TodoItem) other;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description);
    }

    /**
     * 返回基于名称与描述的哈希值。
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }

    /**
     * 返回待办事项的可读字符串表示。
     *
     * @return 形如 "TodoItem[name=..., description=...]" 的字符串
     */
    @Override
    public String toString() {
        return "TodoItem[name=" + name + ", description=" + description + "]";
    }
}