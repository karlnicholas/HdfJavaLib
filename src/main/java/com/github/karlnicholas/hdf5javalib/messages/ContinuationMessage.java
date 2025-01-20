package com.github.karlnicholas.hdf5javalib.messages;

import java.nio.ByteBuffer;

public class ContinuationMessage implements HdfMessage {
    private long continuationOffset; // Offset of the continuation block
    private long continuationSize;   // Size of the continuation block

    @Override
    public ContinuationMessage parseHeaderMessage(ByteBuffer buffer) {
        // Parse the continuation offset and size
        this.continuationOffset = Integer.toUnsignedLong(buffer.getInt());
        this.continuationSize = Integer.toUnsignedLong(buffer.getInt());
        return this;
    }

    public long getContinuationOffset() {
        return continuationOffset;
    }

    public long getContinuationSize() {
        return continuationSize;
    }

    @Override
    public String toString() {
        return "ContinuationMessage{" +
                "continuationOffset=" + continuationOffset +
                ", continuationSize=" + continuationSize +
                '}';
    }
}
