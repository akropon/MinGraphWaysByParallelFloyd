package com.petrolov;

import java.util.concurrent.atomic.AtomicIntegerArray;

import static java.lang.String.format;
import static java.lang.System.out;

class Matrix {

    private static int MAX_MATRIX_SIZE_TO_SHOW = 10;
    private int mySize;
    private AtomicIntegerArray myValues;

    private Matrix(int size) {
        mySize = size;
        myValues = new AtomicIntegerArray(size * size);
    }

    static Matrix create(int size) {
        return new Matrix(size);
    }

    Matrix copy() {
        Matrix newMatrix = new Matrix(mySize);
        for (int i = 0; i < mySize; i++) {
            for (int j = 0; j < mySize; j++) {
                newMatrix.set(i, j, this.get(i, j));
            }
        }
        return newMatrix;
    }

    int getSize() {
        return mySize;
    }

    int get(int i, int j) {
        return myValues.get(i * mySize + j);
    }

    void set(int i, int j, int newValue) {
        myValues.set(i * mySize + j, newValue);
    }

    void printMatrix(String title) {
        if (title != null) {
            out.println(title);
        }

        boolean isTooBig = mySize > MAX_MATRIX_SIZE_TO_SHOW;
        int showSize = isTooBig ? MAX_MATRIX_SIZE_TO_SHOW : mySize;

        for (int i = 0; i < showSize; i++) {
            for (int j = 0; j < showSize; j++) {
                out.print(format("%4d ", myValues.get(i * mySize + j)));
            }
            if (isTooBig) out.print(" ... ");
            out.print("\n");
        }
        if (isTooBig) {
            for (int i = 0; i < showSize + 1; i++) {
                out.print(format("%4s ", "..."));
            }
            out.println();
        }
    }
}
