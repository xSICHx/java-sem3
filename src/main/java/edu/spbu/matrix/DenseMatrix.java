package edu.spbu.matrix;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Плотная матрица
 */
public class DenseMatrix implements Matrix
{
  /**
   * загружает матрицу из файла
   * @param fileName
   */
//  как сделать адекватно private
  private List<List<Integer>> matrixList = new ArrayList<>();
  public DenseMatrix(String fileName) throws IOException {
    BufferedReader buffReader = new BufferedReader(new FileReader(fileName));
    String line = buffReader.readLine();
    while (line != null){
      this.matrixList.add(Arrays.stream((line.split(" "))).mapToInt(Integer::parseInt).boxed().collect(Collectors.toList()));
      line = buffReader.readLine();
    }
  }
  public DenseMatrix(List<List<Integer>> list){
    this.matrixList = list;
  }
  public List<List<Integer>> getMatrixList(){
    return matrixList;
  }
  public void print(){

    for (List<Integer> ints : matrixList) {
      System.out.print(Arrays.toString(new List[]{ints}));
      System.out.println();
    }
  }
  /**
   * однопоточное умнджение матриц
   * должно поддерживаться для всех 4-х вариантов
   *
   * @param o
   * @return
   */
  @Override public Matrix mul(Matrix o)
  {
    if (o instanceof DenseMatrix){


      if (((DenseMatrix) o).getMatrixList().size() != this.matrixList.get(0).size())
        return null;

      List<List<Integer>> m1 = this.matrixList;
      List<List<Integer>> m2 = ((DenseMatrix) o).getMatrixList();
      List<List<Integer>> result = new ArrayList<>();

      for (int i = 0; i < m1.size(); i++){
        List<Integer> raw = new ArrayList<>();
        for (int j = 0; j < m2.get(0).size(); j++){
          int cell = 0;
          for (int k = 0; k < m2.size(); k++){
            cell += m1.get(i).get(k) * m2.get(k).get(j);
          }
          raw.add(cell);
        }
        result.add(raw);
      }
      return new DenseMatrix(result);
    }
    return null;
  }

  /**
   * многопоточное умножение матриц
   *
   * @param o
   * @return
   */
  @Override public Matrix dmul(Matrix o)
  {
   return null;
  }

  /**
   * спавнивает с обоими вариантами
   * @param o
   * @return
   */
  @Override public boolean equals(Object o) {
    if (!(o instanceof DenseMatrix) || (o.hashCode() != this.hashCode()))
      return false;
    List<List<Integer>> m1 = this.getMatrixList();
    List<List<Integer>> m2 = ((DenseMatrix)o).getMatrixList();
    for (int i = 0; i < this.matrixList.size(); i++){
      for (int j = 0; j < this.matrixList.get(0).size(); j++){
        if (!Objects.equals(m2.get(i).get(j), m1.get(i).get(j))) {
          return false;
        }
      }
    }
    return true;
  }
  @Override public int hashCode(){
    int hashCode = 1;
    for (List<Integer> ints: this.matrixList) {
      hashCode = 31*hashCode + (ints==null ? 0 : ints.hashCode());
    }
    return hashCode;
  }


}
