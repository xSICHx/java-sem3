package edu.spbu.matrix;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

/**
 * Разряженная матрица
 */
public class SparseMatrix implements Matrix
{
  private Map<Integer, Map<Integer, Double>> matrixHashMap;
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
  public SparseMatrix(Map<Integer, Map<Integer, Double>> map){
    matrixHashMap = new HashMap<>();
    if (map.isEmpty())
      return;

    for (Integer i: map.keySet()) {
      Map<Integer, Double> temp = map.get(i);
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
  public SparseMatrix(int m, int n, int flagDmul){
    if (flagDmul == 0)
      matrixHashMap = new HashMap<>();
    else
      matrixHashMap = new ConcurrentHashMap<>();
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
  public Map<Integer, Map<Integer, Double>> getMatrixHashMap(){
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
      SparseMatrix res = new SparseMatrix(this.getLineCount(), ((SparseMatrix) o).getColumnCount(), 0);

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

      for (Map.Entry<Integer, Map<Integer, Double>> lineM1: m1.getMatrixHashMap().entrySet()) {//m
        for (Map.Entry<Integer, Map<Integer, Double>> lineM2: m2.getMatrixHashMap().entrySet()) {//z
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
      DenseMatrix res = new DenseMatrix(this.getLineCount(), ((DenseMatrix) o).getColumnCount());
      double[][] resArr = res.getMatrixArr();

      for (Map.Entry<Integer, Map<Integer, Double>> lineM1: m1.getMatrixHashMap().entrySet()) {//m
        for (int j = 0; j < m2.getLineCount(); j++) {
          for (Map.Entry<Integer, Double> elemM1: lineM1.getValue().entrySet()) {
            int i = lineM1.getKey(), k = elemM1.getKey();
            resArr[i][j] += elemM1.getValue()*m2Arr[j][k];
          }
        }
      }
      res.setHash(res.calculateHashCode(resArr));
      return res;
    }
    return new SparseMatrix(new HashMap<>());
  }


  /**
   * многопоточное умножение матриц
   *
   */

  public static class ThreadCalcChunkSparse extends Thread{
    private final Map.Entry<Integer, Map<Integer, Double>> lineM1;
    private final Map<Integer, Map<Integer, Double>> M2;
    private final SparseMatrix result;
    ThreadCalcChunkSparse(Map.Entry<Integer, Map<Integer, Double>> lineM1, Map<Integer, Map<Integer, Double>> M2, SparseMatrix result){
      this.lineM1 = lineM1;
      this.M2 = M2;
      this.result = result;

    }
    @Override public void run(){
      for (Map.Entry<Integer, Map<Integer, Double>> lineM2: M2.entrySet()) {//z
        for (Map.Entry<Integer, Double> elemM1: lineM1.getValue().entrySet()) {//n
          int i = lineM1.getKey(), j = lineM2.getKey(), k = elemM1.getKey();
          if (lineM2.getValue().containsKey(k))
            result.put(i, j, result.getElement(i, j)+elemM1.getValue()*lineM2.getValue().get(k));
        }
      }
    }
  }

  public static class ThreadCalcChunkDense extends Thread{
    private final Map.Entry<Integer, Map<Integer, Double>> lineM1;
    private final double[][] M2;
    private final double[][] result;
    ThreadCalcChunkDense(Map.Entry<Integer, Map<Integer, Double>> lineM1, double[][] M2, double[][] result){
      this.lineM1 = lineM1;
      this.M2 = M2;
      this.result = result;

    }
    @Override public void run(){
      for (int j = 0; j < M2.length; j++) {
        for (Map.Entry<Integer, Double> elemM1: lineM1.getValue().entrySet()) {
          int i = lineM1.getKey(), k = elemM1.getKey();
          result[i][j] += elemM1.getValue()*M2[j][k];
        }
      }
    }
  }


  @Override public Matrix dmul(Matrix o){
    if (o instanceof SparseMatrix) {
      if (((SparseMatrix) o).getLineCount() == 0 || ((SparseMatrix) o).getColumnCount() == 0 ||
              this.getColumnCount() == 0 || this.getLineCount() == 0)
        return new SparseMatrix(new HashMap<>());
      if (((SparseMatrix) o).getLineCount() != this.getColumnCount())
        return new SparseMatrix(new HashMap<>());

      SparseMatrix m1 = this;
      SparseMatrix m2 = ((SparseMatrix) o).matrixTransposition(((SparseMatrix) o));
      SparseMatrix res = new SparseMatrix(this.getLineCount(), ((SparseMatrix) o).getColumnCount(), 1);
      List<ThreadCalcChunkSparse> calcElemThreads = new ArrayList<>();
      for (Map.Entry<Integer, Map<Integer, Double>> lineM1: m1.getMatrixHashMap().entrySet()) {//m
        calcElemThreads.add(new ThreadCalcChunkSparse(lineM1, m2.getMatrixHashMap(), res));
        calcElemThreads.get(calcElemThreads.size()-1).start();
      }
      try{
        for (ThreadCalcChunkSparse thread: calcElemThreads) {
          thread.join();
        }
      }
      catch (Exception e){
        e.printStackTrace();
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

      SparseMatrix m1 = this;
      DenseMatrix m2 = ((DenseMatrix) o).matrixTransposition(((DenseMatrix) o).getMatrixArr());
      DenseMatrix res = new DenseMatrix(this.getLineCount(), ((DenseMatrix) o).getColumnCount());
      double[][] resArr = res.getMatrixArr();

      List<ThreadCalcChunkDense> calcElemThreads = new ArrayList<>();
      for (Map.Entry<Integer, Map<Integer, Double>> lineM1: m1.getMatrixHashMap().entrySet()) {//m
        calcElemThreads.add(new ThreadCalcChunkDense(lineM1, m2.getMatrixArr(), resArr));
        calcElemThreads.get(calcElemThreads.size()-1).start();
      }
      try{
        for (ThreadCalcChunkDense thread: calcElemThreads) {
          thread.join();
        }
      }
      catch (Exception e){
        e.printStackTrace();
      }
      res.setHash(res.calculateHashCode(resArr));
      return res;
    }
    return new SparseMatrix(new HashMap<>());
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
    Map<Integer, Double> tmp = matrixHashMap.get(i);
    if (tmp != null)
      tmp.put(j, value);
    else{
      HashMap<Integer, Double> temp = new HashMap<>();
      temp.put(j, value);
      matrixHashMap.put(i, temp);
    }
  }
  public Double getElement(int i, int j){
    if (matrixHashMap.containsKey(i)){
      Map<Integer, Double> raw = matrixHashMap.get(i);
      if (!raw.containsKey(j))
        return 0.0;
      return raw.get(j);
    }
    return 0.0;
  }

  public SparseMatrix matrixTransposition(SparseMatrix src){
    SparseMatrix res = new SparseMatrix(src.getColumnCount(), src.getLineCount(), 0);
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
  
  private int calculateHashCode(Map<Integer, Map<Integer, Double>> map){
    if (map.isEmpty()){
      return 0;
    }
    int result = 1;
    for (Map<Integer, Double> raw: map.values()) {
      for (Double elem: raw.values()) {
        result *= (int) Math.floor(elem * rounding);
      }
    }
    return result;
  }
  
}
