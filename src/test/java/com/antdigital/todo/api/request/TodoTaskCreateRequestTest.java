package com.antdigital.todo.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.antdigital.todo.common.constant.TodoTaskConstants;
import com.antdigital.todo.common.constant.TodoTipConstants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link TodoTaskCreateRequest} 声明式校验（R01/R02）与名称规范化测试。
 *
 * @author AiWork
 * @date 2026/10/13
 */
class TodoTaskCreateRequestTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @Test
    void should_haveNoViolation_when_requestValid() {
        // Arrange
        TodoTaskCreateRequest request = buildRequest("整理季度复盘材料", "收集各组数据");

        // Act
        Set<ConstraintViolation<TodoTaskCreateRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void should_stripSurroundingSpaces_when_setTaskName() {
        // Arrange & Act
        TodoTaskCreateRequest request = buildRequest("  整理季度复盘材料  ", null);

        // Assert
        assertThat(request.getTaskName()).isEqualTo("整理季度复盘材料");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "　"})
    void should_violate_when_taskNameIsBlank(String taskName) {
        // Arrange
        TodoTaskCreateRequest request = buildRequest(taskName, null);

        // Act
        Set<ConstraintViolation<TodoTaskCreateRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo(TodoTipConstants.TASK_NAME_INVALID);
    }

    @Test
    void should_violate_when_taskNameExceedsMaxLength() {
        // Arrange
        TodoTaskCreateRequest request = buildRequest("a".repeat(TodoTaskConstants.TASK_NAME_MAX_LEN + 1), null);

        // Act
        Set<ConstraintViolation<TodoTaskCreateRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo(TodoTipConstants.TASK_NAME_INVALID);
    }

    @Test
    void should_notViolate_when_taskNameLengthIsMaxBoundary() {
        // Arrange
        TodoTaskCreateRequest request = buildRequest("a".repeat(TodoTaskConstants.TASK_NAME_MAX_LEN), null);

        // Act
        Set<ConstraintViolation<TodoTaskCreateRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void should_violate_when_taskDescExceedsMaxLength() {
        // Arrange
        TodoTaskCreateRequest request = buildRequest("整理季度复盘材料",
                "d".repeat(TodoTaskConstants.TASK_DESC_MAX_LEN + 1));

        // Act
        Set<ConstraintViolation<TodoTaskCreateRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo(TodoTipConstants.TASK_DESC_TOO_LONG);
    }

    @Test
    void should_notViolate_when_taskDescIsNull() {
        // Arrange
        TodoTaskCreateRequest request = buildRequest("整理季度复盘材料", null);

        // Act
        Set<ConstraintViolation<TodoTaskCreateRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void should_notViolate_when_taskDescLengthIsMaxBoundary() {
        // Arrange
        TodoTaskCreateRequest request = buildRequest("整理季度复盘材料",
                "d".repeat(TodoTaskConstants.TASK_DESC_MAX_LEN));

        // Act
        Set<ConstraintViolation<TodoTaskCreateRequest>> violations = VALIDATOR.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    private TodoTaskCreateRequest buildRequest(String taskName, String taskDesc) {
        TodoTaskCreateRequest request = new TodoTaskCreateRequest();
        request.setTaskName(taskName);
        request.setTaskDesc(taskDesc);
        return request;
    }
}
