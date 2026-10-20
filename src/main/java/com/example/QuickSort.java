package com.example;

/**
 * 快速排序工具类。
 * <p>
 * 提供原地快速排序算法，基于经典的分治策略：
 * 选取基准元素（pivot），将数组分区为小于和大于基准的两部分，
 * 递归排序子数组。
 * </p>
 *
 * <p>使用范例如下：</p>
 * <pre>
 * Integer[] arr = {5, 2, 8, 1, 9};
 * QuickSort.quickSort(arr);
 * // arr 变为 {1, 2, 5, 8, 9}
 * </pre>
 *
 * @author AiWork
 * @date 2026/10/20
 */
public class QuickSort {

    private QuickSort() {
        // 工具类禁止实例化
    }

    /**
     * 对数组进行原地快速排序（升序）。
     * <p>
     * 数组元素必须实现 {@link Comparable} 接口。
     * </p>
     *
     * @param <T> 元素类型，必须可比较
     * @param arr 待排序数组，排序结果直接反映在原数组中
     * @throws IllegalArgumentException 当 arr 为 null 时抛出
     */
    public static <T extends Comparable<? super T>> void quickSort(T[] arr) {
        if (arr == null) {
            throw new IllegalArgumentException("数组不能为 null");
        }
        if (arr.length <= 1) {
            return;
        }
        quickSort(arr, 0, arr.length - 1);
    }

    /**
     * 对数组指定子区间进行递归快速排序。
     *
     * @param arr  待排序数组
     * @param low  子区间左边界（包含）
     * @param high 子区间右边界（包含）
     * @param <T>  元素类型
     */
    private static <T extends Comparable<? super T>> void quickSort(T[] arr, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(arr, low, high);
            quickSort(arr, low, pivotIndex - 1);
            quickSort(arr, pivotIndex + 1, high);
        }
    }

    /**
     * 分区操作：以区间最右元素为基准，将小于基准的元素移至左侧。
     * <p>
     * 采用 Lomuto 分区方案：使用单指针遍历，将小于 pivot 的元素
     * 依次交换到数组左端，最终将 pivot 放置到正确位置。
     * </p>
     *
     * @param arr  待分区数组
     * @param low  区间左边界
     * @param high 区间右边界（同时作为 pivot 位置）
     * @param <T>  元素类型
     * @return pivot 元素的最终索引位置
     */
    private static <T extends Comparable<? super T>> int partition(T[] arr, int low, int high) {
        T pivot = arr[high];

        // i 指向最后一个小于 pivot 的元素位置
        int i = low - 1;

        for (int j = low; j < high; j++) {
            // 当前元素小于等于 pivot，交换到左侧
            if (arr[j].compareTo(pivot) <= 0) {
                i++;
                swap(arr, i, j);
            }
        }

        // 将 pivot 放置到正确位置
        swap(arr, i + 1, high);
        return i + 1;
    }

    /**
     * 交换数组中两个位置的元素。
     *
     * @param arr 目标数组
     * @param i   第一个位置
     * @param j   第二个位置
     * @param <T> 元素类型
     */
    private static <T> void swap(T[] arr, int i, int j) {
        T temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}