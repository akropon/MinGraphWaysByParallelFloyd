package com.petrolov;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class FloydParallelProcessor {

    private int myNumOfThreads;
    private ExecutorService[] myExecutorServices;
    private Future<?>[] myFutures;

    FloydParallelProcessor() {
        setNumOfThreads(1);
    }

    void setNumOfThreads(int numOfThreads) {
        if (numOfThreads == myNumOfThreads) return;

        shutdownAllThreads();

        myNumOfThreads = numOfThreads;
        myExecutorServices = new ExecutorService[myNumOfThreads];
        for (int i = 0; i < myExecutorServices.length; i++) {
            myExecutorServices[i] = Executors.newSingleThreadExecutor();
        }
        myFutures = new Future[myNumOfThreads];
    }

    void dispose() {
        shutdownAllThreads();
    }

    private void shutdownAllThreads() {
        if (myExecutorServices != null) {
            for (ExecutorService executorService : myExecutorServices) {
                executorService.shutdown();
            }
        }
    }


    Result process(Matrix matrix) {
        Matrix mutableMatrix = matrix.copy();

        long startTime = System.currentTimeMillis();

        doFloyd(mutableMatrix);

        long stopTime = System.currentTimeMillis();

        Result result = new Result();
        result.matrix = mutableMatrix;
        result.time = stopTime - startTime;
        return result;
    }

    private void doFloyd(Matrix matrix) {
        int size = matrix.getSize();

        double rowsCountPerThread = (double) size / myNumOfThreads;
        int[] startRowsIndexes = new int[1 + myNumOfThreads];

        double iLastRow = 0;
        startRowsIndexes[0] = 0;
        for (int iThread = 1; iThread <= myNumOfThreads - 1; iThread++) {
            iLastRow += rowsCountPerThread;
            startRowsIndexes[iThread] = (int) (iLastRow + 1);
        }
        startRowsIndexes[myNumOfThreads] = size;

        //System.out.println("startRowsIndexes");
        //System.out.println(Arrays.toString(startRowsIndexes));

        for (int iStep = 0; iStep < size; iStep++) {
            doStep(iStep, matrix, size, startRowsIndexes);
        }
    }

    private void doStep(int iStep, Matrix matrix, int size, int[] startRowsIndexes) {

        for (int iThread = 0; iThread < myNumOfThreads; iThread++) {
            int iFirstRow = startRowsIndexes[iThread];
            int iLastRow = startRowsIndexes[iThread + 1] - 1;
            myFutures[iThread] = myExecutorServices[iThread].submit(
                    () -> processRows(iStep, iFirstRow, iLastRow, matrix, size));
        }

        for (int iThread = 0; iThread < myNumOfThreads; iThread++) {
            try {
                myFutures[iThread].get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void processRows(int iStep, int iFirstRow, int iLastRow, Matrix matrix, int size) {
        //System.out.println(String.format("step=%d, rows=[%d,%d]", iStep, iFirstRow, iLastRow));
        for (int iRow = iFirstRow; iRow <= iLastRow; iRow++) {
            handleRow(iStep, iRow, matrix, size);
        }
    }

    private void handleRow(int k, int i, Matrix matrix, int size) {
        for (int j = 0; j < size; j++) {
            handleCell(k, i, j, matrix);
        }
    }

    private void handleCell(int k, int i, int j, Matrix matrix) {
        if ((matrix.get(i, k) != -1) && (matrix.get(k, j) != -1)) {
            int case1 = matrix.get(i, j);
            int case2 = matrix.get(i, k) + matrix.get(k, j);
            matrix.set(i, j, min(case1, case2));
        }
    }

    /* This method looks strange, because we want to spend +- the same time for this method for any arguments sets */
    private int min(int A, int B) {
        int result = Math.min(A, B);
        if ((A < 0) && (B >= 0)) result = B;
        if ((B < 0) && (A >= 0)) result = A;
        return result;
    }
}
