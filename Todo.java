/**
 * Todo 待办事项实体
 *
 * <p>表示一条待办事项，包含标题与可选的描述信息。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class Todo {

    /** 待办事项标题（必填非空） */
    private final String title;

    /** 待办事项描述（允许为 null 或空） */
    private final String description;

    /**
     * 构造一条待办事项
     *
     * <p>title 为空（null、空字符串或纯空白）时抛出 {@link java.lang.IllegalArgumentException}，
     * description 允许为 null 或空。</p>
     *
     * @param title       待办事项标题，必填非空
     * @param description 待办事项描述，可为空
     * @throws IllegalArgumentException 当 title 为空时抛出
     */
    public Todo(String title, String description) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title 不能为空");
        }
        this.title = title;
        this.description = description;
    }

    /**
     * 获取待办事项标题
     *
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取待办事项描述
     *
     * @return 描述，可能为 null
     */
    public String getDescription() {
        return description;
    }

    /**
     * 返回待办事项的字符串表示
     *
     * @return 形如 Todo{title='...', description='...'} 的字符串
     */
    @Override
    public String toString() {
        return "Todo{title='" + title + "', description='" + description + "'}";
    }
}
