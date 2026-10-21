/**
 * 待办事项的不可变值对象。
 *
 * <p>仅用于创建并保存一条待办事项，包含名称与描述字段，
 * 不支持查询、编辑、删除或持久化。</p>
 *
 * <p>该类不可变：所有字段均为 {@code final} 且不提供任何修改方法。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoItem {

    /** 事项名称，非 null 且非空白 */
    private final String name;

    /** 事项描述，可为 null 或空串，按原样保存 */
    private final String description;

    /**
     * 构造一个待办事项。
     *
     * @param name        事项名称，必须非 null 且非空白
     * @param description 事项描述，可为 null 或空串，按原样保存
     * @throws IllegalArgumentException 当 {@code name} 为 null 或仅包含空白字符时抛出
     */
    public TodoItem(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name 不能为 null 且不能为空白字符串");
        }
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
     * @return 事项描述，可能为 null 或空串
     */
    public String getDescription() {
        return description;
    }

    /**
     * 返回可读的字符串表示。
     *
     * <p>形如 {@code TodoItem{name='...', description='...'}}。</p>
     *
     * @return 可读的字符串表示
     */
    @Override
    public String toString() {
        return "TodoItem{name='" + name + "', description='" + description + "'}";
    }
}