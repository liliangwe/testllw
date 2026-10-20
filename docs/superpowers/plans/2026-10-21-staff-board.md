# 人员看板（Staff Board）Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有 todo-app（Spring Boot 3.2.5 + Java 21 + MyBatis + MySQL）上新增「人员看板」模块，提供员工基本信息增删改查、批量导入（受白名单保护）与成本预算记录能力。

**Architecture:** 沿用仓库既有分层（Controller → Service → Mapper → MySQL），新增 `com.example.todo.staff` 子包，复用 `com.example.todo.common.Result` / `BizException` 统一响应与错误码。数据层新增 `staff`、`staff_budget`、`staff_whitelist` 三张表；导入走 CSV 文本解析（零新增依赖），白名单由 `staff_whitelist` 表维护、服务端按请求头账号校验。删除采用逻辑删除（`deleted` 标记）。

**Tech Stack:** Java 21、Spring Boot 3.2.5、MyBatis 3.0.3、MySQL 8、H2（测试）、JUnit 5 + Mockito（测试）。

## Global Constraints

- Java 版本下限：21（`<java.version>21</java.version>`，不改动）。
- Spring Boot 版本：3.2.5（复用现有 parent，不升级）。
- MyBatis：`org.mybatis.spring.boot:mybatis-spring-boot-starter` 3.0.3（复用现有依赖，不新增）。
- 响应结构：`com.example.todo.common.Result<T>`，成功 `code="OK"`，失败 `code` 为业务错误码、`msg` 为提示。
- 业务异常：`com.example.todo.common.BizException(errorCode, message)`，由 Controller 捕获转成 400，未知异常转成 500。
- 错误码前缀：人员看板统一使用 `STAFF_xxx`（不占用现有 `TODO_xxx`）。
- 表命名：全小写下划线；含 `id`/`gmt_create`/`gmt_modified` 标准字段；引擎 InnoDB、字符集 utf8mb4、禁止 enum 类型（状态用 varchar）。
- 工号 `staff_no` 全局唯一（R1）；姓名、工号必填（R2）；预算金额非负、周期枚举 `MONTH`/`YEAR`（R3）。
- 批量导入必须命中白名单，否则拒绝（R4）；删除为逻辑删除（R5）；导入逐行原子提交，单行失败不影响其它行（R6）。
- 导入文件格式（假设 A04 收敛）：本期仅支持 **CSV（UTF-8、逗号分隔、首行表头、按行解析、每行 8 列）**，不引入 Excel 第三方依赖；`.xlsx` 支持为后续迭代（需新增 Apache POI/EasyExcel），不在本期范围。
- 白名单账号来源（假设 A02 收敛）：当前应用无独立认证体系，接口约定通过请求头 `X-Operator-Account` 透传操作人账号，服务端查 `staff_whitelist` 做白名单校验。
- 导入单次上限：`staff.import.max-rows=1000`（可配置，超限拒绝）。
- 逻辑删除限制：全库只读查询均需拼接 `deleted = 0` 过滤条件。

---

## File Structure

新增模块集中放在 `com.example.todo.staff` 及其子包，不修改现有 `todo` 模块的任何源码/配置（除在 `schema.sql`、`application.yml` 追加内容外）。

```
src/main/java/com/example/todo/staff/
  entity/StaffDO.java              员工数据对象
  entity/StaffBudgetDO.java        成本预算数据对象
  entity/WhitelistDO.java          白名单数据对象
  entity/BudgetPeriod.java         预算周期枚举
  mapper/StaffMapper.java          员工 Mapper
  mapper/StaffBudgetMapper.java    预算 Mapper
  mapper/WhitelistMapper.java      白名单 Mapper
  dto/StaffSaveRequest.java        新增/更新员工请求（含预算）
  dto/StaffDetailVO.java           员工详情出参（含预算）
  dto/StaffRowVO.java              列表行出参（含预算）
  dto/StaffPageQuery.java          分页查询入参
  dto/PageVO.java                  分页结果容器
  dto/ImportResultVO.java          导入汇总结果
  dto/ImportFailureVO.java         导入单行失败明细
  common/StaffErrorCodes.java      业务错误码常量
  service/StaffService.java        员工服务接口
  service/impl/StaffServiceImpl.java
  service/impl/CsvImporter.java    CSV 批量导入器（白名单校验）
  controller/StaffController.java  员工 REST 控制器
src/main/resources/
  mapper/StaffMapper.xml
  mapper/StaffBudgetMapper.xml
  mapper/WhitelistMapper.xml
  db/schema.sql                    （追加三张表）
  application.yml                  （追加 staff.* 配置）
src/test/java/com/example/todo/staff/
  service/StaffServiceImplTest.java
  service/impl/CsvImporterTest.java
  controller/StaffControllerTest.java
  mapper/StaffMapperTest.java
```

---

## Task 1: 数据模型 —— schema、实体、枚举

**Files:**
- Modify: `src/main/resources/db/schema.sql`（追加三张表）
- Create: `src/main/java/com/example/todo/staff/entity/BudgetPeriod.java`
- Create: `src/main/java/com/example/todo/staff/entity/StaffDO.java`
- Create: `src/main/java/com/example/todo/staff/entity/StaffBudgetDO.java`
- Create: `src/main/java/com/example/todo/staff/entity/WhitelistDO.java`

**Interfaces:**
- Consumes: 无（首个任务）。
- Produces: `BudgetPeriod`（`MONTH`/`YEAR`，`getCode()`/`getDesc()`/`fromCode(String)`）；`StaffDO`、`StaffBudgetDO`、`WhitelistDO` 的字段与 getter/setter，供后续 Mapper 映射使用。

- [ ] **Step 1: 追加建表语句（改 schema.sql）**

在 `schema.sql` 末尾（`todo_item` 表之后）追加：

```sql
-- 员工表
CREATE TABLE IF NOT EXISTS staff (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    staff_no     VARCHAR(64)   NOT NULL                COMMENT '工号',
    name         VARCHAR(64)   NOT NULL                COMMENT '姓名',
    department   VARCHAR(128)  DEFAULT NULL            COMMENT '部门',
    position     VARCHAR(128)  DEFAULT NULL            COMMENT '岗位',
    email        VARCHAR(128)  DEFAULT NULL            COMMENT '邮箱',
    hire_date    DATE          DEFAULT NULL            COMMENT '入职日期',
    deleted      TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除,1-已删除',
    gmt_create   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_staff_staff_no (staff_no),
    INDEX idx_staff_department (department),
    INDEX idx_staff_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 员工成本预算表
CREATE TABLE IF NOT EXISTS staff_budget (
    id           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    staff_id     BIGINT         NOT NULL                COMMENT '员工ID',
    amount       DECIMAL(18,2)  NOT NULL                COMMENT '预算金额',
    period       VARCHAR(16)    NOT NULL                COMMENT '周期：MONTH-月度,YEAR-年度',
    gmt_create   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_staff_budget_staff_id (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工成本预算表';

-- 导入白名单表
CREATE TABLE IF NOT EXISTS staff_whitelist (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    account    VARCHAR(64) NOT NULL                COMMENT '白名单账号',
    gmt_create DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_staff_whitelist_account (account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入白名单表';
```

- [ ] **Step 2: 创建枚举 BudgetPeriod**

```java
package com.example.todo.staff.entity;

/**
 * 成本预算周期枚举
 *
 * @author AiWork
 * @date 2026/10/21
 */
public enum BudgetPeriod {

    MONTH("MONTH", "月度"),
    YEAR("YEAR", "年度");

    private final String code;
    private final String desc;

    BudgetPeriod(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static BudgetPeriod fromCode(String code) {
        for (BudgetPeriod period : values()) {
            if (period.code.equals(code)) {
                return period;
            }
        }
        throw new IllegalArgumentException("无效的预算周期码: " + code);
    }
}
```

- [ ] **Step 3: 创建实体 StaffDO**

```java
package com.example.todo.staff.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工数据对象
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class StaffDO {

    private Long id;
    private String staffNo;
    private String name;
    private String department;
    private String position;
    private String email;
    private LocalDate hireDate;
    private Integer deleted;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStaffNo() { return staffNo; }
    public void setStaffNo(String staffNo) { this.staffNo = staffNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getGmtCreate() { return gmtCreate; }
    public void setGmtCreate(LocalDateTime gmtCreate) { this.gmtCreate = gmtCreate; }
    public LocalDateTime getGmtModified() { return gmtModified; }
    public void setGmtModified(LocalDateTime gmtModified) { this.gmtModified = gmtModified; }
}
```

