/**
 * 待办事项程序入口。
 *
 * <p>演示「新增待办事项」功能：从命令行读取名称与描述，构造一个 {@link TodoItem}
 * 并打印其结果。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoApp {

    /**
     * 程序入口方法。
     *
     * <p>用法：{@code java TodoApp <名称> <描述>}。参数少于 2 个时向标准错误流打印用法并
     * 以状态码 2 退出；否则以 {@code args[0]} 作为名称、{@code args[1]} 作为描述构造
     * {@link TodoItem} 并打印。</p>
     *
     * <p>多余的命令行参数（第 3 个及以后）被忽略，以保持最小实现。</p>
     *
     * @param args 命令行参数，期望至少包含名称与描述两项
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("用法: java TodoApp <名称> <描述>");
            System.exit(2);
        }
        // 多余参数（args[2] 及之后）被忽略。
        TodoItem todo = new TodoItem(args[0], args[1]);
        System.out.println(todo);
    }
}