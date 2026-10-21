/**
 * 待办事项程序入口
 *
 * <p>演示「新增待办事项」的最小闭环：创建一条待办并输出其内容。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public final class TodoApp {

    /**
     * 程序入口方法。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        TodoService service = new TodoService();

        TodoItem created = service.create("示例待办", "这是第一条待办事项的描述");

        System.out.println("已新增待办事项：");
        System.out.println("  id          = " + created.getId());
        System.out.println("  名称        = " + created.getName());
        System.out.println("  描述        = " + created.getDescription());
        System.out.println("  创建时间(ms) = " + created.getCreatedAt());
        System.out.println("当前待办总数  = " + service.listAll().size());
    }
}