package edu.spbu.matrix;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

/**
 * Разряженная матрица
 */
public class SparseMatrix implements Matrix
{
  private HashMap<Integer, HashMap<Integer, Double>> matrixHashMap;
  private final int[] matrixSize = {0, 0};
  private static final int rounding = 10000;
  private int hash = 0;
  public SparseMatrix(String fileName) {
    try (FileInputStream fIn = new FileInputStream(fileName)){
      BufferedReader reader = new BufferedReader(new InputStreamReader(fIn));
      String line;
      //finding number of lines
      while ((line = reader.readLine()) != null){
        if (!line.isEmpty())
          matrixSize[0]++;
      }
      if (matrixSize[0] == 0){
        throw new IOException("empty file.");
      }
      //finding number of columns and reset file
      fIn.getChannel().position(0);
      reader = new BufferedReader(new InputStreamReader(fIn));

      //avoiding a lot of white spaces and empty lines
      do {
        line = reader.readLine();
      } while (line.isEmpty());
      line = line.replaceAll("\\s+", " ");
      line = line.replaceAll(",", ".");
      List<Double> lst = Arrays.stream((line.split(" "))).mapToDouble(Double::parseDouble).boxed().collect(Collectors.toList());
      matrixSize[1] = lst.size();
      if (matrixSize[1] == 0){
        throw new IOException("zero elements in line.");
      }
      matrixHashMap = new HashMap<>();

      //reading first line matrix
      for (int i = 0; i < lst.size(); i++) {
        if (lst.get(i) != 0){
          this.put(0, i, lst.get(i));
        }
      }
      //reading last part of matrix
      for (int i = 1; i < getLineCount(); i++){
        do {
          line = reader.readLine();
        } while (line.isEmpty());
        line = line.replaceAll("\\s+", " ");
        line = line.replaceAll(",", ".");
        lst = Arrays.stream((line.split(" "))).mapToDouble(Double::parseDouble).boxed().collect(Collectors.toList());
        if (lst.size() != getColumnCount()){
          throw new IOException("different count of elements in lines.");
        }
        for (int j = 0; j < lst.size(); j++) {
          if (lst.get(j) != 0){
            this.put(i, j, lst.get(j));
          }
        }
      }
      hash = calculateHashCode(this.matrixHashMap);
    }
    catch (Exception e){
      hash = 0;
      matrixHashMap = new HashMap<>();
      matrixSize[0] = 0;
      matrixSize[1] = 0;
      System.out.println("Wrong input format: " + e.getMessage() + " This matrix was created empty");
    }
  }
  public SparseMatrix(HashMap<Integer, HashMap<Integer, Double>> map){
    matrixHashMap = new HashMap<>();
    if (map.isEmpty())
      return;

    for (Integer i: map.keySet()) {
      HashMap<Integer, Double> temp = map.get(i);
      for (Integer j: temp.keySet()) {
        this.put(i, j, temp.get(j));
      }
    }
    this.hash = calculateHashCode(map);
  }
  public SparseMatrix(double[][] arr){
    matrixHashMap = new HashMap<>();
    if (Arrays.deepEquals(arr, new double[][]{{}})){
      return;
    }
    if (Arrays.deepEquals(arr, new double[0][0])){
      return;
    }
    matrixSize[0] = arr.length;
    matrixSize[1] = arr[0].length;
    for (int i = 0; i < arr.length; i++) {
      for (int j = 0; j < arr[0].length; j++) {
        if (arr[i][j] != 0)
          this.put(i, j, arr[i][j]);
      }
    }
    hash = calculateHashCode(this.matrixHashMap);
  }
  public SparseMatrix(int m, int n){
    matrixHashMap = new HashMap<>();
    if (m <= 0 || n <= 0)
      return;
    matrixSize[0] = m;
    matrixSize[1] = n;
  }

