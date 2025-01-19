package com.github.karlnicholas.hdf5javalib.datatypes;

import java.nio.ByteBuffer;

// Floating-point datatype
public class HdfFloatingPoint extends HdfDatatype {
    public HdfFloatingPoint(int size) {
        super(size);
    }

    @Override
    public Object readData(ByteBuffer buffer) {
        if (getSize() == 4) {
            return buffer.getFloat();
        } else if (getSize() == 8) {
            return buffer.getDouble();
        }
        throw new IllegalArgumentException("Unsupported size for floating-point datatype: " + getSize());
    }
}
