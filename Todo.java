/**
 * 待办事项模型。
 *
 * <p>描述一条待办事项：名称（title）与描述（description）。</p>
 *
 * @author AiWork
 */
public class Todo {

    /** 事项名称 */
    private final String title;

    /** 事项描述 */
    private final String description;

    /**
     * 构造一条待办事项。
     *
     * @param title       事项名称
     * @param description 事项描述
     */
    public Todo(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /**
     * 获取事项名称。
     *
     * @return 事项名称
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取事项描述。
     *
     * @return 事项描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 返回待办事项的字符串表示。
     *
     * @return 事项名称与描述
     */
    @Override
    public String toString() {
        return "Todo{title='" + title + "', description='" + description + "'}";
    }
}