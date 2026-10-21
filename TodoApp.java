/**
 * 待办事项应用入口。
 * <p>演示新增一条待办事项的最小闭环。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoApp {

    public static void main(String[] args) {
        TodoService service = new TodoService();
        TodoItem item = service.add("示例待办", "这是一条描述");
        System.out.println("已创建待办: id=" + item.getId()
                + ", name=" + item.getName()
                + ", description=" + item.getDescription());
        System.out.println("当前待办总数: " + service.list().size());
    }
}
