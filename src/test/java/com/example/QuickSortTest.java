package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuickSort 单元测试类。
 * <p>
 * 覆盖正常路径、边界值、异常路径及特殊场景。
 * </p>
 *
 * @author AiWork
 * @date 2026/10/20
 */
@DisplayName("QuickSort 单元测试")
class QuickSortTest {

    // ==================== quickSort(T[]) 测试 ====================

    @Test
    @DisplayName("正常乱序数组 → 升序排列")
    void should_sortAscending_when_unsortedArray() {
        // Arrange
        Integer[] arr = {5, 2, 8, 1, 9, 3};

        // Act
        QuickSort.quickSort(arr);

        // Assert
        assertArrayEquals(new Integer[]{1, 2, 3, 5, 8, 9}, arr);
    }

    @Test
    @DisplayName("已排序数组 → 保持不变")
    void should_keepOrder_when_alreadySorted() {
        // Arrange
        Integer[] arr = {1, 2, 3, 4, 5};

        // Act
        QuickSort.quickSort(arr);

        // Assert
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, arr);
    }

    @Test
    @DisplayName("逆序数组 → 升序排列")
    void should_sortAscending_when_reverseSorted() {
        // Arrange
        Integer[] arr = {9, 7, 5, 3, 1};

        // Act
        QuickSort.quickSort(arr);

        // Assert
        assertArrayEquals(new Integer[]{1, 3, 5, 7, 9}, arr);
    }

    @Test
    @DisplayName("含重复元素数组 → 稳定升序排列")
    void should_sortAscending_when_hasDuplicates() {
        // Arrange
        Integer[] arr = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};

        // Act
        QuickSort.quickSort(arr);

        // Assert
        assertArrayEquals(new Integer[]{1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9}, arr);
    }

    @Test
    @DisplayName("单元素数组 → 保持不变")
    void should_keepSingleElement_when_singleElementArray() {
        // Arrange
        Integer[] arr = {42};

        // Act
        QuickSort.quickSort(arr);

        // Assert
        assertArrayEquals(new Integer[]{42}, arr);
    }

    @Test
    @DisplayName("空数组 → 不抛异常")
    void should_notThrowException_when_emptyArray() {
        // Arrange
        Integer[] arr = {};

        // Act & Assert
        assertDoesNotThrow(() -> QuickSort.quickSort(arr));
    }

    @Test
    @DisplayName("null 数组 → 抛出 IllegalArgumentException")
    void should_throwException_when_nullArray() {
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> QuickSort.quickSort((Integer[]) null));
        assertEquals("数组不能为 null", ex.getMessage());
    }

    @Test
    @DisplayName("字符串数组 → 字典序升序排列")
    void should_sortLexicographically_when_stringArray() {
        // Arrange
        String[] arr = {"banana", "apple", "cherry", "date"};

        // Act
        QuickSort.quickSort(arr);

        // Assert
        assertArrayEquals(new String[]{"apple", "banana", "cherry", "date"}, arr);
    }

    @Test
    @DisplayName("两元素数组 → 交换排序")
    void should_swap_when_twoElementOutOfOrder() {
        // Arrange
        Integer[] arr = {2, 1};

        // Act
        QuickSort.quickSort(arr);

        // Assert
        assertArrayEquals(new Integer[]{1, 2}, arr);
    }

    @Test
    @DisplayName("全相同元素数组 → 保持不变")
    void should_keepOrder_when_allElementsEqual() {
        // Arrange
        Integer[] arr = {5, 5, 5, 5, 5};

        // Act
        QuickSort.quickSort(arr);

        // Assert
        assertArrayEquals(new Integer[]{5, 5, 5, 5, 5}, arr);
    }

    @Test
    @DisplayName("大数组 → 正确快速排序")
    void should_sortCorrectly_when_largeArray() {
        // Arrange
        int size = 100;
        Integer[] arr = new Integer[size];
        Integer[] expected = new Integer[size];
        for (int i = 0; i < size; i++) {
            arr[i] = size - i;
            expected[i] = i + 1;
        }

        // Act
        QuickSort.quickSort(arr);

        // Assert
        assertArrayEquals(expected, arr);
    }
}