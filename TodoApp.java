/**
 * 待办事项应用入口。
 *
 * <p>演示「新增待办事项」的最小闭环：命令行传入事项名称与（可选）描述。</p>
 *
 * <p>用法：{@code java TodoApp <事项名称> [事项描述]}</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoApp {

    /**
     * 程序入口方法。
     *
     * @param args 命令行参数：args[0]=事项名称（必填），args[1]=事项描述（可选）
     */
    public static void main(String[] args) {
        if (args.length < 1 || args[0] == null || args[0].trim().isEmpty()) {
            System.err.println("用法: java TodoApp <事项名称> [事项描述]");
            System.exit(2);
        }

        TodoService service = new TodoService();
        String name = args[0];
        String description = args.length > 1 ? args[1] : "";

        TodoItem item = service.create(name, description);
        System.out.println("已创建待办事项：");
        System.out.println("  名称：" + item.getName());
        System.out.println("  描述：" + item.getDescription());
    }
}
