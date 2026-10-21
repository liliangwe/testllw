/**
 * 待办事项演示程序。
 *
 * <p>程序入口，演示「创建待办事项」的最小闭环：创建列表、新增两条事项、打印列表，
 * 并在 main 中做简单断言。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoApp {

    /**
     * 程序入口方法。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        TodoList list = new TodoList();

        list.add("购买牛奶", "下班后去超市购买一盒牛奶");
        list.add("阅读文档", "阅读待办事项任务简报，理解验收标准");

        // 断言 1：新增后的 size 符合预期
        assert list.size() == 2 : "expected size 2, but was " + list.size();

        // 断言 2：打印内容包含所加事项
        StringBuilder printed = new StringBuilder();
        for (TodoItem item : list.getItems()) {
            printed.append(item.getName());
            printed.append(":");
            printed.append(item.getDescription());
            printed.append(System.lineSeparator());
        }
        String content = printed.toString();
        assert content.contains("购买牛奶") : "missing item 购买牛奶";
        assert content.contains("阅读文档") : "missing item 阅读文档";

        System.out.println("待办事项列表（共 " + list.size() + " 条）：");
        System.out.print(content);
    }
}
