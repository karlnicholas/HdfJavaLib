package com.github.karlnicholas.hdf5javalib.time;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

public class HdfTime {

    private final Instant timestamp;

    // Constructor (byte[] input as epoch milliseconds)
    public HdfTime(byte[] bytes) {
        long epochMillis = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
        this.timestamp = Instant.ofEpochMilli(epochMillis);
    }

    // Get the timestamp
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return timestamp.toString();
    }
}
