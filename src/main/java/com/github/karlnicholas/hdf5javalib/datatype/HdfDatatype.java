package com.github.karlnicholas.hdf5javalib.datatype;

import java.nio.ByteBuffer;

public interface HdfDatatype {
    short getSizeMessageData();
    void writeDefinitionToByteBuffer(ByteBuffer buffer);

    int getSize();
//    short getSize();
}
