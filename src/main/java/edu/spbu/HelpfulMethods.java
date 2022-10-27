package edu.spbu;

import java.util.List;

public class HelpfulMethods {
    public static void listToArr(List<Double> lst, double[] arr){
        for (int i = 0; i < arr.length; i++){
            arr[i] = lst.get(i);
        }
    }
}