  public int getLineCount(){
    return matrixSize[0];
  }
  public int getColumnCount(){
    return  matrixSize[1];
  }
  public HashMap<Integer, HashMap<Integer, Double>> getMatrixHashMap(){
    return matrixHashMap;
  }
  /**
   * однопоточное умнджение матриц
   * должно поддерживаться для всех 4-х вариантов
   *
   */
  @Override
  public Matrix mul(Matrix o)
  {
    if (o instanceof SparseMatrix) {
      if (((SparseMatrix) o).getLineCount() == 0 || ((SparseMatrix) o).getColumnCount() == 0 ||
              this.getColumnCount() == 0 || this.getLineCount() == 0)
        return new SparseMatrix(new HashMap<>());
      if (((SparseMatrix) o).getLineCount() != this.getColumnCount())
        return new SparseMatrix(new HashMap<>());

      SparseMatrix m1 = this;
      SparseMatrix m2 = ((SparseMatrix) o).matrixTransposition(((SparseMatrix) o));
      SparseMatrix res = new SparseMatrix(this.getLineCount(), ((SparseMatrix) o).getColumnCount());
//      for (int i = 0; i < m1.getLineCount(); i++) {//m
//        if (!(m1.getMatrixHashMap().containsKey(i))){
//          continue;
//        }
//        for (int j = 0; j < m2.getLineCount(); j++) {//z
//          if (!(m2.getMatrixHashMap().containsKey(j))){
//            continue;
//          }
//          for (int k = 0; k < m1.getColumnCount(); k++) { //n
//            res.put(i, j, res.getElement(i, j)+m1.getElement(i, k)*m2.getElement(j, k));
//          }
//        }
//      }
      for (Map.Entry<Integer, HashMap<Integer, Double>> lineM1: m1.getMatrixHashMap().entrySet()) {//m
        for (Map.Entry<Integer, HashMap<Integer, Double>> lineM2: m2.getMatrixHashMap().entrySet()) {//z
          for (Map.Entry<Integer, Double> elemM1: lineM1.getValue().entrySet()) {//n
            int i = lineM1.getKey(), j = lineM2.getKey(), k = elemM1.getKey();
            if (lineM2.getValue().containsKey(k))
              res.put(i, j, res.getElement(i, j)+elemM1.getValue()*lineM2.getValue().get(k));
          }
        }
      }
      res.hash = calculateHashCode(res.matrixHashMap);
      return res;
    }


    if (o instanceof DenseMatrix){
      if (((DenseMatrix) o).getLineCount() == 0 || ((DenseMatrix) o).getColumnCount() == 0 ||
              this.getColumnCount() == 0 || this.getLineCount() == 0)
        return new SparseMatrix(new HashMap<>());
      if (((DenseMatrix) o).getLineCount() != this.getColumnCount())
        return new SparseMatrix(new HashMap<>());
      //todo
      SparseMatrix m1 = this;
      DenseMatrix m2 = ((DenseMatrix) o).matrixTransposition(((DenseMatrix) o).getMatrixArr());
      double[][] m2Arr = m2.getMatrixArr();
//      DenseMatrix res = new DenseMatrix(this.getLineCount(), ((DenseMatrix) o).getColumnCount());
//      double[][] resArr = res.getMatrixArr();
//      for (int i = 0; i < m1.getLineCount(); i++) {//m
//        if (!(m1.getMatrixHashMap().containsKey(i))){
//          for (int j = 0; j < m2.getLineCount(); j++){
//            resArr[i][j] = 0;
//          }
//          continue;
//        }
//        for (int j = 0; j < m2.getLineCount(); j++) {//z
//          for (int k = 0; k < m1.getColumnCount(); k++) { //n
//            resArr[i][j] += m1.getElement(i, k)*m2Arr[j][k];
//          }
//        }
//      }
      SparseMatrix res = new SparseMatrix(this.getLineCount(), ((DenseMatrix) o).getColumnCount());
      HashMap<Integer, HashMap<Integer, Double>> resHashMap = res.matrixHashMap;

      for (Map.Entry<Integer, HashMap<Integer, Double>> lineM1: m1.getMatrixHashMap().entrySet()) {//m
        for (int j = 0; j < m2.getLineCount(); j++) {
          for (Map.Entry<Integer, Double> elemM1: lineM1.getValue().entrySet()) {
            int i = lineM1.getKey(), k = elemM1.getKey();
            res.put(i, j, res.getElement(i, j)+elemM1.getValue()*m2Arr[j][k]);
          }
        }
      }
      res.hash = res.calculateHashCode(resHashMap);
      return res;
    }
    return new SparseMatrix(new HashMap<>());
  }


