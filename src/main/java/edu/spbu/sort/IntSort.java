package edu.spbu.sort;

import java.util.Collections;
import java.util.List;

public class IntSort {
  public static int medianThree(int[] a, int l, int r){
    if ( (a[l]>a[r]) ^ (a[l] > a[(r+l)/2]) ) return l;
    else if ( (a[r] < a[l]) ^ (a[r] < a[(l+r)/2])) return r;
    else return l + (r-l)/2;
  }
  public static void intersectionSort(int[] array, int start, int end){
    for (int i = start; i <= end; i++){
      int j = i;
      while ((j > start) && (array[j-1]>array[j])){
        int temp = array[j-1];
        array[j-1] = array[j];
        array[j] = temp;
        j--;
      }
    }
  }
  // КАК СДЕЛАТЬ SWAP???
  private static void quickSort (int[] array, int start, int end){
    if (end - start <= 16){
      intersectionSort(array, start, end);
      return;
    }
    int left = start, right = end;
    int supportElem = array[medianThree(array, start, end)] ;

    while (left <= right){
      while (array[left] < supportElem) left++;
      while (array[right] > supportElem) right--;
      if (left <= right){
        if (array[left] > array[right]) {
          int temp = array[left];
          array[left] = array[right];
          array[right] = temp;
        }
        left++;
        right--;
      }
    }
    if (start < right) quickSort(array, start, right);
    if (left < end) quickSort(array, left, end);
  }

  public static void sort (int[] array) {
    ParallelQuickSort a = new ParallelQuickSort();
    a.sort(array);

    //Arrays.sort(array);

//    quickSort(array, 0, array.length-1);
  }

  public static void sort (List<Integer> list) {
    Collections.sort(list);
  }
}