- [ ] **Step 4: 创建实体 StaffBudgetDO**

```java
package com.example.todo.staff.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工成本预算数据对象
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class StaffBudgetDO {

    private Long id;
    private Long staffId;
    private BigDecimal amount;
    private String period;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public LocalDateTime getGmtCreate() { return gmtCreate; }
    public void setGmtCreate(LocalDateTime gmtCreate) { this.gmtCreate = gmtCreate; }
    public LocalDateTime getGmtModified() { return gmtModified; }
    public void setGmtModified(LocalDateTime gmtModified) { this.gmtModified = gmtModified; }
}
```

- [ ] **Step 5: 创建实体 WhitelistDO**

```java
package com.example.todo.staff.entity;

import java.time.LocalDateTime;

/**
 * 导入白名单数据对象
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class WhitelistDO {

    private Long id;
    private String account;
    private LocalDateTime gmtCreate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public LocalDateTime getGmtCreate() { return gmtCreate; }
    public void setGmtCreate(LocalDateTime gmtCreate) { this.gmtCreate = gmtCreate; }
}
```

- [ ] **Step 6: 编译验证**

Run: `mvn -q -o compile`（无网络时）或 `mvn -q compile`
Expected: BUILD SUCCESS，无编译错误。

- [ ] **Step 7: Commit**

```bash
git add src/main/resources/db/schema.sql src/main/java/com/example/todo/staff/entity/
git commit -m "feat(staff): add staff board schema and entities"
```

---

## Task 2: Mapper 层 —— 员工/预算/白名单

**Files:**
- Create: `src/main/java/com/example/todo/staff/mapper/StaffMapper.java`
- Create: `src/main/java/com/example/todo/staff/mapper/StaffBudgetMapper.java`
- Create: `src/main/java/com/example/todo/staff/mapper/WhitelistMapper.java`
- Create: `src/main/resources/mapper/StaffMapper.xml`
- Create: `src/main/resources/mapper/StaffBudgetMapper.xml`
- Create: `src/main/resources/mapper/WhitelistMapper.xml`
- Test: `src/test/java/com/example/todo/staff/mapper/StaffMapperTest.java`

**Interfaces:**
- Consumes: `StaffDO`、`StaffBudgetDO`、`WhitelistDO`（Task 1）。
- Produces: 下述方法签名，供 `StaffServiceImpl` 使用：
  - `StaffMapper`: `int insert(StaffDO)`、`int updateById(StaffDO)`、`int softDeleteById(@Param("id") Long id)`、`StaffDO findById(Long id)`、`StaffDO findByStaffNo(String staffNo)`、`List<StaffDO> findByCondition(StaffPageQuery query)`、`long countByCondition(StaffPageQuery query)`
  - `StaffBudgetMapper`: `int insert(StaffBudgetDO)`、`int updateByStaffId(StaffBudgetDO)`、`StaffBudgetDO findByStaffId(Long staffId)`
  - `WhitelistMapper`: `WhitelistDO findByAccount(String account)`

- [ ] **Step 1: 编写 StaffMapper 接口**

```java
package com.example.todo.staff.mapper;

import com.example.todo.staff.dto.StaffPageQuery;
import com.example.todo.staff.entity.StaffDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工 Mapper
 *
 * @author AiWork
 * @date 2026/10/21
 */
@Mapper
public interface StaffMapper {

    int insert(StaffDO staff);

    int updateById(StaffDO staff);

    int softDeleteById(@Param("id") Long id);

    StaffDO findById(@Param("id") Long id);

    StaffDO findByStaffNo(@Param("staffNo") String staffNo);

    List<StaffDO> findByCondition(StaffPageQuery query);

    long countByCondition(StaffPageQuery query);
}
```

