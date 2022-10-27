package edu.spbu;

import edu.spbu.matrix.DenseMatrix;
import edu.spbu.matrix.Matrix;
import edu.spbu.matrix.SparseMatrix;

public class MatrixPerfTest
{
  public static final String MATRIX1_NAME = "src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/sparseDefaultMatrix";
  public static final String MATRIX2_NAME = "src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/sparseDefaultMatrix";

  public static void main(String[] s) {

    System.out.println("Starting loading dense matrices");
    Matrix m1 = new DenseMatrix(MATRIX1_NAME);
    Matrix m2 = new DenseMatrix(MATRIX2_NAME);
    long start;
    Matrix r1 = m1.mul(m2);
    ((DenseMatrix)r1).print();
    System.out.println("Starting loading sparse matrices");
    m1 = new SparseMatrix(MATRIX1_NAME);

    System.out.println("1 loaded");
    m2 = new SparseMatrix(MATRIX2_NAME);
    System.out.println("2 loaded");
    start = System.currentTimeMillis();
    Matrix r2 = m1.mul(m2);
    ((SparseMatrix) r2).printLikeArr();
    System.out.println("Sparse Matrix time: " +(System.currentTimeMillis() - start));
    System.out.println("equals: " + r1.equals(r2));
  }
}
