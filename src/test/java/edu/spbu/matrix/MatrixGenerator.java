package edu.spbu.matrix;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class MatrixGenerator {
    //никак иначе нельзя было(
    static double[][] generateArray(int bound){
//        int m = ThreadLocalRandom.current().nextInt(1, bound);
//        int n = ThreadLocalRandom.current().nextInt(1, bound);
        int m = bound, n = bound;


        double[][] arr = new double[m][n];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                arr[i][j] = ThreadLocalRandom.current().nextDouble(-10000, 10000);
            }
        }
        return arr;
    }
    static double[][] generateArray(int lineNumber, int bound){
//        int z = ThreadLocalRandom.current().nextInt(1, bound);

//        double[][] arr = new double[lineNumber][z];
        double[][] arr = new double[bound][bound];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                arr[i][j] = ThreadLocalRandom.current().nextDouble(-10000, 10000);
            }
        }
        return arr;
    }

    //generates 75% of zeros
    static void generateSparse(String fileName, int lineCount, int rawCount, int percentOfZeros){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
            for (int i = 0; i < lineCount; i++) {
                for (int j = 0; j < rawCount; j++) {
                    if (ThreadLocalRandom.current().nextInt(0, 100) > percentOfZeros)
                        writer.write(String.valueOf(ThreadLocalRandom.current().nextDouble(-10000, 10000)));
                    else
                        writer.write("0");
                    writer.write(" ");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
