/**
 * 待办事项命令行入口。
 *
 * <p>用法：TodoApp &lt;事项名称&gt; [描述...]</p>
 *
 * @author AiWork
 */
public class TodoApp {

    /**
     * 程序入口方法。
     *
     * @param args 命令行参数：args[0] 为事项名称，其余参数拼接为描述
     */
    public static void main(String[] args) {
        if (args.length == 0 || args[0] == null || args[0].trim().isEmpty()) {
            System.err.println("用法: TodoApp <事项名称> [描述...]");
            System.exit(1);
        }

        String name = args[0].trim();
        StringBuilder descriptionBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                descriptionBuilder.append(' ');
            }
            descriptionBuilder.append(args[i]);
        }

        TodoService service = new TodoService(new TodoRepository());
        Todo created = service.createTodo(name, descriptionBuilder.toString());
        System.out.println(created);
    }
}
