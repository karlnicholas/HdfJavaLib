package com.github.karlnicholas.hdf5javalib.level1;

import java.nio.ByteBuffer;

public class Heap {
    private static final String HEAP_SIGNATURE = "HEAP";
    private final int dataSegmentSize;
    private final int offsetToFreeList;
    private final long addressOfDataSegment;

    public Heap(int dataSegmentSize, int offsetToFreeList, long addressOfDataSegment) {
        this.dataSegmentSize = dataSegmentSize;
        this.offsetToFreeList = offsetToFreeList;
        this.addressOfDataSegment = addressOfDataSegment;
    }

    public Heap(ByteBuffer buffer) {
        byte[] signature = new byte[4];
        buffer.get(signature);
        if (!HEAP_SIGNATURE.equals(new String(signature))) {
            throw new IllegalArgumentException("Invalid heap signature");
        }

        buffer.getInt(); // Reserved
        this.dataSegmentSize = buffer.getInt();
        this.offsetToFreeList = buffer.getInt();
        this.addressOfDataSegment = buffer.getLong();
    }

    public byte[] toHdfBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.put(HEAP_SIGNATURE.getBytes());
        buffer.putInt(0); // Reserved
        buffer.putInt(dataSegmentSize);
        buffer.putInt(offsetToFreeList);
        buffer.putLong(addressOfDataSegment);
        return buffer.array();
    }
}
