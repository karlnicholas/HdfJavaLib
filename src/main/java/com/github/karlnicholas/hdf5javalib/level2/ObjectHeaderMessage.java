package com.github.karlnicholas.hdf5javalib.level2;

import java.nio.ByteBuffer;

public abstract class ObjectHeaderMessage {
    private final int messageType;
    private final int dataSize;
    private final boolean isConstant;
    private final boolean isShared;

    protected ObjectHeaderMessage(int messageType, int dataSize, boolean isConstant, boolean isShared) {
        this.messageType = messageType;
        this.dataSize = dataSize;
        this.isConstant = isConstant;
        this.isShared = isShared;
    }

    public static ObjectHeaderMessage readFromBuffer(ByteBuffer buffer) {
        int messageType = buffer.getShort() & 0xFFFF;
        int size = buffer.getShort() & 0xFFFF;
        byte flags = buffer.get();
        buffer.position(buffer.position() + 3); // Skip reserved bytes

        boolean isConstant = (flags & 0x01) != 0;
        boolean isShared = (flags & 0x02) != 0;

        if (messageType == 0x0006) { // Compact Storage Message
            return new CompactStorageMessage(buffer, size, isConstant, isShared);
        }

        throw new IllegalArgumentException("Unsupported message type: " + messageType);
    }

    public abstract byte[] toHdfBytes();

    public abstract int getTotalSize();

    public int getMessageType() {
        return messageType;
    }

    public int getDataSize() {
        return dataSize;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public boolean isShared() {
        return isShared;
    }
}
