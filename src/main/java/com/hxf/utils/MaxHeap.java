package com.hxf.utils;

import java.util.Arrays;

/**
 * Created by fangqing on 12/17/17.
 */

public class MaxHeap {
    int[] heap;
    int heapsize;

    public MaxHeap(int[] array) {
        this.heap = array;
        this.heapsize = heap.length;
    }

    public void BuildMaxHeap() {
        for (int i = heapsize / 2 - 1; i >= 0; i--) {
            Maxify(i);//依次向上将当前子树最大堆化
        }
    }

    public void HeapSort() {
        for (int i = 0; i < heap.length; i++) {
            //执行n次，将每个当前最大的值放到堆末尾
            int tmp = heap[0];
            heap[0] = heap[heapsize - 1];
            heap[heapsize - 1] = tmp;
            heapsize--;
            Maxify(0);
        }
    }

    public void Maxify(int i) {
        int l = Left(i);//获得左子树索引
        int r = Right(i);//获得右子树索引
        int largest;

        if (l < heapsize && heap[l] > heap[i])
            largest = l;
        else
            largest = i;
        if (r < heapsize && heap[r] > heap[largest])
            largest = r;
        if (largest == i || largest >= heapsize)//如果largest等于i说明i是最大元素 largest超出heap范围说明不存在比i节点大的子女
            return;
        int tmp = heap[i];//交换i与largest对应的元素位置，在largest位置递归调用maxify
        heap[i] = heap[largest];
        heap[largest] = tmp;
        Maxify(largest);
    }

    public void IncreaseValue(int i, int val) {
        heap[i] = val;
        if (i >= heapsize || i <= 0 || heap[i] >= val)
            return;
        int p = Parent(i);
        if (heap[p] >= val)
            return;
        heap[i] = heap[p];
        IncreaseValue(p, val);
    }

    private int Parent(int i) {
        return (i - 1) / 2;
    }

    private int Left(int i) {
        return 2 * (i + 1) - 1;
    }

    private int Right(int i) {
        return 2 * (i + 1);
    }


    private static void printHeapTree(int[] array) {
        for (int i = 1; i < array.length; i = i * 2) {
            for (int k = i - 1; k < 2 * (i) - 1 && k < array.length; k++) {
                System.out.print(array[k] + " ");
            }
            System.out.println();
        }
    }

    private static void printHeap(int[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
    }

    /**
     * 冒泡排序
     *
     * @param arr
     */
    public static void BubbleSort(int[] arr) {
        int temp;//定义一个临时变量
        for (int i = 0; i < arr.length - 1; i++) {//冒泡趟数
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j + 1] < arr[j]) {
                    temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }


    /**
     * 快速排序算法分区
     *
     * @param array
     * @param lo
     * @param hi
     * @return
     */
    public static int partition(int[] array, int lo, int hi) {
        //固定的切分方式
        int key = array[lo];
        while (lo < hi) {
            while (array[hi] >= key && hi > lo) {//从后半部分向前扫描
                hi--;
            }
            array[lo] = array[hi];
            while (array[lo] <= key && hi > lo) {//从前半部分向后扫描
                lo++;
            }
            array[hi] = array[lo];
        }
        array[hi] = key;
        return hi;
    }


    /**
     * 递归的快速排序算法
     *
     * @param array
     * @param lo
     * @param hi
     */
    public static void sort(int[] array, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int index = partition(array, lo, hi);
        sort(array, lo, index - 1);
        sort(array, index + 1, hi);
    }


    /**
     * 选择排序算法
     *
     * @param a
     */
    public static void selectionSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n; i++) {
            int k = i;
            // 找出最小值的小标
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[k]) {
                    k = j;
                }
            }
            // 将最小值放到排序序列末尾
            if (k > i) {
                int tmp = a[i];
                a[i] = a[k];
                a[k] = tmp;
            }
        }
    }

    /**
     * 插入排序
     *
     * @param array
     */
    private static void insertSort(int[] array) {
        int n = array.length;
        for (int i = 1; i < n; i++) {
            int key = array[i];
            int j = i - 1;
            while (j >= 0 && array[j] > key) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
    }

    /**
     * 希尔排序
     *
     * @param array
     */
    private static void shellSort(int[] array) {
        int n = array.length;
        int h = 1;
        while (h < n / 3) h = 3 * h + 1;
        while (h >= 1) {
            for (int i = h; i < n; i++) {
                for (int j = i; j >= h && (array[j] < array[j - h]); j -= h) {
                    int temp = array[j];
                    array[j] = array[j - h];
                    array[j - h] = temp;
                }
            }
            h /= 3;
        }
    }


    /**
     * 归并排序
     */
    class MergeSort {

        private void mergeSort(int[] array) {
            int[] aux = new int[array.length];
            sort(array, aux, 0, array.length - 1);
        }

        private void sort(int[] array, int[] aux, int lo, int hi) {
            if (hi <= lo) return;
            int mid = lo + (hi - lo) / 2;
            sort(array, aux, lo, mid);
            sort(array, aux, mid + 1, hi);
            merge(array, aux, lo, mid, hi);
        }

        private void merge(int[] array, int[] aux, int lo, int mid, int hi) {
            System.arraycopy(array, 0, aux, 0, array.length);
            int i = lo, j = mid + 1;
            for (int k = lo; k <= hi; k++) {
                if (i > mid) array[k] = aux[j++];
                else if (j > hi) array[k] = aux[i++];
                else if (aux[j] < aux[i]) array[k] = aux[j++];
                else array[k] = aux[i++];
            }
        }
    }
}