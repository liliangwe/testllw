/**
 * 待办事项应用入口
 *
 * <p>演示最小闭环：新增一条待办事项并打印创建结果（id/name/description）。</p>
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
        TodoItem item = service.create("购买牛奶", "低脂牛奶一箱");

        System.out.println("id=" + item.getId()
                + ", name=" + item.getName()
                + ", description=" + item.getDescription());

        // 验证最小闭环：通过 listAll 取回刚创建的待办
        if (service.listAll().size() == 1) {
            System.out.println("listAll 校验通过，共 " + service.listAll().size() + " 条待办");
        }
    }
}
