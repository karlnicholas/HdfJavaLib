package com.github.karlnicholas.hdf5javalib;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ObjectHeader {
    private final int version;
    private final int headerSize;
    private final int chunkSize;
    private final int entryCount;

    public ObjectHeader(int version, int headerSize, int chunkSize, int entryCount) {
        this.version = version;
        this.headerSize = headerSize;
        this.chunkSize = chunkSize;
        this.entryCount = entryCount;
    }

    public static ObjectHeader readFromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        int version = Byte.toUnsignedInt(buffer.get());
        int headerSize = Byte.toUnsignedInt(buffer.get());
        int chunkSize = buffer.getShort();
        int entryCount = buffer.getShort();

        return new ObjectHeader(version, headerSize, chunkSize, entryCount);
    }

    public byte[] writeToBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put((byte) version);
        buffer.put((byte) headerSize);
        buffer.putShort((short) chunkSize);
        buffer.putShort((short) entryCount);

        return buffer.array();
    }

    // ✅ Getters
    public int getVersion() {
        return version;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getEntryCount() {
        return entryCount;
    }

    @Override
    public String toString() {
        return "ObjectHeader{" +
                "version=" + version +
                ", headerSize=" + headerSize +
                ", chunkSize=" + chunkSize +
                ", entryCount=" + entryCount +
                '}';
    }
}
