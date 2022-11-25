package edu.spbu.matrix;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import edu.spbu.HelpfulMethods;

import static java.lang.Math.abs;

/**
 * Плотная матрица
 */
public class DenseMatrix implements Matrix
{
  private double[][] matrixArr;
  private final int[] matrixSize = {0, 0};
  private static final int rounding = 10000;
  private int hash = 0;
  public DenseMatrix(String fileName){
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
      matrixArr = new double[getLineCount()][getColumnCount()];

      //reading matrix
      HelpfulMethods.listToArr(lst, matrixArr[0]);
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
        HelpfulMethods.listToArr(lst, matrixArr[i]);
      }
      hash = calculateHashCode(matrixArr);
    }
    catch (Exception e){
      hash = 0;
      matrixArr = new double[0][0];
      matrixSize[0] = 0;
      matrixSize[1] = 0;
      System.out.println("Wrong input format: " + e.getMessage() + " This matrix was created empty");
    }
  }
  public DenseMatrix(double[][] arr){
    if (Arrays.deepEquals(arr, new double[][]{{}})){
      matrixArr = arr.clone();
      return;
    }
    if (Arrays.deepEquals(arr, new double[0][0])){
      matrixArr = arr.clone();
      return;
    }
    matrixSize[0] = arr.length;
    matrixSize[1] = arr[0].length;
    matrixArr = arr.clone();
    hash = calculateHashCode(arr);
  }

  //emptyArray
  DenseMatrix(int lineCount, int columnCount){
    matrixSize[0] = lineCount;
    matrixSize[1] = columnCount;
    matrixArr = new double[lineCount][columnCount];
  }

  public double[][] getMatrixArr(){
    return matrixArr;
  }
  public int getLineCount(){
    return matrixSize[0];
  }
  public int getColumnCount(){
    return  matrixSize[1];
  }
  public void print(){
    System.out.println(Arrays.toString(matrixSize));
    for (int i = 0; i < getLineCount(); i++) {
      System.out.println(Arrays.toString(matrixArr[i]));
    }
  }
  public void printAsArray(){
    System.out.println(Arrays.deepToString(matrixArr).replaceAll("\\[", "{").replaceAll("]", "}"));
  }

  @Override
  public Matrix mul(Matrix o) {
    //TODO sparse matrix
    if (o instanceof DenseMatrix) {
      if (((DenseMatrix) o).getLineCount() == 0 || ((DenseMatrix) o).getColumnCount() == 0 ||
          this.getColumnCount() == 0 || this.getLineCount() == 0)
        return new DenseMatrix(0, 0);
      if (((DenseMatrix) o).getLineCount() != this.getColumnCount())
        return new DenseMatrix(0, 0);
      double[][] m1 = matrixArr;
      double[][] m2 = matrixTransposition(((DenseMatrix) o).matrixArr).matrixArr;
      // m2.length because of transposition
      DenseMatrix res = new DenseMatrix(m1.length, m2.length);
      double[][] mRes = res.getMatrixArr();
      for (int i = 0; i < m1.length; i++) {//m
        for (int j = 0; j < m2.length; j++) {//z
          mRes[i][j] = 0;
          for (int k = 0; k < m1[0].length; k++) {//n
            mRes[i][j] += m1[i][k]*m2[j][k];
          }
        }
      }
      res.hash = calculateHashCode(mRes);
      return res;
    }


    if (o instanceof SparseMatrix){
      SparseMatrix bTrans = ((SparseMatrix) o).matrixTransposition((SparseMatrix) o);
      DenseMatrix aTrans = this.matrixTransposition(this.getMatrixArr());
      return (aTrans).matrixTransposition(((DenseMatrix) bTrans.mul(aTrans)).getMatrixArr());
    }

    return new DenseMatrix(0, 0);
  }
  public DenseMatrix matrixTransposition(double[][] src){
    double[][] res = new double[src[0].length][src.length];
    for (int i = 0; i < src[0].length; i++) {
      for (int j = 0; j < src.length; j++) {
        res[i][j] = src[j][i];
      }
    }
    return new DenseMatrix(res);
  }

  /**
   * многопоточное умножение матриц
   *
   */
  private void calcElemDenseToDense(int i, int j, double[][] M1, double[][] M2, double[][] result){

    result[i][j] = 0;
    for (int k = 0; k < M1[0].length; k++) {//n
      result[i][j] += M1[i][k]*M2[j][k];
    }
  }
  public static class ThreadCalcElem extends Thread{
    private final double[][] M1;
    private final double[][] M2;
    private final double[][] result;
    private final int chunk;
    private final int numberOfElementsInThread;
    ThreadCalcElem(int i, int numberOfElementsInThread, double[][] M1, double[][] M2, double[][] result){
      this.M1 = M1;
      this.M2 = M2;
      this.result = result;
      this.chunk = i;
      this.numberOfElementsInThread = numberOfElementsInThread;
    }
    @Override public void run(){
//      for (int j = 0; j < M2.length; j++) {//z
//        result[chunk][j] = 0;
//        for (int k = 0; k < M1[0].length; k++) {//n
//          result[chunk][j] += M1[chunk][k] * M2[j][k];
//        }
//      }


      int lowerBound = chunk*numberOfElementsInThread;
      int upperBound = Math.min((chunk + 1) * numberOfElementsInThread, result.length * result[0].length);
      int i = lowerBound/result[0].length, j = lowerBound % result[0].length;
      for (int l = lowerBound; l < upperBound; l++) {
        for (int k = 0; k < M1[0].length; k++) {//n
          result[i][j] += M1[i][k] * M2[j][k];
        }
        j++;
        if (j == result[0].length){
          i++;
          j = 0;
        }
      }
    }
  }

  @Override public Matrix dmul(Matrix o)
  {
    if (o instanceof DenseMatrix) {
      if (((DenseMatrix) o).getLineCount() == 0 || ((DenseMatrix) o).getColumnCount() == 0 ||
              this.getColumnCount() == 0 || this.getLineCount() == 0)
        return new DenseMatrix(0, 0);
      if (((DenseMatrix) o).getLineCount() != this.getColumnCount())
        return new DenseMatrix(0, 0);
      double[][] M1 = this.getMatrixArr();
      // m2.length because of transposition
      double[][] M2 = matrixTransposition(((DenseMatrix) o).matrixArr).matrixArr;
      DenseMatrix res = new DenseMatrix(M1.length, M2.length);
      double[][] mRes = res.getMatrixArr();
      int numberOfThreads = Runtime.getRuntime().availableProcessors();
      int numberOfElementsInThread = res.getLineCount()*res.getColumnCount()/numberOfThreads;
      numberOfThreads++;

//      ThreadCalcElem calcThread = new ThreadCalcElem(0, numberOfElementsInThread, M1, M2, mRes);
      ThreadCalcElem[] calcElemThreads = new ThreadCalcElem[numberOfThreads];
      for (int i = 0; i < numberOfThreads; i++) {
        calcElemThreads[i] = new ThreadCalcElem(i, numberOfElementsInThread, M1, M2, mRes);
        calcElemThreads[i].start();
      }

//      ThreadCalcElem[] calcElemThreads = new ThreadCalcElem[M1.length];
//      for (int i = 0; i < M1.length; i++) {//m
//        calcElemThreads[i] = new ThreadCalcElem(i,numberOfElementsInThread, M1, M2, mRes);
//        calcElemThreads[i].start();
//
//      }
      try{
        for (ThreadCalcElem thread: calcElemThreads)
          thread.join();
//        calcThread.join();
      }
      catch (Exception e){
        e.printStackTrace();
      }
      res.setHash(calculateHashCode(res.getMatrixArr()));
      return res;
    }


    if (o instanceof SparseMatrix){
      SparseMatrix bTrans = ((SparseMatrix) o).matrixTransposition((SparseMatrix) o);
      DenseMatrix aTrans = this.matrixTransposition(this.getMatrixArr());
      return (aTrans).matrixTransposition(((DenseMatrix) bTrans.dmul(aTrans)).getMatrixArr());
    }

    return new DenseMatrix(0, 0);
  }

  /**
   * спавнивает с обоими вариантами
   */
  @Override public boolean equals(Object o) {
    if ((o instanceof DenseMatrix)){
    if (this.getColumnCount() != ((DenseMatrix) o).getColumnCount() || this.getLineCount() != ((DenseMatrix) o).getLineCount())
      return false;
    if(this.hash != ((DenseMatrix) o).hash)
      return false;
    double[][] m1 = this.getMatrixArr();
    double[][] m2 = ((DenseMatrix)o).getMatrixArr();
    for (int i = 0; i < this.getLineCount(); i++){
      for (int j = 0; j < this.getColumnCount(); j++){
        if (abs(m2[i][j] - m1[i][j]) > 1.0/(double) rounding) {
          return false;
        }
      }
    }
    return true;
    }
    else if ((o instanceof SparseMatrix)) {
      return o.equals(this);
    }
    return false;
  }

  @Override public int hashCode(){
    return hash;
  }
  public int calculateHashCode(double[][] arr){
    int resultHash = 0;
    int flagNotaZero = 0;
    for (double[] doubles : arr) {
      for (int j = 0; j < arr[0].length; j++) {
        if (doubles[j] != 0) {
          if (flagNotaZero == 0) {
            resultHash = 1;
            flagNotaZero = 1;
          }
          resultHash *=(int) Math.floor(doubles[j] * rounding);
        }
      }
    }
    return resultHash;
  }

  void setHash(int x){
    hash = x;
  }

}
