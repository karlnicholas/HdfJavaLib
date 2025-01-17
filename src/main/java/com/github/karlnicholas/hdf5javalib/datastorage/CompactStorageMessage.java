package com.github.karlnicholas.hdf5javalib.datastorage;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CompactStorageMessage {
    private static final int MESSAGE_TYPE = 0x0006;
    private final byte[] data;

    public CompactStorageMessage(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    public CompactStorageMessage(ByteBuffer buffer, int messageLength) {
        this.data = new byte[messageLength];
        buffer.get(this.data);
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    public int getMessageType() {
        return MESSAGE_TYPE;
    }

    public int getMessageLength() {
        return data.length;
    }

    public void writeToBuffer(ByteBuffer buffer) {
        buffer.put(data);
    }

    @Override
    public String toString() {
        return "CompactStorageMessage{" +
                "messageType=" + MESSAGE_TYPE +
                ", messageLength=" + data.length +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
