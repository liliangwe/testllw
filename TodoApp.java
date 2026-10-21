/**
 * 待办事项程序入口。
 *
 * <p>演示「创建待办事项」最小闭环。</p>
 *
 * @author AiWork
 */
public class TodoApp {

    /**
     * 程序入口方法。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        TodoService service = new TodoService();
        Todo todo = service.create("示例事项", "示例描述");
        System.out.println(todo.getTitle());
        System.out.println(todo.getDescription());
    }
}