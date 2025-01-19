package com.github.karlnicholas.hdf5javalib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HdfObjectHeaderV1 {
    private final int version;
    private final int totalMessages;
    private final int objectReferenceCount;
    private final int objectHeaderSize;
    private final List<HdfHeaderMessage> headerMessages;

    public HdfObjectHeaderV1(int version, int totalMessages, int objectReferenceCount, int objectHeaderSize, List<HdfHeaderMessage> headerMessages) {
        this.version = version;
        this.totalMessages = totalMessages;
        this.objectReferenceCount = objectReferenceCount;
        this.objectHeaderSize = objectHeaderSize;
        this.headerMessages = headerMessages;
    }

    public static HdfObjectHeaderV1 fromByteBuffer(ByteBuffer buffer, int offsetSize) {
        // Read fixed fields
        int version = Byte.toUnsignedInt(buffer.get());
        int reserved1 = Byte.toUnsignedInt(buffer.get());
        if (reserved1 != 0) {
            throw new IllegalArgumentException("Reserved field must be zero");
        }

        int totalMessages = Short.toUnsignedInt(buffer.getShort());
        int objectReferenceCount = buffer.getInt();
        int objectHeaderSize = buffer.getInt();
        int reserved2 = buffer.getInt();
        if (reserved2 != 0) {
            throw new IllegalArgumentException("Reserved field must be zero");
        }

        // Parse header messages
        List<HdfHeaderMessage> messages = new ArrayList<>();
        for (int i = 0; i < totalMessages; i++) {
            messages.add(HdfHeaderMessage.fromByteBuffer(buffer, offsetSize));
        }

        return new HdfObjectHeaderV1(version, totalMessages, objectReferenceCount, objectHeaderSize, messages);
    }

    @Override
    public String toString() {
        return "HdfObjectHeaderV1{" +
                "version=" + version +
                ", totalMessages=" + totalMessages +
                ", objectReferenceCount=" + objectReferenceCount +
                ", objectHeaderSize=" + objectHeaderSize +
                ", headerMessages=" + headerMessages +
                '}';
    }
}
