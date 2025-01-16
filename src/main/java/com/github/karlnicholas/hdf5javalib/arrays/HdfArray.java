package com.github.karlnicholas.hdf5javalib.arrays;

import java.util.Arrays;

public class HdfArray<T> {

    private final T[] values;

    // Constructor
    public HdfArray(T[] values) {
        this.values = values;
    }

    // Get the values
    public T[] getValues() {
        return values;
    }

    // Get the values as a string
    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
