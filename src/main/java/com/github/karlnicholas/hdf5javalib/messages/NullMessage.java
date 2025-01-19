package com.github.karlnicholas.hdf5javalib.messages;

import java.nio.ByteBuffer;

public class NullMessage implements HdfMessage {

    @Override
    public HdfMessage parseHeaderMessage(ByteBuffer buffer) {
        // No data to parse for null message
        return this;
    }

    @Override
    public String toString() {
        return "NullMessage{}";
    }
}
