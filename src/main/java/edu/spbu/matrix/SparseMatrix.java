package edu.spbu.matrix;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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
      int lineHash = 1;
      for (int i = 0; i < lst.size(); i++) {
        if (lst.get(i) != 0){
          this.put(0, i, lst.get(i));
        }
        long bits = Double.doubleToLongBits(lst.get(i));
        lineHash = 31 * lineHash + (int)(bits ^ (bits >>> 32));
      }
      hash = 31 + lineHash;
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
        lineHash = 1;
        for (int j = 0; j < lst.size(); j++) {
          if (lst.get(j) != 0){
            this.put(i, j, lst.get(j));
          }
          long bits = Double.doubleToLongBits(lst.get(j));
          lineHash = 31 * lineHash + (int)(bits ^ (bits >>> 32));
        }
        hash = hash*31 + lineHash;
      }
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
    hash = 1;
    for (int i = 0; i < arr.length; i++) {
      int lineHash = 1;
      for (int j = 0; j < arr[0].length; j++) {
        if (arr[i][j] != 0)
          this.put(i, j, arr[i][j]);
        long bits = Double.doubleToLongBits(arr[i][j]);
        lineHash = 31 * lineHash + (int)(bits ^ (bits >>> 32));
      }
      hash = hash*31 + lineHash;
    }
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
      int resHash = 1;
      for (int i = 0; i < m1.getLineCount(); i++) {//m
        int tempHash = 1;
        if (!(m1.getMatrixHashMap().containsKey(i))){
          tempHash = (int) pow(31, m2.getLineCount());
          resHash = resHash*31 + tempHash;
          continue;
        }
        for (int j = 0; j < m2.getLineCount(); j++) {//z
          if (!(m2.getMatrixHashMap().containsKey(j))){
            long bits = Double.doubleToLongBits(res.getElement(i, j));
            tempHash = tempHash*31 + (int)(bits ^ (bits >>> 32));
            continue;
          }
          for (int k = 0; k < m1.getColumnCount(); k++) { //n
            res.put(i, j, res.getElement(i, j)+m1.getElement(i, k)*m2.getElement(j, k));
          }
          long bits = Double.doubleToLongBits(res.getElement(i, j));
          tempHash = tempHash*31 + (int)(bits ^ (bits >>> 32));
        }
        resHash = resHash*31 + tempHash;
      }
      res.hash = resHash;
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
      DenseMatrix res = new DenseMatrix(this.getLineCount(), ((DenseMatrix) o).getColumnCount());
      double[][] resArr = res.getMatrixArr();
      int resHash = 1;
      for (int i = 0; i < m1.getLineCount(); i++) {//m
        int tempHash = 1;
        if (!(m1.getMatrixHashMap().containsKey(i))){
          for (int j = 0; j < m2.getLineCount(); j++){
            resArr[i][j] = 0;
            tempHash = tempHash*31;
          }
          resHash = resHash*31 + tempHash;
          continue;
        }
        for (int j = 0; j < m2.getLineCount(); j++) {//z
          for (int k = 0; k < m1.getColumnCount(); k++) { //n
            resArr[i][j] += m1.getElement(i, k)*m2Arr[j][k];
          }
          long bits = Double.doubleToLongBits(resArr[i][j]);
          tempHash = tempHash*31 + (int)(bits ^ (bits >>> 32));
        }
        resHash = resHash*31 + tempHash;
      }
      res.setHash(resHash);
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
}
