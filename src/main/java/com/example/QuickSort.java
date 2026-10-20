package com.example;

/**
 * 快速排序工具类，提供 int 数组的原地升序排序。
 * <p>
 * 采用经典 Lomuto 分区方案，选取区间最右元素为基准值（pivot），
 * 递归对左右子区间进行排序。平均时间复杂度 O(n log n)，最坏 O(n²)。
 * </p>
 *
 * <pre>
 * 使用示例：
 *   int[] arr = {3, 1, 4, 1, 5};
 *   QuickSort.sort(arr);
 *   // arr 变为 {1, 1, 3, 4, 5}
 * </pre>
 *
 * @author AiWork
 * @date 2026/10/20
 */
public final class QuickSort {

    /**
     * 工具类私有构造方法，禁止实例化。
     */
    private QuickSort() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 对 int 数组进行原地升序快速排序。
     * <p>
     * 数组为 null 时抛出 NullPointerException；空数组或单元素数组不做任何操作直接返回。
     * </p>
     *
     * @param arr 待排序的 int 数组，可为空但不能为 null
     * @throws NullPointerException 如果 arr 为 null
     */
    public static void sort(int[] arr) {
        if (arr == null) {
            throw new NullPointerException("arr must not be null");
        }

        if (arr.length <= 1) {
            return;
        }

        quickSort(arr, 0, arr.length - 1);
    }

    /**
     * 递归快速排序的核心方法，对 arr[low..high] 区间进行原地升序排序。
     *
     * @param arr  待排序数组
     * @param low  区间左边界（含）
     * @param high 区间右边界（含）
     */
    private static void quickSort(int[] arr, int low, int high) {
        if (low >= high) {
            return;
        }

        int pivotIndex = partition(arr, low, high);

        quickSort(arr, low, pivotIndex - 1);
        quickSort(arr, pivotIndex + 1, high);
    }

    /**
     * Lomuto 分区操作：选取 arr[high] 为基准值，将区间内小于基准值的元素移至左侧，
     * 大于等于基准值的元素移至右侧，返回基准值的最终位置。
     *
     * @param arr  待排序数组
     * @param low  区间左边界（含）
     * @param high 区间右边界（含），同时也是基准值所在位置
     * @return 基准值在排序完成后的位置索引
     */
    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        // i 指向"小于基准值区域"的最后一个元素位置
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                swap(arr, i, j);
            }
        }

        // 将基准值放到正确位置
        swap(arr, i + 1, high);

        return i + 1;
    }

    /**
     * 交换数组中两个位置的元素。
     *
     * @param arr 数组
     * @param a   第一个位置索引
     * @param b   第二个位置索引
     */
    private static void swap(int[] arr, int a, int b) {
        int temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
}