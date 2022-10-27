package edu.spbu.matrix;


import org.junit.Test;
import org.junit.jupiter.api.RepeatedTest;


import static org.junit.Assert.assertEquals;

public class MatrixFileReaderUnitTest {

    @Test
    public void fileReaderTest(){

        double[][] expected;
        expected = new double[][]{
                {1.0, 2.0, 0.26, 3.0},
                {1.0, 1.0, 1.0, 1.3},
                {11.1, 2.0, 3.523, 1.0},
                {2.0, 3.0, 4.0, 5.0}};
        assertEquals(
                new DenseMatrix(expected),
                new DenseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/commasInsteadOfDots.txt"));
    }

    @Test
    public void differentNumberOfElementsInRows(){

        double[][] expected;
        expected = new double[][]{{}};
        assertEquals(
                new DenseMatrix(expected),
                new DenseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/differentNumberOfElementsInRows.txt"));
    }

    @Test
    public void elementBeginningWithDot(){

        double[][] expected;
        expected = new double[][]{{0.33}};
        assertEquals(
                new DenseMatrix(expected),
                new DenseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/elementBeginningWithDot"));
    }

    @Test
    public void emptyFile(){

        double[][] expected;
        expected = new double[][]{{}};
        assertEquals(
                new DenseMatrix(expected),
                new DenseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/emptyFile.txt"));
    }

    @Test
    public void simpleMatrix(){

        double[][] expected;
        expected = new double[][]{
                {1.1, 0.2, 3.0},
                {44.0, 5.0, 6.0},
                {8.0, 0.1, 3.0},
                {3.15, 3.0, 6.0}};
        assertEquals(
                new DenseMatrix(expected),
                new DenseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/simpleMatrix.txt"));
    }

    @Test
    public void unacceptableSymbol(){

        double[][] expected;
        expected = new double[][]{{}};
        assertEquals(
                new DenseMatrix(expected),
                new DenseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/unacceptableSymbol.txt"));
    }

    @Test
    public void whitespacesAndEmptyLines(){

        double[][] expected;
        expected = new double[][]{
                {1.0, 2.0, 0.0, 3.0},
                {1.0, 1.0, 1.0, 1.3},
                {11.1, 2.0, 3.0, 1.0},
                {2.0, 3.0, 4.0, 5.0}};
        assertEquals(
                new DenseMatrix(expected),
                new DenseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/whitespacesAndEmptyLines.txt"));
    }


    //sparse tests
    @Test
    public void sparseDefaultTest(){

        double[][] expected = new double[][]{
                {0, 0, 0, 0, 0},
                {1, 3, 0, 0 , 1},
                {0, 0, 3, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 4, 0, 4, 0}};
        assertEquals(
                new SparseMatrix(expected),
                new SparseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/sparseDefaultMatrix"));
    }

    @Test
    public void sparceAndDenseEquals(){

        double[][] expected = new double[][]{
                {0, 0, 0, 0, 0},
                {1, 3, 0, 0 , 1},
                {0, 0, 3, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 4, 0, 4, 0}};
        assertEquals(
                new DenseMatrix(expected),
                new SparseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/sparseDefaultMatrix"));
    }


    @RepeatedTest(10)
    public void sparseRead(){
        MatrixGenerator.generateSparse("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/generatedSparse1.txt", 1000, 500, 90);
        assertEquals(
                new DenseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/generatedSparse1.txt"),
                new SparseMatrix("src/test/java/edu/spbu/matrix/matrixFileReaderTestSet/generatedSparse1.txt"));
    }


}
