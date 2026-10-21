/**
 * TodoServiceTest 自包含测试入口（无 JUnit 依赖）。
 *
 * <p>通过手写断言验证 TodoService 的「仅创建」能力，断言失败时抛出
 * {@link AssertionError}。所有断言串行执行，任一步失败即中止。</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class TodoServiceTest {

    public static void main(String[] args) {
        TodoServiceTest test = new TodoServiceTest();
        test.testCreateSavesFieldsAndIncrementsSize();
        test.testCreateRejectsBlankName();
        test.testCreateRejectsNullName();
        test.testCreateAcceptsNullDescription();
        System.out.println("All tests passed.");
    }

    /** 正常创建：名称/描述正确保存，size 正确递增。 */
    private void testCreateSavesFieldsAndIncrementsSize() {
        TodoService service = new TodoService();
        assertState("初始 size 应为 0", service.size() == 0);

        Todo first = service.create("买菜", "买牛奶和鸡蛋");
        assertState("首次创建后 size 应为 1", service.size() == 1);
        assertState("首次创建的名称应正确保存", "买菜".equals(first.getName()));
        assertState("首次创建的描述应正确保存", "买牛奶和鸡蛋".equals(first.getDescription()));

        Todo second = service.create("买菜", null);
        assertState("第二次创建后 size 应为 2", service.size() == 2);
        assertState("第二次创建的描述应为 null", second.getDescription() == null);
    }

    /** 名称空白（trim 后为空）应拒绝创建并抛出 IllegalArgumentException。 */
    private void testCreateRejectsBlankName() {
        TodoService service = new TodoService();
        try {
            service.create("   ", "任意描述");
            throw new AssertionError("空白名称应抛出 IllegalArgumentException，但未抛出");
        } catch (IllegalArgumentException expected) {
            assertState("空白名称被拒绝后 size 应保持 0", service.size() == 0);
        }
    }

    /** 名称为 null 应拒绝创建并抛出 IllegalArgumentException。 */
    private void testCreateRejectsNullName() {
        TodoService service = new TodoService();
        try {
            service.create(null, "任意描述");
            throw new AssertionError("null 名称应抛出 IllegalArgumentException，但未抛出");
        } catch (IllegalArgumentException expected) {
            assertState("null 名称被拒绝后 size 应保持 0", service.size() == 0);
        }
    }

    /** description 为 null 可接受，应正常创建并保存 name。 */
    private void testCreateAcceptsNullDescription() {
        TodoService service = new TodoService();
        Todo todo = service.create("会议纪要", null);
        assertState("description 为 null 时应正常创建，size 应为 1", service.size() == 1);
        assertState("名称应正确保存", "会议纪要".equals(todo.getName()));
        assertState("描述应为 null", todo.getDescription() == null);

        Todo todoEmpty = service.create("空描述", "");
        assertState("description 为空串时应正常创建，size 应为 2", service.size() == 2);
        assertState("空串描述应正确保存", "".equals(todoEmpty.getDescription()));
    }

    /** 手写断言：条件为 false 时抛出 AssertionError。 */
    private static void assertState(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
