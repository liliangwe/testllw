# Task A：后端骨架 + 算法模块（对应 plan Task 1-2）

先在仓库根搭建 Maven 工程骨架，并实现三个算法接口。**先完成这步再做后续埋点/报表/导出**。

## 硬性前置

- 只写文件，**不要运行 mvn 构建**（JDK/Maven 正在后台安装，本任务纯写代码，验证由控制器统一执行）。
- Git 只读：不 commit、不 push、不 checkout。
- 包名 `com.example.demo`；后端源码放仓库根 `src/main/java/...`、`src/main/resources`、`src/test/java/...`；`pom.xml` 放仓库根。
- 统一响应结构 `{ "code":"0", "msg":"SUCCESS", "data":... }`；code 为字符串 "0" 表示成功。
- 参数校验失败返回业务码（见下），不抛 500 堆栈。

## 上下文

读取 `.agents/20261020-分别写三个接口helloworld_哈希/design.md` 的 5.1 节（算法模块）与 4.1/4.2（接口总览），
以及 `.agents/changes/task-DEV-966dcd0a-7905-11f1-9649-3b4281182f10-ebbe48e3-72a8-459e-b48f-b25c23672dd0/plan.md` 的「全局约束」。
仓库当前几乎为空（仅 README.md、.agents/）。

## 要求实现

### 1. pom.xml（仓库根）
- groupId `com.example`，artifactId `demo`，Java 17，Spring Boot 3.2.x（parent spring-boot-starter-parent）。
- 依赖：spring-boot-starter-web、spring-boot-starter-validation、spring-boot-starter-jdbc、spring-boot-starter-aop、spring-boot-starter-test、h2（scope runtime）、mysql-connector-j（scope runtime）。
- 因 H2 运行时存在，SQL 用 H2 与 MySQL 均可运行的通用语法（见 3）。

### 2. 应用与配置
- `DemoApplication`（@SpringBootApplication，@EnableAsync）。
- `config/AsyncConfig`：`ThreadPoolTaskExecutor` bean，名 `trackingExecutor`，用于后续埋点异步（本任务仅定义）。
- 统一响应体 `common/ApiResponse<T>`：字段 code(String)、msg(String)、data(T)；静态成功/失败工厂。
- 统一异常处理器 `common/GlobalExceptionHandler`：返回 ApiResponse，HTTP 状态 200（业务码在 code），并对参数校验异常返回明确 msg。
- `resources/application.yml`：H2 内存库 `jdbc:h2:mem:demo;MODE=MySQL;DB_CLOSE_DELAY=-1`，用户名 sa，空密码，driver org.h2.Driver；spring.sql.init 用 schema.sql 初始化（`mode: always`）；端口 8080。
- `resources/schema.sql`：建埋点表 `demo_api_tracking`（见 plan 全局约束字段；用 H2/MySQL 兼容语法：`id BIGINT AUTO_INCREMENT PRIMARY KEY`、`VARCHAR`、`DATETIME`、`CALL_TIME DATETIME`；MySQL 8 `DATETIME` 均可用）。

### 3. 算法模块
- `service/AlgorithmService`：纯算法，无状态：
  - `String hello(String name)`：返回 `"Hello, {name}!"`；name 为空/缺省用 "World"。
  - `String sha256(String text)`：JDK MessageDigest，返回小写 hex 64 位。
  - `int[] bubbleSort(int[] array)`：升序排序（返回新数组）。
  - 交换次数由 controller 侧需要，服务可另提供 `BubbleSortResult bubbleSortWithSwaps(int[])` 返回 `{int[] sorted; int swaps;}`（record 或小类）。
- `controller/DemoController`（`@RestController`，映射 `/api/demo`）：
  - `GET /helloworld?name=` → data `{message}`；name 长度 >64 返回业务码 ALG_001。
  - `POST /hash` body `{text}` → data `{algorithm:"SHA-256", digest}`；text 空返回 ALG_002。
  - `POST /bubble-sort` body `{array:number[]}` → data `{sorted, swaps}`；空数组 ALG_003；含非数字 ALG_004；长度>1000 返回 ALG_005。
  - 入参 DTO：`dto/HashRequest(text)`、`dto/BubbleSortRequest(array)`（用 int[]/Integer[] 或 List<Integer>，需能捕获非数字输入——建议 `List<Object>` 或自定义校验，最简单是接收 `Integer[]` 并用 @NotNull + 空值判 ALG_004）。
- 这些接口**暂不接埋点**（埋点切面在 Task B 加）。

### 4. 单元测试（src/test/java/com/example/demo）
- `service/AlgorithmServiceTest`：断言 sha256("abc") == ba7816bf...；bubbleSort([5,3,8,1]) → [1,3,5,8] 且 swaps==4；hello()。
- `controller/DemoControllerTest`：用 @WebMvcTest 或 @SpringBootTest + MockMvc 断言三个接口的 200 与业务码（如空 text→code ALG_002）。
测试须真实校验行为（非空断言）。

## 交付
实现完成后写报告到同目录 `task-A-report.md`：列出实现文件清单、测试说明（本任务不实际跑 mvn，说明测试意图与断言）、任何疑虑。回传状态 DONE / DONE_WITH_CONCERNS / BLOCKED（≤15 行）。