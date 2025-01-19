package com.github.karlnicholas.hdf5javalib.datatypes;

import java.nio.ByteBuffer;

public abstract class HdfDatatype {
    private final int size;

    public HdfDatatype(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public abstract Object readData(ByteBuffer buffer);
}

