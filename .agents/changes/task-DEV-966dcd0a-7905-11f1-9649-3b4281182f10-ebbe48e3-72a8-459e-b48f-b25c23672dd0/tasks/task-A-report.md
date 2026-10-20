# Task A 实现报告

## 状态
DONE_WITH_CONCERNS（未运行 mvn，符合"纯写代码"约束；测试意图见下）

## 实现文件清单（全部位于 worktree 仓库根）
- pom.xml（仓库根）：groupId com.example、artifactId demo、Java 17、Spring Boot 3.2.5；依赖 web/validation/jdbc/aop/test + h2(runtime) + mysql-connector-j(runtime)。
- src/main/java/com/example/demo/DemoApplication.java：@SpringBootApplication + @EnableAsync。
- src/main/java/com/example/demo/config/AsyncConfig.java：ThreadPoolTaskExecutor bean，名 `trackingExecutor`（供 Task B 埋点异步用）。
- src/main/java/com/example/demo/config/WebConfig.java：/api/** CORS（plan 全局约束 Task 1 含 CORS）。
- src/main/java/com/example/demo/common/ApiResponse.java：统一响应 {code,msg,data}，成功 code "0"/msg "SUCCESS"，静态 success/failure 工厂。
- src/main/java/com/example/demo/common/GlobalExceptionHandler.java：MethodArgumentNotValidException / HttpMessageNotReadableException / 兜底 Exception，均返回 ApiResponse，HTTP 200（业务码进 code）。
- src/main/java/com/example/demo/service/AlgorithmService.java：hello / sha256(小写hex) / bubbleSort(新数组) / bubbleSortWithSwaps。
- src/main/java/com/example/demo/service/BubbleSortResult.java：record(int[] sorted, int swaps)。
- src/main/java/com/example/demo/dto/HashRequest.java：text。
- src/main/java/com/example/demo/dto/BubbleSortRequest.java：array 用 List<Object>（可捕获非数字与溢出）。
- src/main/java/com/example/demo/controller/DemoController.java：GET /helloworld、POST /hash、POST /bubble-sort。
- src/main/resources/application.yml：H2 mem demo MODE=MySQL DB_CLOSE_DELAY=-1、sa/空密码、org.h2.Driver、spring.sql.init.mode=always、端口 8080。
- src/main/resources/schema.sql：demo_api_tracking（id BIGINT AUTO_INCREMENT PK；api_name/caller_id/person_type/person_level/department/call_time/gmt_create/gmt_modified；DATETIME 默认 CURRENT_TIMESTAMP；idx_tracking_api_time、idx_tracking_dim 两索引）。
- src/test/java/com/example/demo/service/AlgorithmServiceTest.java。
- src/test/java/com/example/demo/controller/DemoControllerTest.java。

## 关键取值落实
- SHA-256("abc") = ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad（JDK MessageDigest + HexFormat 小写）。
- bubbleSortWithSwaps([5,3,8,1]) → sorted [1,3,5,8]、swaps=4（已手推验证 4 次交换）。
- 业务码：ALG_001(name>64)、ALG_002(text 空)、ALG_003(array 空)、ALG_004(含非数字)、ALG_005(array>1000)。
- trackingExecutor bean、@EnableAsync 已就位，但接口暂未接埋点（未引入切面/DAO，符合本任务范围）。
- Git 只读：仅执行过 git status --short，未 commit/push/checkout/reset。

## 测试意图与断言（本任务按约束不运行 mvn）
AlgorithmServiceTest（纯 JUnit，无 Spring 上下文）：
- hello(null) / hello("") → "Hello, World!"；hello("Alice") → "Hello, Alice!"。
- sha256("abc") == ba7816bf...（标准向量）；sha256 输出长度 64 且全小写 hex。
- bubbleSortWithSwaps([5,3,8,1]) → {[1,3,5,8], swaps=4}。
- bubbleSort 不修改入参数组（clone 独立性）；已序 [1,2,3] swaps=0。

DemoControllerTest（@WebMvcTest(DemoController) + @Import(AlgorithmService) + MockMvc）：
- GET /helloworld 默认 → code "0"、data.message "Hello, World!"；name=Alice → "Hello, Alice!"；name 65 字符 → code "ALG_001"。
- POST /hash {"text":"abc"} → code "0"、data.algorithm "SHA-256"、digest 标准向量；{"text":""} → code "ALG_002"。
- POST /bubble-sort {"array":[5,3,8,1]} → code "0"、sorted [1,3,5,8]、swaps 4；空数组 → ALG_003；含 "x" → ALG_004；1001 元素 → ALG_005。
- 所有断言均为具体值断言，非空断言。

## 疑虑 / 待澄清
1. 未运行 mvn/test（硬约束），因此编译与测试运行结果待控制器统一验证，无法在此自证通过。
2. schema.sql 中的 CREATE INDEX 未加 IF NOT EXISTS（H2/MySQL 均不支持 IF NOT EXISTS 索引）；spring.sql.init 对 H2 内存库 mode=always 只初始化一次，无重复执行风险，但若未来重复执行同库需注意。
3. bubbling sort 的 swaps 为"相邻交换次数"，与标准冒泡实现一致；若宿主要求"元素位移次数"则需调整（当前按 design 5.1.1 示例 [5,3,8,1]→4 为准）。
4. 统一异常处理器把 GenericException 返回 INTERNAL_ERROR 且 HTTP 状态仍为 200（@RestControllerAdvice 默认），符合"HTTP 状态 200（业务码在 code）"要求；若希望 5xx 需另行改 MediaType/HttpStatus，暂按任务要求保持 200。
5. mysql-connector-j 引入但 H2 运行时即可；无 @Mapper 相关依赖（埋点 DAO 属 Task B）。

## 后续（Task B 视角）
- trackingExecutor bean 已备；Controller 三接口定位在 com.example.demo.controller.DemoController，方法名 hello/hash/bubbleSort，映射 /api/demo/*，供切面 Pointcut 使用。