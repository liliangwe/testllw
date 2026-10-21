/**
 * 待办事项程序入口
 *
 * <p>演示程序：创建 {@link TodoService}，新增一条示例待办事项并打印结果。</p>
 *
 * @author AiWork
 */
public class TodoApp {

    /**
     * 程序入口方法
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        TodoService service = new TodoService();
        TodoItem item = service.create("示例事项", "这是一个待办事项描述");
        System.out.println(item);
    }
}
