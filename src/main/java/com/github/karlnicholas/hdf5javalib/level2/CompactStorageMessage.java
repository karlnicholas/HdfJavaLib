package com.github.karlnicholas.hdf5javalib.level2;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CompactStorageMessage extends ObjectHeaderMessage {
    private final byte[] data;

    public CompactStorageMessage(byte[] data) {
        super(0x0006, data.length, false, false);
        this.data = Arrays.copyOf(data, data.length);
    }

    public CompactStorageMessage(ByteBuffer buffer, int size, boolean isConstant, boolean isShared) {
        super(0x0006, size, isConstant, isShared);
        this.data = new byte[size];
        buffer.get(this.data);
    }

    @Override
    public byte[] toHdfBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getTotalSize());
        buffer.putShort((short) getMessageType());
        buffer.putShort((short) getDataSize());
        buffer.put((byte) ((isConstant() ? 0x01 : 0) | (isShared() ? 0x02 : 0)));
        buffer.put(new byte[3]); // Reserved
        buffer.put(data);
        return buffer.array();
    }

    @Override
    public int getTotalSize() {
        return 8 + data.length; // Header (8 bytes) + data length
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}
