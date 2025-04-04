package org.hdf5javalib.dataclass;

import org.hdf5javalib.file.dataobject.message.datatype.FloatingPointDatatype;

import java.nio.ByteBuffer;

public class HdfFloatPoint implements HdfData {
    private final byte[] bytes;
    private final FloatingPointDatatype datatype;

    public HdfFloatPoint(byte[] bytes, FloatingPointDatatype datatype) {
        this.bytes = bytes;
        this.datatype = datatype;
    }

    @Override
    public String toString() {
        return datatype.getInstance(String.class, bytes);
    }

    @Override
    public void writeValueToByteBuffer(ByteBuffer buffer) {
        buffer.put(bytes);
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return datatype.getInstance(clazz, bytes);
    }
}
