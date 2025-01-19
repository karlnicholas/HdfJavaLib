package com.github.karlnicholas.hdf5javalib.datatypes;

import java.nio.ByteBuffer;

public class HdfFixedPoint extends HdfDatatype {
    private final boolean signed;

    public HdfFixedPoint(int size, boolean signed) {
        super(size);
        this.signed = signed;
    }

    public boolean isSigned() {
        return signed;
    }

    @Override
    public Object readData(ByteBuffer buffer) {
        if (getSize() == 4) {
            return signed ? buffer.getInt() : buffer.getInt() & 0xFFFFFFFFL;
        } else if (getSize() == 8) {
            return signed ? buffer.getLong() : buffer.getLong() & 0xFFFFFFFFFFFFFFFFL;
        }
        throw new IllegalArgumentException("Unsupported size for fixed-point datatype: " + getSize());
    }
}