- [ ] **Step 2: 编写 StaffMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.todo.staff.mapper.StaffMapper">

    <resultMap id="BaseResultMap" type="com.example.todo.staff.entity.StaffDO">
        <id column="id" property="id"/>
        <result column="staff_no" property="staffNo"/>
        <result column="name" property="name"/>
        <result column="department" property="department"/>
        <result column="position" property="position"/>
        <result column="email" property="email"/>
        <result column="hire_date" property="hireDate"/>
        <result column="deleted" property="deleted"/>
        <result column="gmt_create" property="gmtCreate"/>
        <result column="gmt_modified" property="gmtModified"/>
    </resultMap>

    <sql id="baseColumns">
        id, staff_no, name, department, position, email, hire_date, deleted, gmt_create, gmt_modified
    </sql>

    <insert id="insert" parameterType="com.example.todo.staff.entity.StaffDO"
            useGeneratedKeys="true" keyProperty="id">
        INSERT INTO staff (staff_no, name, department, position, email, hire_date, deleted, gmt_create, gmt_modified)
        VALUES (#{staffNo}, #{name}, #{department}, #{position}, #{email}, #{hireDate}, #{deleted}, #{gmtCreate}, #{gmtModified})
    </insert>

    <update id="updateById" parameterType="com.example.todo.staff.entity.StaffDO">
        UPDATE staff
        SET name = #{name},
            department = #{department},
            position = #{position},
            email = #{email},
            hire_date = #{hireDate},
            gmt_modified = #{gmtModified}
        WHERE id = #{id}
          AND staff_no = #{staffNo}
          AND deleted = 0
    </update>

    <update id="softDeleteById">
        UPDATE staff
        SET deleted = 1,
            gmt_modified = CURRENT_TIMESTAMP
        WHERE id = #{id}
          AND deleted = 0
    </update>

    <select id="findById" resultMap="BaseResultMap">
        SELECT <include refid="baseColumns"/>
        FROM staff
        WHERE id = #{id}
          AND deleted = 0
        LIMIT 1
    </select>

    <select id="findByStaffNo" resultMap="BaseResultMap">
        SELECT <include refid="baseColumns"/>
        FROM staff
        WHERE staff_no = #{staffNo}
          AND deleted = 0
        LIMIT 1
    </select>

    <select id="findByCondition" resultMap="BaseResultMap">
        SELECT <include refid="baseColumns"/>
        FROM staff
        WHERE deleted = 0
        <if test="name != null and name != ''">
            AND name LIKE CONCAT('%', #{name}, '%')
        </if>
        <if test="staffNo != null and staffNo != ''">
            AND staff_no = #{staffNo}
        </if>
        <if test="department != null and department != ''">
            AND department = #{department}
        </if>
        ORDER BY id DESC
        LIMIT #{offset}, #{pageSize}
    </select>

    <select id="countByCondition" resultType="long">
        SELECT COUNT(*)
        FROM staff
        WHERE deleted = 0
        <if test="name != null and name != ''">
            AND name LIKE CONCAT('%', #{name}, '%')
        </if>
        <if test="staffNo != null and staffNo != ''">
            AND staff_no = #{staffNo}
        </if>
        <if test="department != null and department != ''">
            AND department = #{department}
        </if>
    </select>

</mapper>
```

- [ ] **Step 3: 编写 StaffBudgetMapper 接口与 XML**

```java
package com.example.todo.staff.mapper;

import com.example.todo.staff.entity.StaffBudgetDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 员工成本预算 Mapper
 *
 * @author AiWork
 * @date 2026/10/21
 */
@Mapper
public interface StaffBudgetMapper {

    int insert(StaffBudgetDO budget);

    int updateByStaffId(StaffBudgetDO budget);

    StaffBudgetDO findByStaffId(@Param("staffId") Long staffId);
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.todo.staff.mapper.StaffBudgetMapper">

    <resultMap id="BaseResultMap" type="com.example.todo.staff.entity.StaffBudgetDO">
        <id column="id" property="id"/>
        <result column="staff_id" property="staffId"/>
        <result column="amount" property="amount"/>
        <result column="period" property="period"/>
        <result column="gmt_create" property="gmtCreate"/>
        <result column="gmt_modified" property="gmtModified"/>
    </resultMap>

    <insert id="insert" parameterType="com.example.todo.staff.entity.StaffBudgetDO"
            useGeneratedKeys="true" keyProperty="id">
        INSERT INTO staff_budget (staff_id, amount, period, gmt_create, gmt_modified)
        VALUES (#{staffId}, #{amount}, #{period}, #{gmtCreate}, #{gmtModified})
    </insert>

    <update id="updateByStaffId" parameterType="com.example.todo.staff.entity.StaffBudgetDO">
        UPDATE staff_budget
        SET amount = #{amount},
            period = #{period},
            gmt_modified = #{gmtModified}
        WHERE staff_id = #{staffId}
    </update>

    <select id="findByStaffId" resultMap="BaseResultMap">
        SELECT id, staff_id, amount, period, gmt_create, gmt_modified
        FROM staff_budget
        WHERE staff_id = #{staffId}
        LIMIT 1
    </select>

</mapper>
```

- [ ] **Step 4: 编写 WhitelistMapper 接口与 XML**

```java
package com.example.todo.staff.mapper;

import com.example.todo.staff.entity.WhitelistDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 白名单 Mapper
 *
 * @author AiWork
 * @date 2026/10/21
 */
@Mapper
public interface WhitelistMapper {

    WhitelistDO findByAccount(@Param("account") String account);
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.todo.staff.mapper.WhitelistMapper">

    <resultMap id="BaseResultMap" type="com.example.todo.staff.entity.WhitelistDO">
        <id column="id" property="id"/>
        <result column="account" property="account"/>
        <result column="gmt_create" property="gmtCreate"/>
    </resultMap>

    <select id="findByAccount" resultMap="BaseResultMap">
        SELECT id, account, gmt_create
        FROM staff_whitelist
        WHERE account = #{account}
        LIMIT 1
    </select>

</mapper>
```

- [ ] **Step 5: 编写 Mapper 集成测试（H2）**

```java
package com.example.todo.staff.mapper;

import com.example.todo.staff.entity.StaffDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * StaffMapper 集成测试（H2，MODE=MySQL）
 *
 * @author AiWork
 * @date 2026/10/21
 */
@MybatisTest
@Sql(scripts = "classpath:db/schema.sql")
@DisplayName("StaffMapper 集成测试")
class StaffMapperTest {

    @Autowired
    private StaffMapper staffMapper;

    @Test
    @DisplayName("插入后可按工号查询且逻辑删除后不可查询")
    void should_insert_find_and_softDelete() {
        StaffDO staff = new StaffDO();
        staff.setStaffNo("S-001");
        staff.setName("张三");
        staff.setDepartment("研发部");
        staff.setHireDate(LocalDate.of(2026, 1, 5));
        staff.setDeleted(0);
        staff.setGmtCreate(LocalDateTime.now());
        staff.setGmtModified(staff.getGmtCreate());

        staffMapper.insert(staff);
        assertNotNull(staff.getId());

        StaffDO found = staffMapper.findByStaffNo("S-001");
        assertNotNull(found);
        assertEquals("张三", found.getName());
        assertEquals("研发部", found.getDepartment());

        int affected = staffMapper.softDeleteById(staff.getId());
        assertEquals(1, affected);
        assertNull(staffMapper.findById(staff.getId()));
    }
}
```

- [ ] **Step 6: 运行测试验证通过**

Run: `mvn -q -Dtest=StaffMapperTest test`
Expected: BUILD SUCCESS，`should_insert_find_and_softDelete` 通过。

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/example/todo/staff/mapper/ src/main/resources/mapper/ src/test/java/com/example/todo/staff/mapper/
git commit -m "feat(staff): add staff/budget/whitelist mappers"
```

---

## Task 3: DTO 与分页容器

**Files:**
- Create: `src/main/java/com/example/todo/staff/dto/StaffSaveRequest.java`
- Create: `src/main/java/com/example/todo/staff/dto/StaffDetailVO.java`
- Create: `src/main/java/com/example/todo/staff/dto/StaffRowVO.java`
- Create: `src/main/java/com/example/todo/staff/dto/StaffPageQuery.java`
- Create: `src/main/java/com/example/todo/staff/dto/PageVO.java`
- Create: `src/main/java/com/example/todo/staff/dto/ImportResultVO.java`
- Create: `src/main/java/com/example/todo/staff/dto/ImportFailureVO.java`

**Interfaces:**
- Consumes: 无（纯数据结构）。
- Produces: DTO 字段，供 `StaffService`/`StaffController` 序列化与映射：
  - `StaffSaveRequest(staffNo, name, department, position, email, hireDate, amount, period)`
  - `StaffDetailVO(id, staffNo, name, department, position, email, hireDate, amount, period, gmtCreate)`
  - `StaffRowVO(id, staffNo, name, department, position, amount, period)`
  - `StaffPageQuery(pageNum, pageSize, name, staffNo, department)`（含 `getOffset()` 派生值）
  - `PageVO<T>(total, pageNum, pageSize, records)`
  - `ImportResultVO(totalRows, successCount, failureCount, failures)`；`ImportFailureVO(rowNo, reason)`

- [ ] **Step 1: 编写 StaffSaveRequest**

```java
package com.example.todo.staff.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新增/更新员工请求（含成本预算）
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class StaffSaveRequest {

    private String staffNo;
    private String name;
    private String department;
    private String position;
    private String email;
    private LocalDate hireDate;
    private BigDecimal amount;
    private String period;

    public String getStaffNo() { return staffNo; }
    public void setStaffNo(String staffNo) { this.staffNo = staffNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}
```

- [ ] **Step 2: 编写 StaffDetailVO / StaffRowVO**

```java
package com.example.todo.staff.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 员工详情出参（含成本预算）
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class StaffDetailVO {

    private Long id;
    private String staffNo;
    private String name;
    private String department;
    private String position;
    private String email;
    private LocalDate hireDate;
    private BigDecimal amount;
    private String period;
    private String gmtCreate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStaffNo() { return staffNo; }
    public void setStaffNo(String staffNo) { this.staffNo = staffNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getGmtCreate() { return gmtCreate; }
    public void setGmtCreate(String gmtCreate) { this.gmtCreate = gmtCreate; }
}
```

```java
package com.example.todo.staff.dto;

import java.math.BigDecimal;

/**
 * 员工列表行出参（含成本预算）
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class StaffRowVO {

    private Long id;
    private String staffNo;
    private String name;
    private String department;
    private String position;
    private BigDecimal amount;
    private String period;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStaffNo() { return staffNo; }
    public void setStaffNo(String staffNo) { this.staffNo = staffNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}
```

- [ ] **Step 3: 编写 StaffPageQuery / PageVO**

```java
package com.example.todo.staff.dto;

/**
 * 员工分页查询入参
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class StaffPageQuery {

    private int pageNum = 1;
    private int pageSize = 20;
    private String name;
    private String staffNo;
    private String department;

    public int getPageNum() { return pageNum; }
    public void setPageNum(int pageNum) { this.pageNum = pageNum; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStaffNo() { return staffNo; }
    public void setStaffNo(String staffNo) { this.staffNo = staffNo; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    /** 派生：MySQL LIMIT 偏移量 */
    public int getOffset() {
        return Math.max(pageNum - 1, 0) * Math.max(pageSize, 1);
    }
}
```

```java
package com.example.todo.staff.dto;

import java.util.List;

/**
 * 分页结果容器
 *
 * @param <T> 列表元素类型
 * @author AiWork
 * @date 2026/10/21
 */
public class PageVO<T> {

    private long total;
    private int pageNum;
    private int pageSize;
    private List<T> records;

    public PageVO() {
    }

    public PageVO(long total, int pageNum, int pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPageNum() { return pageNum; }
    public void setPageNum(int pageNum) { this.pageNum = pageNum; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
}
```

- [ ] **Step 4: 编写导入结果 DTO**

```java
package com.example.todo.staff.dto;

import java.util.List;

/**
 * 批量导入汇总结果
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class ImportResultVO {

    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<ImportFailureVO> failures;

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
    public List<ImportFailureVO> getFailures() { return failures; }
    public void setFailures(List<ImportFailureVO> failures) { this.failures = failures; }
}
```

```java
package com.example.todo.staff.dto;

/**
 * 导入单行失败明细
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class ImportFailureVO {

    private int rowNo;
    private String reason;

    public ImportFailureVO() {
    }

    public ImportFailureVO(int rowNo, String reason) {
        this.rowNo = rowNo;
        this.reason = reason;
    }

    public int getRowNo() { return rowNo; }
    public void setRowNo(int rowNo) { this.rowNo = rowNo; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
```

- [ ] **Step 5: 编译验证**

Run: `mvn -q compile`
Expected: BUILD SUCCESS。

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/todo/staff/dto/
git commit -m "feat(staff): add staff DTOs and page container"
```

---

## Task 4: Service 层 —— CRUD + 白名单 + CSV 批量导入

**Files:**
- Create: `src/main/java/com/example/todo/staff/service/StaffService.java`
- Create: `src/main/java/com/example/todo/staff/service/impl/StaffServiceImpl.java`
- Create: `src/main/java/com/example/todo/staff/service/impl/CsvImporter.java`
- Create: `src/main/java/com/example/todo/staff/common/StaffErrorCodes.java`
- Test: `src/test/java/com/example/todo/staff/service/StaffServiceImplTest.java`
- Test: `src/test/java/com/example/todo/staff/service/impl/CsvImporterTest.java`

**Interfaces:**
- Consumes: Task 2 的 Mapper 方法、Task 3 的 DTO。
- Produces: 供 `StaffController` 调用：
  - `PageVO<StaffRowVO> page(StaffPageQuery query)`
  - `StaffDetailVO getDetail(Long id)`
  - `StaffDetailVO create(StaffSaveRequest request)`
  - `StaffDetailVO update(Long id, StaffSaveRequest request)`
  - `void delete(Long id)`
  - `ImportResultVO importCsv(String account, String csvText)`

**业务规则（错误码）：**
- `STAFF_001` 工号/姓名不能为空；`STAFF_002` 工号已存在；`STAFF_003` 员工不存在；`STAFF_004` 预算金额为负数；`STAFF_005` 预算周期无效；`STAFF_006` 缺少白名单账号；`STAFF_403` 无批量导入权限；`STAFF_007` 导入行数超限；`STAFF_999` 系统内部错误。

- [ ] **Step 1: 编写 StaffService 接口**

```java
package com.example.todo.staff.service;

import com.example.todo.staff.dto.ImportResultVO;
import com.example.todo.staff.dto.PageVO;
import com.example.todo.staff.dto.StaffDetailVO;
import com.example.todo.staff.dto.StaffPageQuery;
import com.example.todo.staff.dto.StaffRowVO;
import com.example.todo.staff.dto.StaffSaveRequest;

/**
 * 员工服务接口
 *
 * @author AiWork
 * @date 2026/10/21
 */
public interface StaffService {

    PageVO<StaffRowVO> page(StaffPageQuery query);

    StaffDetailVO getDetail(Long id);

    StaffDetailVO create(StaffSaveRequest request);

    StaffDetailVO update(Long id, StaffSaveRequest request);

    void delete(Long id);

    ImportResultVO importCsv(String account, String csvText);
}
```

- [ ] **Step 2: 编写 StaffServiceImpl（实现 CRUD）**

```java
package com.example.todo.staff.service.impl;

import com.example.todo.staff.common.StaffErrorCodes;
import com.example.todo.common.BizException;
import com.example.todo.staff.dto.ImportResultVO;
import com.example.todo.staff.dto.PageVO;
import com.example.todo.staff.dto.StaffDetailVO;
import com.example.todo.staff.dto.StaffPageQuery;
import com.example.todo.staff.dto.StaffRowVO;
import com.example.todo.staff.dto.StaffSaveRequest;
import com.example.todo.staff.entity.BudgetPeriod;
import com.example.todo.staff.entity.StaffBudgetDO;
import com.example.todo.staff.entity.StaffDO;
import com.example.todo.staff.mapper.StaffBudgetMapper;
import com.example.todo.staff.mapper.StaffMapper;
import com.example.todo.staff.service.StaffService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工服务实现
 *
 * @author AiWork
 * @date 2026/10/21
 */
@Service
public class StaffServiceImpl implements StaffService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String DEFAULT_PERIOD = BudgetPeriod.MONTH.getCode();

    private final StaffMapper staffMapper;
    private final StaffBudgetMapper budgetMapper;
    private final CsvImporter csvImporter;

    public StaffServiceImpl(StaffMapper staffMapper, StaffBudgetMapper budgetMapper, CsvImporter csvImporter) {
        this.staffMapper = staffMapper;
        this.budgetMapper = budgetMapper;
        this.csvImporter = csvImporter;
    }

    @Override
    public PageVO<StaffRowVO> page(StaffPageQuery query) {
        if (query.getPageSize() > 200) {
            query.setPageSize(200);
        }
        long total = staffMapper.countByCondition(query);
        List<StaffDO> staffs = staffMapper.findByCondition(query);
        List<StaffRowVO> rows = staffs.stream().map(this::toRowVO).collect(Collectors.toList());
        return new PageVO<>(total, query.getPageNum(), query.getPageSize(), rows);
    }

    @Override
    public StaffDetailVO getDetail(Long id) {
        StaffDO staff = requireStaff(id);
        return toDetailVO(staff);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StaffDetailVO create(StaffSaveRequest request) {
        validateSaveRequest(request);
        StaffDO existing = staffMapper.findByStaffNo(request.getStaffNo().trim());
        if (existing != null) {
            throw new BizException(StaffErrorCodes.DUPLICATE_STAFF_NO, "工号已存在");
        }

        StaffDO staff = new StaffDO();
        applyRequest(staff, request);
        staff.setDeleted(0);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        staff.setGmtCreate(now);
        staff.setGmtModified(now);
        staffMapper.insert(staff);

        insertBudget(staff.getId(), request);

        return toDetailVO(staff);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StaffDetailVO update(Long id, StaffSaveRequest request) {
        requireStaff(id);
        validateSaveRequest(request);

        StaffDO staff = new StaffDO();
        staff.setId(id);
        applyRequest(staff, request);
        staff.setGmtModified(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));

        int affected = staffMapper.updateById(staff);
        if (affected == 0) {
            throw new BizException(StaffErrorCodes.STAFF_NOT_FOUND, "员工不存在");
        }

        upsertBudget(id, request);

        return getDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireStaff(id);
        staffMapper.softDeleteById(id);
    }

    @Override
    public ImportResultVO importCsv(String account, String csvText) {
        return csvImporter.doImport(account, csvText);
    }

    // ---------- 校验 ----------

    private void validateSaveRequest(StaffSaveRequest request) {
        if (request == null || request.getStaffNo() == null || request.getStaffNo().isBlank()) {
            throw new BizException(StaffErrorCodes.INVALID_REQUIRED_FIELD, "工号不能为空");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BizException(StaffErrorCodes.INVALID_REQUIRED_FIELD, "姓名不能为空");
        }
        if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(StaffErrorCodes.INVALID_AMOUNT, "预算金额不能为负数");
        }
        if (request.getPeriod() != null && !request.getPeriod().isBlank()) {
            parsePeriod(request.getPeriod());
        }
    }

    private BudgetPeriod parsePeriod(String code) {
        try {
            return BudgetPeriod.fromCode(code);
        } catch (IllegalArgumentException e) {
            throw new BizException(StaffErrorCodes.INVALID_PERIOD, "预算周期无效，仅支持 MONTH/YEAR");
        }
    }

    // ---------- 组装 ----------

    private void applyRequest(StaffDO staff, StaffSaveRequest request) {
        staff.setStaffNo(request.getStaffNo().trim());
        staff.setName(request.getName().trim());
        staff.setDepartment(trimToNull(request.getDepartment()));
        staff.setPosition(trimToNull(request.getPosition()));
        staff.setEmail(trimToNull(request.getEmail()));
        staff.setHireDate(request.getHireDate());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private StaffDO requireStaff(Long id) {
        StaffDO staff = staffMapper.findById(id);
        if (staff == null) {
            throw new BizException(StaffErrorCodes.STAFF_NOT_FOUND, "员工不存在");
        }
        return staff;
    }

    private StaffRowVO toRowVO(StaffDO staff) {
        StaffRowVO vo = new StaffRowVO();
        vo.setId(staff.getId());
        vo.setStaffNo(staff.getStaffNo());
        vo.setName(staff.getName());
        vo.setDepartment(staff.getDepartment());
        vo.setPosition(staff.getPosition());
        StaffBudgetDO budget = budgetMapper.findByStaffId(staff.getId());
        if (budget != null) {
            vo.setAmount(budget.getAmount());
            vo.setPeriod(budget.getPeriod());
        }
        return vo;
    }

    private StaffDetailVO toDetailVO(StaffDO staff) {
        StaffDetailVO vo = new StaffDetailVO();
        vo.setId(staff.getId());
        vo.setStaffNo(staff.getStaffNo());
        vo.setName(staff.getName());
        vo.setDepartment(staff.getDepartment());
        vo.setPosition(staff.getPosition());
        vo.setEmail(staff.getEmail());
        vo.setHireDate(staff.getHireDate());
        vo.setGmtCreate(staff.getGmtCreate() == null ? null : staff.getGmtCreate().format(ISO_FORMATTER));
        StaffBudgetDO budget = budgetMapper.findByStaffId(staff.getId());
        if (budget != null) {
            vo.setAmount(budget.getAmount());
            vo.setPeriod(budget.getPeriod());
        }
        return vo;
    }

    private void insertBudget(Long staffId, StaffSaveRequest request) {
        BigDecimal amount = request.getAmount() == null ? BigDecimal.ZERO : request.getAmount();
        String period = (request.getPeriod() == null || request.getPeriod().isBlank())
                ? DEFAULT_PERIOD : request.getPeriod().trim();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));

        StaffBudgetDO budget = new StaffBudgetDO();
        budget.setStaffId(staffId);
        budget.setAmount(amount);
        budget.setPeriod(period);
        budget.setGmtCreate(now);
        budget.setGmtModified(now);
        budgetMapper.insert(budget);
    }

    private void upsertBudget(Long staffId, StaffSaveRequest request) {
        if (request.getAmount() == null && (request.getPeriod() == null || request.getPeriod().isBlank())) {
            return;
        }
        StaffBudgetDO existing = budgetMapper.findByStaffId(staffId);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        StaffBudgetDO budget = new StaffBudgetDO();
        budget.setStaffId(staffId);
        budget.setAmount(request.getAmount() == null
                ? (existing == null ? BigDecimal.ZERO : existing.getAmount())
                : request.getAmount());
        budget.setPeriod((request.getPeriod() == null || request.getPeriod().isBlank())
                ? (existing == null ? DEFAULT_PERIOD : existing.getPeriod())
                : request.getPeriod().trim());
        budget.setGmtModified(now);
        if (existing == null) {
            budget.setGmtCreate(now);
            budgetMapper.insert(budget);
        } else {
            budgetMapper.updateByStaffId(budget);
        }
    }
}
```

- [ ] **Step 3: 创建错误码常量类**

```java
package com.example.todo.staff.common;

/**
 * 人员看板业务错误码
 *
 * @author AiWork
 * @date 2026/10/21
 */
public final class StaffErrorCodes {

    private StaffErrorCodes() {
    }

    public static final String INVALID_REQUIRED_FIELD = "STAFF_001";
    public static final String DUPLICATE_STAFF_NO = "STAFF_002";
    public static final String STAFF_NOT_FOUND = "STAFF_003";
    public static final String INVALID_AMOUNT = "STAFF_004";
    public static final String INVALID_PERIOD = "STAFF_005";
    public static final String MISSING_ACCOUNT = "STAFF_006";
    public static final String IMPORT_ROWS_EXCEEDED = "STAFF_007";
    public static final String NOT_IN_WHITELIST = "STAFF_403";
    public static final String SYSTEM_ERROR = "STAFF_999";
}
```

> 注意：`StaffErrorCodes` 放在新增的 `com.example.todo.staff.common` 包，而非改动既有的 `com.example.todo.common`，避免污染现有模块。

- [ ] **Step 4: 编写 CSV 导入器（含白名单校验）**

`CsvImporter` 作为 `StaffServiceImpl` 的内部辅助类，放在同包 `impl` 下（文件 `CsvImporter.java`）。

```java
package com.example.todo.staff.service.impl;

import com.example.todo.common.BizException;
import com.example.todo.staff.common.StaffErrorCodes;
import com.example.todo.staff.dto.ImportFailureVO;
import com.example.todo.staff.dto.ImportResultVO;
import com.example.todo.staff.entity.BudgetPeriod;
import com.example.todo.staff.entity.StaffBudgetDO;
import com.example.todo.staff.entity.StaffDO;
import com.example.todo.staff.mapper.StaffBudgetMapper;
import com.example.todo.staff.mapper.StaffMapper;
import com.example.todo.staff.mapper.WhitelistMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 员工 CSV 批量导入器（白名单校验 + 逐行原子提交）
 *
 * <p>CSV 格式：首行表头，随后每行 8 列，顺序固定：
 * staff_no,name,department,position,email,hire_date,amount,period</p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
@Component
public class CsvImporter {

    private static final String[] EXPECTED_HEADER = {
            "staff_no", "name", "department", "position", "email", "hire_date", "amount", "period"
    };

    private final StaffMapper staffMapper;
    private final StaffBudgetMapper budgetMapper;
    private final WhitelistMapper whitelistMapper;

    @Value("${staff.import.max-rows:1000}")
    private int maxRows;

    public CsvImporter(StaffMapper staffMapper,
                       StaffBudgetMapper budgetMapper,
                       WhitelistMapper whitelistMapper) {
        this.staffMapper = staffMapper;
        this.budgetMapper = budgetMapper;
        this.whitelistMapper = whitelistMapper;
    }

    /**
     * 执行导入
     *
     * @param account 操作人账号（需在白名单内）
     * @param csvText CSV 文本内容
     * @return 导入结果
     */
    public ImportResultVO doImport(String account, String csvText) {
        checkWhitelist(account);

        if (csvText == null || csvText.isBlank()) {
            throw new BizException(StaffErrorCodes.SYSTEM_ERROR, "导入内容为空");
        }

        String[] lines = csvText.split("\\R", -1);
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            String[] cells = parseCsvLine(line);
            if (i == 0 && isHeader(cells)) {
                continue; // 跳过表头
            }
            rows.add(cells);
        }

        if (rows.size() > maxRows) {
            throw new BizException(StaffErrorCodes.IMPORT_ROWS_EXCEEDED,
                    "单次导入行数超过上限 " + maxRows);
        }

        ImportResultVO result = new ImportResultVO();
        result.setTotalRows(rows.size());
        List<ImportFailureVO> failures = new ArrayList<>();
        int successCount = 0;

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        for (int idx = 0; idx < rows.size(); idx++) {
            String[] cells = rows.get(idx);
            int rowNo = idx + 1;
            try {
                importRow(cells, now);
                successCount++;
            } catch (BizException e) {
                failures.add(new ImportFailureVO(rowNo, e.getMessage()));
            } catch (Exception e) {
                failures.add(new ImportFailureVO(rowNo, "解析失败: " + e.getMessage()));
            }
        }

        result.setSuccessCount(successCount);
        result.setFailureCount(failures.size());
        result.setFailures(failures);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void importRow(String[] cells, LocalDateTime now) {
        if (cells.length < 8) {
            throw new BizException(StaffErrorCodes.SYSTEM_ERROR, "列数不足 8 列");
        }
        String staffNo = trim(cells[0]);
        String name = trim(cells[1]);
        if (staffNo == null || staffNo.isEmpty()) {
            throw new BizException(StaffErrorCodes.INVALID_REQUIRED_FIELD, "工号不能为空");
        }
        if (name == null || name.isEmpty()) {
            throw new BizException(StaffErrorCodes.INVALID_REQUIRED_FIELD, "姓名不能为空");
        }
        if (staffMapper.findByStaffNo(staffNo) != null) {
            throw new BizException(StaffErrorCodes.DUPLICATE_STAFF_NO, "工号已存在: " + staffNo);
        }

        BigDecimal amount = parseAmount(cells[6]);
        String period = normalizePeriod(cells[7]);

        StaffDO staff = new StaffDO();
        staff.setStaffNo(staffNo);
        staff.setName(name);
        staff.setDepartment(trimOrNull(cells[2]));
        staff.setPosition(trimOrNull(cells[3]));
        staff.setEmail(trimOrNull(cells[4]));
        staff.setHireDate(parseDate(cells[5]));
        staff.setDeleted(0);
        staff.setGmtCreate(now);
        staff.setGmtModified(now);
        staffMapper.insert(staff);

        StaffBudgetDO budget = new StaffBudgetDO();
        budget.setStaffId(staff.getId());
        budget.setAmount(amount);
        budget.setPeriod(period);
        budget.setGmtCreate(now);
        budget.setGmtModified(now);
        budgetMapper.insert(budget);
    }

    private void checkWhitelist(String account) {
        if (account == null || account.isBlank()) {
            throw new BizException(StaffErrorCodes.MISSING_ACCOUNT, "缺少操作人账号");
        }
        if (whitelistMapper.findByAccount(account.trim()) == null) {
            throw new BizException(StaffErrorCodes.NOT_IN_WHITELIST, "无批量导入权限，账号不在白名单内");
        }
    }

    private boolean isHeader(String[] cells) {
        if (cells.length < 2) {
            return false;
        }
        String first = cells[0] == null ? "" : cells[0].trim().toLowerCase();
        String second = cells[1] == null ? "" : cells[1].trim().toLowerCase();
        return "staff_no".equals(first) && "name".equals(second);
    }

    private String[] parseCsvLine(String line) {
        // 简单实现：按逗号切分，去掉字段两端引号（CSV 无内嵌逗号场景）
        String[] cells = line.split(",", -1);
        for (int i = 0; i < cells.length; i++) {
            String c = cells[i] == null ? "" : cells[i].trim();
            if (c.length() >= 2 && c.startsWith("\"") && c.endsWith("\"")) {
                c = c.substring(1, c.length() - 1);
            }
            cells[i] = c;
        }
        return cells;
    }

    private BigDecimal parseAmount(String raw) {
        String value = trim(raw);
        if (value == null || value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal amount = new BigDecimal(value);
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(StaffErrorCodes.INVALID_AMOUNT, "预算金额不能为负数");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new BizException(StaffErrorCodes.INVALID_AMOUNT, "预算金额格式错误: " + raw);
        }
    }

    private String normalizePeriod(String raw) {
        String value = trim(raw);
        if (value == null || value.isEmpty()) {
            return BudgetPeriod.MONTH.getCode();
        }
        try {
            return BudgetPeriod.fromCode(value.toUpperCase()).getCode();
        } catch (IllegalArgumentException e) {
            throw new BizException(StaffErrorCodes.INVALID_PERIOD, "预算周期无效: " + raw);
        }
    }

    private LocalDate parseDate(String raw) {
        String value = trim(raw);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BizException(StaffErrorCodes.SYSTEM_ERROR, "入职日期格式错误(应为 YYYY-MM-DD): " + raw);
        }
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    private String trimOrNull(String s) {
        String v = trim(s);
        return (v == null || v.isEmpty()) ? null : v;
    }

    int getMaxRows() {
        return maxRows;
    }
}
```

- [ ] **Step 5: 编写 Service 单元测试（Mock Mapper）**

```java
package com.example.todo.staff.service;

import com.example.todo.common.BizException;
import com.example.todo.staff.common.StaffErrorCodes;
import com.example.todo.staff.dto.ImportResultVO;
import com.example.todo.staff.dto.StaffDetailVO;
import com.example.todo.staff.dto.StaffPageQuery;
import com.example.todo.staff.dto.StaffSaveRequest;
import com.example.todo.staff.entity.StaffBudgetDO;
import com.example.todo.staff.entity.StaffDO;
import com.example.todo.staff.mapper.StaffBudgetMapper;
import com.example.todo.staff.mapper.StaffMapper;
import com.example.todo.staff.mapper.WhitelistMapper;
import com.example.todo.staff.service.impl.CsvImporter;
import com.example.todo.staff.service.impl.StaffServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * StaffServiceImpl 单元测试
 *
 * @author AiWork
 * @date 2026/10/21
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StaffServiceImpl 单元测试")
class StaffServiceImplTest {

    @Mock
    private StaffMapper staffMapper;
    @Mock
    private StaffBudgetMapper budgetMapper;
    @Mock
    private WhitelistMapper whitelistMapper;

    @Mock
    private CsvImporter csvImporter;

    @InjectMocks
    private StaffServiceImpl staffService;

    private StaffSaveRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new StaffSaveRequest();
        validRequest.setStaffNo("S-001");
        validRequest.setName("张三");
        validRequest.setDepartment("研发部");
        validRequest.setAmount(new BigDecimal("12000.50"));
        validRequest.setPeriod("MONTH");
    }

    @Test
    @DisplayName("创建：工号不存在 → 插入员工与预算并返回详情")
    void should_create_when_staffNoAvailable() {
        when(staffMapper.findByStaffNo("S-001")).thenReturn(null);
        when(staffMapper.insert(any(StaffDO.class))).thenAnswer(inv -> {
            inv.getArgument(0, StaffDO.class).setId(1L);
            return 1;
        });
        when(staffMapper.findById(1L)).thenReturn(buildStaffDO());
        when(budgetMapper.findByStaffId(1L)).thenReturn(buildBudgetDO());

        StaffDetailVO result = staffService.create(validRequest);

        assertEquals(1L, result.getId());
        assertEquals("张三", result.getName());
        verify(staffMapper).insert(any(StaffDO.class));
        verify(budgetMapper).insert(any(StaffBudgetDO.class));
    }

    @Test
    @DisplayName("创建：工号已存在 → 抛 STAFF_002")
    void should_throw_when_staffNoExists() {
        when(staffMapper.findByStaffNo("S-001")).thenReturn(buildStaffDO());

        BizException ex = assertThrows(BizException.class, () -> staffService.create(validRequest));
        assertEquals(StaffErrorCodes.DUPLICATE_STAFF_NO, ex.getErrorCode());
        verify(staffMapper, never()).insert(any());
    }

    @Test
    @DisplayName("创建：工号为空 → 抛 STAFF_001")
    void should_throw_when_staffNoBlank() {
        validRequest.setStaffNo("  ");

        BizException ex = assertThrows(BizException.class, () -> staffService.create(validRequest));
        assertEquals(StaffErrorCodes.INVALID_REQUIRED_FIELD, ex.getErrorCode());
    }

    @Test
    @DisplayName("创建：预算为负 → 抛 STAFF_004")
    void should_throw_when_amountNegative() {
        validRequest.setAmount(new BigDecimal("-1"));

        BizException ex = assertThrows(BizException.class, () -> staffService.create(validRequest));
        assertEquals(StaffErrorCodes.INVALID_AMOUNT, ex.getErrorCode());
    }

    @Test
    @DisplayName("删除：员工不存在 → 抛 STAFF_003")
    void should_throw_when_deleteMissing() {
        when(staffMapper.findById(99L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> staffService.delete(99L));
        assertEquals(StaffErrorCodes.STAFF_NOT_FOUND, ex.getErrorCode());
        verify(staffMapper, never()).softDeleteById(anyLong());
    }

    @Test
    @DisplayName("导入：委托 CsvImporter 并返回结果")
    void should_delegate_import() {
        ImportResultVO expected = new ImportResultVO();
        expected.setTotalRows(1);
        expected.setSuccessCount(1);
        expected.setFailureCount(0);
        expected.setFailures(Collections.emptyList());
        when(csvImporter.doImport("admin", "staff_no,name")).thenReturn(expected);

        ImportResultVO result = staffService.importCsv("admin", "staff_no,name");
        assertEquals(1, result.getSuccessCount());
        verify(csvImporter).doImport(eq("admin"), eq("staff_no,name"));
    }

    private StaffDO buildStaffDO() {
        StaffDO staff = new StaffDO();
        staff.setId(1L);
        staff.setStaffNo("S-001");
        staff.setName("张三");
        staff.setDepartment("研发部");
        staff.setGmtCreate(LocalDateTime.now());
        staff.setGmtModified(LocalDateTime.now());
        return staff;
    }

    private StaffBudgetDO buildBudgetDO() {
        StaffBudgetDO budget = new StaffBudgetDO();
        budget.setId(1L);
        budget.setStaffId(1L);
        budget.setAmount(new BigDecimal("12000.50"));
        budget.setPeriod("MONTH");
        return budget;
    }
}
```

- [ ] **Step 6: 编写 CsvImporter 单测（白名单 + 解析）**

```java
package com.example.todo.staff.service.impl;

import com.example.todo.common.BizException;
import com.example.todo.staff.common.StaffErrorCodes;
import com.example.todo.staff.dto.ImportResultVO;
import com.example.todo.staff.entity.StaffDO;
import com.example.todo.staff.entity.WhitelistDO;
import com.example.todo.staff.mapper.StaffBudgetMapper;
import com.example.todo.staff.mapper.StaffMapper;
import com.example.todo.staff.mapper.WhitelistMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * CsvImporter 单元测试
 *
 * @author AiWork
 * @date 2026/10/21
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CsvImporter 单元测试")
class CsvImporterTest {

    @Mock
    private StaffMapper staffMapper;
    @Mock
    private StaffBudgetMapper budgetMapper;
    @Mock
    private WhitelistMapper whitelistMapper;

    private CsvImporter importer;

    @BeforeEach
    void setUp() {
        importer = new CsvImporter(staffMapper, budgetMapper, whitelistMapper);
        ReflectionTestUtils.setField(importer, "maxRows", 1000);
    }

    @Test
    @DisplayName("账号不在白名单 → 抛 STAFF_403")
    void should_reject_nonWhitelistedAccount() {
        when(whitelistMapper.findByAccount("guest")).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> importer.doImport("guest", "staff_no,name\nS-001,张三"));
        assertEquals(StaffErrorCodes.NOT_IN_WHITELIST, ex.getErrorCode());
    }

    @Test
    @DisplayName("白名单账号导入 → 成功解析并写入")
    void should_import_rows_forWhitelistedAccount() {
        WhitelistDO allowed = new WhitelistDO();
        allowed.setId(1L);
        allowed.setAccount("admin");
        when(whitelistMapper.findByAccount("admin")).thenReturn(allowed);
        when(staffMapper.findByStaffNo(anyString())).thenReturn(null);
        when(staffMapper.insert(any(StaffDO.class))).thenAnswer(inv -> {
            inv.getArgument(0, StaffDO.class).setId(1L);
            return 1;
        });

        String csv = "staff_no,name,department,position,email,hire_date,amount,period\n"
                + "S-001,张三,研发部,工程师,zhang@x.com,2026-01-05,12000.5,MONTH";
        ImportResultVO result = importer.doImport("admin", csv);

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
    }

    @Test
    @DisplayName("单行工号重复 → 失败明细记录，其他行成功")
    void should_record_failure_when_duplicateRow() {
        WhitelistDO allowed = new WhitelistDO();
        allowed.setId(1L);
        allowed.setAccount("admin");
        when(whitelistMapper.findByAccount("admin")).thenReturn(allowed);
        when(staffMapper.findByStaffNo("S-001")).thenReturn(null);
        when(staffMapper.findByStaffNo("S-002")).thenReturn(new StaffDO());
        when(staffMapper.insert(any(StaffDO.class))).thenAnswer(inv -> {
            inv.getArgument(0, StaffDO.class).setId(1L);
            return 1;
        });

        String csv = "staff_no,name,department,position,email,hire_date,amount,period\n"
                + "S-001,张三,研发部,工程师,zhang@x.com,2026-01-05,12000,MONTH\n"
                + "S-002,李四,财务部,会计,li@x.com,2026-02-01,8000,YEAR";
        ImportResultVO result = importer.doImport("admin", csv);

        assertEquals(2, result.getTotalRows());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(2, result.getFailures().get(0).getRowNo());
    }
}
```

- [ ] **Step 7: 运行测试验证通过**

Run: `mvn -q -Dtest=StaffServiceImplTest,CsvImporterTest test`
Expected: BUILD SUCCESS，所有用例通过。

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/example/todo/staff/service/ src/main/java/com/example/todo/staff/common/ src/test/java/com/example/todo/staff/service/
git commit -m "feat(staff): add staff service with CRUD, whitelist and CSV import"
```

---

## Task 5: Controller 层 —— REST 端点

**Files:**
- Create: `src/main/java/com/example/todo/staff/controller/StaffController.java`
- Test: `src/test/java/com/example/todo/staff/controller/StaffControllerTest.java`

**Interfaces:**
- Consumes: `StaffService`（Task 4）。
- Produces: 对外 REST 端点（`Result<T>` 统一包装）：
  - `GET /api/staff` → `Result<PageVO<StaffRowVO>>`
  - `GET /api/staff/{id}` → `Result<StaffDetailVO>`
  - `POST /api/staff` → `Result<StaffDetailVO>`
  - `PUT /api/staff/{id}` → `Result<StaffDetailVO>`
  - `DELETE /api/staff/{id}` → `Result<Void>`
  - `POST /api/staff/import` → `Result<ImportResultVO>`（header `X-Operator-Account` + body 文本 `csvText`）

- [ ] **Step 1: 编写 StaffController**

```java
package com.example.todo.staff.controller;

import com.example.todo.common.BizException;
import com.example.todo.common.Result;
import com.example.todo.staff.dto.ImportResultVO;
import com.example.todo.staff.dto.PageVO;
import com.example.todo.staff.dto.StaffDetailVO;
import com.example.todo.staff.dto.StaffPageQuery;
import com.example.todo.staff.dto.StaffRowVO;
import com.example.todo.staff.dto.StaffSaveRequest;
import com.example.todo.staff.service.StaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 人员看板控制器
 *
 * @author AiWork
 * @date 2026/10/21
 */
@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private static final Logger log = LoggerFactory.getLogger(StaffController.class);

    private final StaffService staffService;

    @Value("${staff.feature.enabled:true}")
    private boolean featureEnabled;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ResponseEntity<Result<PageVO<StaffRowVO>>> page(StaffPageQuery query) {
        if (!featureEnabled) {
            return maintenance();
        }
        try {
            return ResponseEntity.ok(Result.success(staffService.page(query)));
        } catch (Exception e) {
            log.error("GET /api/staff ERROR", e);
            return serverError();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<StaffDetailVO>> getDetail(@PathVariable Long id) {
        if (!featureEnabled) {
            return maintenance();
        }
        try {
            return ResponseEntity.ok(Result.success(staffService.getDetail(id)));
        } catch (BizException e) {
            return badRequest(e);
        } catch (Exception e) {
            log.error("GET /api/staff/{} ERROR", id, e);
            return serverError();
        }
    }

    @PostMapping
    public ResponseEntity<Result<StaffDetailVO>> create(@RequestBody StaffSaveRequest request) {
        if (!featureEnabled) {
            return maintenance();
        }
        try {
            return ResponseEntity.ok(Result.success(staffService.create(request)));
        } catch (BizException e) {
            return badRequest(e);
        } catch (Exception e) {
            log.error("POST /api/staff ERROR", e);
            return serverError();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<StaffDetailVO>> update(@PathVariable Long id,
                                                        @RequestBody StaffSaveRequest request) {
        if (!featureEnabled) {
            return maintenance();
        }
        try {
            return ResponseEntity.ok(Result.success(staffService.update(id, request)));
        } catch (BizException e) {
            return badRequest(e);
        } catch (Exception e) {
            log.error("PUT /api/staff/{} ERROR", id, e);
            return serverError();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable Long id) {
        if (!featureEnabled) {
            return maintenance();
        }
        try {
            staffService.delete(id);
            return ResponseEntity.ok(Result.success(null));
        } catch (BizException e) {
            return badRequest(e);
        } catch (Exception e) {
            log.error("DELETE /api/staff/{} ERROR", id, e);
            return serverError();
        }
    }

    @PostMapping("/import")
    public ResponseEntity<Result<ImportResultVO>> importCsv(
            @RequestHeader(value = "X-Operator-Account", required = false) String account,
            @RequestBody Map<String, String> body) {
        if (!featureEnabled) {
            return maintenance();
        }
        String csvText = body == null ? null : body.get("csvText");
        try {
            return ResponseEntity.ok(Result.success(staffService.importCsv(account, csvText)));
        } catch (BizException e) {
            return badRequest(e);
        } catch (Exception e) {
            log.error("POST /api/staff/import ERROR", e);
            return serverError();
        }
    }

    private <T> ResponseEntity<Result<T>> badRequest(BizException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(e.getErrorCode(), e.getMessage()));
    }

    private <T> ResponseEntity<Result<T>> serverError() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail("STAFF_999", "系统内部错误"));
    }

    private <T> ResponseEntity<Result<T>> maintenance() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Result.fail("STAFF_998", "功能维护中"));
    }
}
```

- [ ] **Step 2: 编写 Controller 单元测试（Mock Service）**

```java
package com.example.todo.staff.controller;

import com.example.todo.common.BizException;
import com.example.todo.common.Result;
import com.example.todo.staff.common.StaffErrorCodes;
import com.example.todo.staff.dto.StaffDetailVO;
import com.example.todo.staff.service.StaffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * StaffController 单元测试
 *
 * @author AiWork
 * @date 2026/10/21
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StaffController 单元测试")
class StaffControllerTest {

    @Mock
    private StaffService staffService;

    private StaffController controller;

    @BeforeEach
    void setUp() {
        controller = new StaffController(staffService);
        ReflectionTestUtils.setField(controller, "featureEnabled", true);
    }

    @Test
    @DisplayName("查询详情 → 返回 200 及员工")
    void should_returnDetail() {
        StaffDetailVO vo = new StaffDetailVO();
        vo.setId(1L);
        vo.setName("张三");
        when(staffService.getDetail(1L)).thenReturn(vo);

        ResponseEntity<Result<StaffDetailVO>> response = controller.getDetail(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OK", response.getBody().getCode());
        assertEquals("张三", response.getBody().getData().getName());
    }

    @Test
    @DisplayName("Service 抛 BizException → 返回 400")
    void should_return400_onBizException() {
        when(staffService.getDetail(99L)).thenThrow(new BizException(StaffErrorCodes.STAFF_NOT_FOUND, "员工不存在"));

        ResponseEntity<Result<StaffDetailVO>> response = controller.getDetail(99L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(StaffErrorCodes.STAFF_NOT_FOUND, response.getBody().getCode());
    }

    @Test
    @DisplayName("未知异常 → 返回 500")
    void should_return500_onUnknownError() {
        when(staffService.getDetail(anyLong())).thenThrow(new RuntimeException("boom"));

        ResponseEntity<Result<StaffDetailVO>> response = controller.getDetail(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("STAFF_999", response.getBody().getCode());
    }

    @Test
    @DisplayName("功能开关关闭 → 返回 503")
    void should_return503_whenDisabled() {
        ReflectionTestUtils.setField(controller, "featureEnabled", false);

        ResponseEntity<Result<StaffDetailVO>> response = controller.getDetail(1L);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("STAFF_998", response.getBody().getCode());
    }

    @Test
    @DisplayName("导入：透传账号与 CSV 文本")
    void should_delegateImport() {
        Map<String, String> body = new HashMap<>();
        body.put("csvText", "staff_no,name");
        when(staffService.importCsv("admin", "staff_no,name")).thenReturn(null);

        ResponseEntity<Result<com.example.todo.staff.dto.ImportResultVO>> response =
                controller.importCsv("admin", body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

- [ ] **Step 3: 运行测试验证通过**

Run: `mvn -q -Dtest=StaffControllerTest test`
Expected: BUILD SUCCESS，5 个用例通过。

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/todo/staff/controller/ src/test/java/com/example/todo/staff/controller/
git commit -m "feat(staff): add staff REST controller"
```

---

## Task 6: 配置与监控收尾 + 全量验证

**Files:**
- Modify: `src/main/resources/application.yml`（追加 `staff.*` 配置）
- Modify: `docs/modules/staff/README.md`（新建模块说明）

**Interfaces:**
- Consumes: Task 1–5 全部产物。
- Produces: 配置键 `staff.feature.enabled`、`staff.import.max-rows`；模块文档。

- [ ] **Step 1: 追加 staff 配置到 application.yml**

在 `application.yml` 文件末尾追加：

```yaml
# 人员看板
staff:
  feature:
    enabled: true
  import:
    max-rows: 1000
```

- [ ] **Step 2: 编写模块 README**

```markdown
# 人员看板模块（staff）

负责员工基本信息增删改查、成本预算记录与批量导入（白名单保护）。

## 数据表

- `staff`：员工表（工号唯一，逻辑删除）
- `staff_budget`：员工成本预算表（金额 + 周期 MONTH/YEAR）
- `staff_whitelist`：导入白名单表（唯一账号）

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /api/staff | 分页 + 条件查询（name/staffNo/department） |
| GET | /api/staff/{id} | 员工详情（含预算） |
| POST | /api/staff | 新增员工及预算 |
| PUT | /api/staff/{id} | 更新员工及预算 |
| DELETE | /api/staff/{id} | 逻辑删除 |
| POST | /api/staff/import | 批量导入（header X-Operator-Account 白名单校验） |

导入 CSV 列顺序：`staff_no,name,department,position,email,hire_date,amount,period`（首行表头，每行 8 列）。
```

- [ ] **Step 3: 全量测试**

Run: `mvn -q test`
Expected: BUILD SUCCESS，包含既有 todo 测试与新增 staff 测试全部通过。

- [ ] **Step 4: 冒烟启动（可选，验证上下文装配）**

Run: `mvn -q -DskipTests package` 然后 `java -jar target/todo-app-1.0.0-SNAPSHOT.jar --server.port=0`
Expected: 应用成功启动并扫描到 StaffController（日志无 Bean 冲突错误）。启动后 Ctrl+C 退出。

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/application.yml docs/modules/staff/README.md
git commit -m "feat(staff): add staff config and module docs"
```

---

## Self-Review

**1. Spec coverage：**
| Spec 能力点/F | 覆盖任务 |
| --- | --- |
| F1 看板展示（列表/筛选/汇总） | Task 4 `page()` + Task 5 `GET /api/staff`；列表分页与条件检索覆盖；看板在册人数=分页 `total`，预算总额当前未聚合到列表响应（如需「预算汇总」指标需扩展 `StatsVO`，见风险） |
| F2 录入入口 | Task 4 `create()` + Task 5 `POST /api/staff` |
| F3 CRUD | Task 4/5 增删改查全部覆盖（删除为逻辑删除 R5） |
| F4 成本预算记录 | Task 1 `staff_budget` + Task 4 `insertBudget/upsertBudget` + 详情/列表返回预算 |
| F5 导入（批量+白名单） | Task 4 `CsvImporter`（白名单 R4、工号唯一 R1、必填 R2、金额格式 R3、逐行原子 R6）+ Task 5 `POST /api/staff/import` |
| R1–R6 业务规则 | 全部在 Service 校验与三表 schema 中落实 |

**未覆盖项（有意排除或需决策）：**
- 「看板预算总额汇总」指标：spec F1 提到「预算总额概览指标」，本期计划仅在详情/列表中返回单员工预算，未做看板级求和聚合。实现阶段如需，新增 `StatsVO(total, budgetSum)` 与 `StaffMapper.sumBudget()`，作为可选项（不回退主路径）。
- 「白名单 CRUD 管理界面」：spec 8.2 A02 未明确是否需要白名单维护接口，本期仅提供表 + 校验，白名单数据由运维/初始化脚本写入；如需管理接口，追加 `POST/DELETE /api/staff/whitelist`。
- `.xlsx` 导入：Global Constraints 已收敛为 CSV；`.xlsx` 待后续引入 Apache POI/EasyExcel。

**2. Placeholder scan：** 无 TBD/TODO/“适当处理”；所有代码步骤含完整代码、命令与期望输出。

**3. Type consistency：** `StaffServiceImpl.create/update/getDetail/delete` 返回值与 `StaffService` 接口一致；`CsvImporter.doImport(String,String)` 由 `StaffServiceImpl.importCsv(String,String)` 委托；错误码常量集中在 `StaffErrorCodes` 且与测试断言字符串一致；`StaffPageQuery.getOffset()` 供 `StaffMapper.xml` 的 `#{offset}` 引用（需 MyBatis 可访问 getter，已提供）。

## 跨仓对齐点检查

- **两 worktree 关系**：`testllw-main`（branch `AI/task-...-1`，需求/计划工作区）与 `testllw-AI-task-...`（branch `AI/task-...-2`，代码仓库）为同一仓库 `testllw` 的不同分支。实现计划落在代码仓库 worktree 的 `docs/superpowers/plans/`。
- **复用既有契约**：新模块复用 `com.example.todo.common.Result`/`BizException`，不修改既有 todo 模块源码，避免破坏既有 `POST /api/todo/create` 契约（向后兼容）。
- **入参/出参类型**：`Result<T>` 泛型包装 + 200/400/500/503 状态码与既有 todo 模块一致；新增错误码以 `STAFF_` 前缀隔离，不侵占 `TODO_` 命名空间。
- **依赖**：不新增任何 Maven 依赖（CSV 用 JDK 原生解析），`pom.xml` 零改动，消除跨库依赖风险。

## 风险与遗留

- 预算看板级汇总指标未纳入主路径（见 Self-Review）。
- 白名单管理接口未纳入主路径（仅表 + 校验）。
- CSV 解析为简化实现，不支持字段内嵌逗号/引号转义；若导入文件含此类内容，需升级为 RFC 4180 解析器（后续迭代项）。