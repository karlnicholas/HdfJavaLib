package com.github.karlnicholas.hdf5javalib.datatypes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

// String datatype
public class HdfString extends HdfDatatype {
    public HdfString(int size) {
        super(size);
    }

    @Override
    public Object readData(ByteBuffer buffer) {
        byte[] stringBytes = new byte[getSize()];
        buffer.get(stringBytes);
        return new String(stringBytes, StandardCharsets.UTF_8).trim();
    }
}
