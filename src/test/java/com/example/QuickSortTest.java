package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * QuickSort 快速排序单元测试。
 *
 * @author AiWork
 * @date 2026/10/20
 */
@DisplayName("快速排序算法测试")
class QuickSortTest {

    // ==================== sort 正常路径测试 ====================

    @Test
    @DisplayName("应对乱序数组时正确升序排序")
    void should_sortInAscendingOrder_when_arrayIsUnsorted() {
        // Arrange
        int[] input = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};

        // Act
        QuickSort.sort(input);

        // Assert
        assertThat(input).containsExactly(1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9);
    }

    @Test
    @DisplayName("应对已排序数组时不破坏原有顺序")
    void should_keepOrder_when_arrayIsAlreadySorted() {
        // Arrange
        int[] input = {1, 2, 3, 4, 5};

        // Act
        QuickSort.sort(input);

        // Assert
        assertThat(input).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @DisplayName("应对逆序数组时正确反转排序")
    void should_sortInAscendingOrder_when_arrayIsReversed() {
        // Arrange
        int[] input = {9, 8, 7, 6, 5, 4, 3, 2, 1};

        // Act
        QuickSort.sort(input);

        // Assert
        assertThat(input).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    @DisplayName("应对有重复元素时正确排序并保留重复项")
    void should_retainDuplicates_when_arrayHasDuplicates() {
        // Arrange
        int[] input = {2, 2, 2, 1, 1, 3, 3};

        // Act
        QuickSort.sort(input);

        // Assert
        assertThat(input).containsExactly(1, 1, 2, 2, 2, 3, 3);
    }

    @Test
    @DisplayName("应对含负数数组时正确排序")
    void should_sortCorrectly_when_arrayContainsNegativeNumbers() {
        // Arrange
        int[] input = {-3, 0, -1, 5, 2, -4};

        // Act
        QuickSort.sort(input);

        // Assert
        assertThat(input).containsExactly(-4, -3, -1, 0, 2, 5);
    }

    // ==================== sort 边界值测试 ====================

    @Test
    @DisplayName("应对空数组时不抛异常")
    void should_notThrowException_when_arrayIsEmpty() {
        // Arrange
        int[] input = {};

        // Act & Assert
        assertThatCode(() -> QuickSort.sort(input))
                .as("空数组排序不应抛异常")
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("应对单元素数组时不改变数组")
    void should_keepSingleElement_when_arrayHasOneElement() {
        // Arrange
        int[] input = {42};

        // Act
        QuickSort.sort(input);

        // Assert
        assertThat(input).containsExactly(42);
    }

    @Test
    @DisplayName("应对双元素数组时正确排序")
    void should_sortTwoElements_when_arrayHasTwoElements() {
        // Arrange
        int[] input1 = {5, 2};
        int[] input2 = {2, 5};

        // Act
        QuickSort.sort(input1);
        QuickSort.sort(input2);

        // Assert
        assertThat(input1).containsExactly(2, 5);
        assertThat(input2).containsExactly(2, 5);
    }

    @Test
    @DisplayName("应对已有序的大数组时正确排序（回归验证）")
    void should_sortCorrectly_when_arrayIsLarge() {
        // Arrange
        int size = 1000;
        int[] input = new int[size];
        for (int i = 0; i < size; i++) {
            input[i] = size - i;
        }

        // Act
        QuickSort.sort(input);

        // Assert
        assertThat(input).isSorted();
        assertThat(input).hasSize(size);
    }

    // ==================== sort 异常路径测试 ====================

    @Test
    @DisplayName("应对 null 数组时抛 NullPointerException")
    void should_throwNullPointerException_when_arrayIsNull() {
        // Act & Assert
        assertThatCode(() -> QuickSort.sort(null))
                .as("null 数组应抛 NullPointerException")
                .isInstanceOf(NullPointerException.class);
    }
}