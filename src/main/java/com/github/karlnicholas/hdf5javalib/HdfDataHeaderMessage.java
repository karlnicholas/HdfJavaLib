package com.github.karlnicholas.hdf5javalib;

import java.util.Arrays;

public class HdfDataHeaderMessage {
    private final int type;    // Header message type
    private final int size;    // Size of header message data
    private final byte flags;  // Header message flags
    private final byte[] data; // Header message data

    public HdfDataHeaderMessage(int type, int size, byte flags, byte[] data) {
        this.type = type;
        this.size = size;
        this.flags = flags;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public byte getFlags() {
        return flags;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "HdfDataHeaderMessage{" +
                "type=" + type +
                ", size=" + size +
                ", flags=" + flags +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
