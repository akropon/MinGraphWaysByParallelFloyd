package com.petrolov;

import java.util.Random;

import static java.lang.String.format;
import static java.lang.System.out;

/**
 * the there is no edge between two nodes, then let this edge weight equals '-1'
 */
public class Main {

    private static final int[] SIZES = {300};  // nodes count
    private static final int[] THREAD_COUNTS = {1, 2, 4, 8, 16, 32};
    private static final int REPEAT_COUNT = 5;

    public static void main(String[] args) {
        for (int i = 0; i < 6; i++) {
            doMeasure();
        }
    }

    private static void doMeasure() {
        long[][] avgResultTimes = new long[SIZES.length][THREAD_COUNTS.length];
        long[] resultTimes = new long[REPEAT_COUNT];
        FloydParallelProcessor floydParallelProcessor = new FloydParallelProcessor();

        for (int iSize = 0; iSize < SIZES.length; iSize++) {
            for (int iThreadCount = 0; iThreadCount < THREAD_COUNTS.length; iThreadCount++) {
                floydParallelProcessor.setNumOfThreads(THREAD_COUNTS[iThreadCount]);
                for (int r = 0; r < REPEAT_COUNT; r++) {
                    Matrix matrix = processInitialization(SIZES[iSize], System.currentTimeMillis());
                    Result result = floydParallelProcessor.process(matrix);
                    resultTimes[r] = result.time;
                }
                avgResultTimes[iSize][iThreadCount] = getAvg(resultTimes);
            }
        }

        floydParallelProcessor.dispose();

        printResults(avgResultTimes);
    }

    private static long getAvg(long[] resultTimes) {
        long sum = 0;
        for (long time : resultTimes) {
            sum += time;
        }
        return sum / resultTimes.length;
    }

    private static void printResults(long[][] resultTimes) {
        int columnWidth = 10;
        String cellFormatInt = "%" + columnWidth + "d";
        String cellFormatStr = "%" + columnWidth + "s";

        out.println("RESULTS");
        out.println("Rows: sizes. Columns: threadCounts.");

        out.print(format(cellFormatStr, " "));
        for (int iThreadCount = 0; iThreadCount < THREAD_COUNTS.length; iThreadCount++) {
            out.print(format(cellFormatInt, THREAD_COUNTS[iThreadCount]));
        }
        out.println();

        for (int iSize = 0; iSize < SIZES.length; iSize++) {
            out.print(format(cellFormatInt, SIZES[iSize]));
            for (int iThreadCount = 0; iThreadCount < THREAD_COUNTS.length; iThreadCount++) {
                out.print(format(cellFormatInt, resultTimes[iSize][iThreadCount]));
            }
            out.println();
        }
    }

    private static Matrix processInitialization(int size, long seed) {
        //out.print(format("Количество вершин в графе %d \n", size));
        Matrix matrix = Matrix.create(size);

        Random random = new Random(seed);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix.set(i, j, random.nextInt(5) - 1);
            }
        }

        //matrix.printMatrix("inited:");

        return matrix;
    }

}
