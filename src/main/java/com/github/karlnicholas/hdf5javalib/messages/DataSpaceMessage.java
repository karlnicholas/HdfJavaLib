package com.github.karlnicholas.hdf5javalib.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DataSpaceMessage implements HdfMessage {
    private int version;
    private int rank;
    private long[] dimensions;

    @Override
    public HdfMessage parseHeaderMessage(ByteBuffer buffer) {
        this.version = Byte.toUnsignedInt(buffer.get());
        this.rank = Byte.toUnsignedInt(buffer.get());
        this.dimensions = new long[rank];
        for (int i = 0; i < rank; i++) {
            dimensions[i] = Integer.toUnsignedLong(buffer.getInt());
        }
        return this;
    }

    @Override
    public String toString() {
        return "DataSpaceMessage{" +
                "version=" + version +
                ", rank=" + rank +
                ", dimensions=" + Arrays.toString(dimensions) +
                '}';
    }
}
