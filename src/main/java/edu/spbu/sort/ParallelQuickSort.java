package edu.spbu.sort;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelQuickSort {
    public void sort(int[] array) {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(new SortAction(array, 0, array.length-1));
    }

    private static class SortAction extends RecursiveAction {
        int[] array;
        int start;
        int end;

        SortAction(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            if (start >= end) return;
            if (end - start <= 16) {
                IntSort.intersectionSort(array, start, end);
                return;
            }
            int left = start, right = end;
            int supportElem = array[IntSort.medianThree(array, start, end)];

            while (left <= right) {
                while (array[left] < supportElem) left++;
                while (array[right] > supportElem) right--;
                if (left <= right) {
                    if (array[left] > array[right]) {
                        int temp = array[left];
                        array[left] = array[right];
                        array[right] = temp;
                    }
                    left++;
                    right--;
                }
            }

            SortAction leftArr = new SortAction(array, start, right);
            SortAction rightArr = new SortAction(array, left, end);

            leftArr.fork();
            rightArr.fork();
            rightArr.join();

            leftArr.join();
        }
    }
}