  /**
   * многопоточное умножение матриц
   *
   */
  @Override public Matrix dmul(Matrix o)
  {
    return null;
  }

  /**
   * спавнивает с обоими вариантами
   */
  @Override
  public boolean equals(Object o) {

    if (o instanceof SparseMatrix){
      if (((SparseMatrix) o).getLineCount() != this.getLineCount() || ((SparseMatrix) o).getColumnCount() != this.getColumnCount())
        return false;
      if (!matrixHashMap.keySet().equals(((SparseMatrix) o).getMatrixHashMap().keySet()))
        return false;
      Set<Integer> columnKeySet = matrixHashMap.keySet();
      for (Integer i: columnKeySet) {
        if (!matrixHashMap.get(i).keySet().equals(((SparseMatrix) o).getMatrixHashMap().get(i).keySet()))
          return false;
        Set<Integer> rawKeySet = matrixHashMap.get(i).keySet();
        for (Integer j: rawKeySet) {
          if (abs(this.getElement(i, j) - ((SparseMatrix) o).getElement(i, j)) > 0.0001)
            return false;
        }
      }
      return true;
    }

    else if (o instanceof DenseMatrix) {
      if (((DenseMatrix) o).getLineCount() != this.getLineCount() || ((DenseMatrix) o).getColumnCount() != this.getColumnCount())
        return false;
      double[][] arr = ((DenseMatrix) o).getMatrixArr();
      for (int i = 0; i < arr.length; i++) {
        for (int j = 0; j < arr[0].length; j++) {
          if (abs(arr[i][j] - this.getElement(i, j)) > 0.0001){
            //todo
            ((DenseMatrix) o).print();
            this.printLikeArr();
            System.out.print(arr[i][j] + " " + this.getElement(i, j));
            return false;}
        }
      }
      return true;
    }
    return false;
  }

  private void put(int i, int j, double value){
    if (value == 0)
      return;
    if (matrixHashMap.containsKey(i))
      matrixHashMap.get(i).put(j, value);
    else{
      HashMap<Integer, Double> temp = new HashMap<>();
      temp.put(j, value);
      matrixHashMap.put(i, temp);
    }
  }
  public double getElement(int i, int j){
    if (matrixHashMap.containsKey(i)){
      HashMap<Integer, Double> raw = matrixHashMap.get(i);
      if (!raw.containsKey(j))
        return 0;
      return raw.get(j);
    }
    return 0;
  }
  public SparseMatrix matrixTransposition(SparseMatrix src){
    SparseMatrix res = new SparseMatrix(src.getColumnCount(), src.getLineCount());
    for (int i = 0; i < src.getColumnCount(); i++) {
      for (int j = 0; j < src.getLineCount(); j++) {
        res.put(i, j, src.getElement(j,i));
      }
    }
    return res;
  }

  public void printLikeArr(){
    System.out.println(Arrays.toString(this.matrixSize));
    for (int i = 0; i < this.getLineCount(); i++) {
      for (int j = 0; j < this.getColumnCount(); j++) {
        System.out.print(this.getElement(i, j) + " ");
      }
      System.out.println();
    }
  }
  
  private int calculateHashCode(HashMap<Integer, HashMap<Integer, Double>> map){
    if (map.isEmpty()){
      return 0;
    }
    int result = 1;
    for (HashMap<Integer, Double> raw: map.values()) {
      for (Double elem: raw.values()) {
        result *= (int) Math.floor(elem * rounding);
      }
    }
    return result;
  }
  
}
