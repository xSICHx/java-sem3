package edu.spbu.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;


public class MatrixMulUnitTest {

    @RepeatedTest(25)
    public void repeatedDenseMulTest(){
        double[][] matrixData = MatrixGenerator.generateArray(2000);
        RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
        double[][] matrixData2 = MatrixGenerator.generateArray(matrixData[0].length, 2000);
        RealMatrix n = new Array2DRowRealMatrix(matrixData2);
        double[][] expected = m.multiply(n).getData();
        DenseMatrix m1 = new DenseMatrix(matrixData);
        DenseMatrix m2 = new DenseMatrix(matrixData2);
        long start = System.currentTimeMillis();
        DenseMatrix actual = ((DenseMatrix) m1.dmul(m2));
        System.out.println("time: " +(System.currentTimeMillis() - start));
        Assertions.assertEquals(new DenseMatrix(expected), actual);
    }

    @Test
    public void incompatibleDenseMatrix(){
        DenseMatrix m1 = new DenseMatrix(MatrixGenerator.generateArray(1000));
        DenseMatrix m2 = new DenseMatrix(MatrixGenerator.generateArray(m1.getColumnCount()+1, 1000));
        DenseMatrix expected = new DenseMatrix(new double[0][0]);
        DenseMatrix actual = (DenseMatrix) m1.mul(m2);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void zeroDenseMatrixMul(){
        DenseMatrix m1 = new DenseMatrix(new double[0][0]);
        DenseMatrix m2 = new DenseMatrix(new double[0][0]);
        DenseMatrix expected = new DenseMatrix(new double[0][0]);
        DenseMatrix actual = (DenseMatrix) m1.mul(m2);
        Assertions.assertEquals(expected, actual);
    }


    //sparse tests
    @RepeatedTest(5)
    public void repeatedSparseMulTest(){
        double[][] matrixData = MatrixGenerator.generateArray(500);
        double[][] matrixData2 = MatrixGenerator.generateArray(matrixData[0].length, 500);
        DenseMatrix m1 = new DenseMatrix(matrixData);
        DenseMatrix m2 = new DenseMatrix(matrixData2);
        DenseMatrix expected = ((DenseMatrix) m1.mul(m2));
        SparseMatrix sparseMatrix1 = new SparseMatrix(matrixData);
        SparseMatrix sparseMatrix2 = new SparseMatrix(matrixData2);
        SparseMatrix actual = ((SparseMatrix) sparseMatrix1.mul(sparseMatrix2));
        Assertions.assertEquals(expected, actual);
    }

    @RepeatedTest(5)
    public void repeatedSparseMulOnDenseTest(){
        double[][] matrixData = MatrixGenerator.generateArray(2000);
        double[][] matrixData2 = MatrixGenerator.generateArray(matrixData[0].length, 2000);
        DenseMatrix m1 = new DenseMatrix(matrixData);
        DenseMatrix m2 = new DenseMatrix(matrixData2);
        DenseMatrix expected = ((DenseMatrix) m1.dmul(m2));
        SparseMatrix sparseMatrix1 = new SparseMatrix(matrixData);
        Matrix actual =  sparseMatrix1.dmul(m2);
        Assertions.assertEquals(expected, actual);
    }

    @RepeatedTest(5)
    public void repeatedDenseMulOnSparseTest(){
        double[][] matrixData = MatrixGenerator.generateArray(1500);
        double[][] matrixData2 = MatrixGenerator.generateArray(matrixData[0].length, 1500);
        DenseMatrix m1 = new DenseMatrix(matrixData);
        DenseMatrix m2 = new DenseMatrix(matrixData2);
        DenseMatrix expected = ((DenseMatrix) m1.dmul(m2));
        SparseMatrix sparseMatrix2 = new SparseMatrix(matrixData2);
        Matrix actual = m1.dmul(sparseMatrix2);
        Assertions.assertEquals(expected, actual);
    }

    @RepeatedTest(5)
    public void repeatedSparseConcretePercentOfZerosTest(){
        MatrixGenerator.generateSparse("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/generatedSparse1.txt", 1000, 1000, 90);
        MatrixGenerator.generateSparse("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/generatedSparse2.txt", 1000, 1000, 90);
        DenseMatrix m1 = new DenseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/generatedSparse1.txt");
        DenseMatrix m2 = new DenseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/generatedSparse2.txt");
        long start = System.currentTimeMillis();
        Matrix expected = m1.dmul(m2);
        System.out.println("dense time: " +(System.currentTimeMillis() - start));
        SparseMatrix sparseMatrix1 = new SparseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/generatedSparse1.txt");
        SparseMatrix sparseMatrix2 = new SparseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/generatedSparse2.txt");
        start = System.currentTimeMillis();
        Matrix actual = sparseMatrix1.dmul(sparseMatrix2);
        System.out.println("sparse time: " +(System.currentTimeMillis() - start));
        Assertions.assertEquals(expected, actual);
    }
}
