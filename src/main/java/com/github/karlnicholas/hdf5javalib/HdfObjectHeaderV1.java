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

    public static HdfObjectHeaderV1 fromByteBuffer(ByteBuffer buffer) {
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
            messages.add(HdfHeaderMessage.fromByteBuffer(buffer));
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

    public static class HdfHeaderMessage {
        private final int type;
        private final int size;
        private final int flags;
        private final byte[] data;

        public HdfHeaderMessage(int type, int size, int flags, byte[] data) {
            this.type = type;
            this.size = size;
            this.flags = flags;
            this.data = data;
        }

        public static HdfHeaderMessage fromByteBuffer(ByteBuffer buffer) {
            // Read header message fields
            int type = Short.toUnsignedInt(buffer.getShort());
            int size = Short.toUnsignedInt(buffer.getShort());
            int flags = Byte.toUnsignedInt(buffer.get());
            buffer.get(new byte[3]); // Skip 3 reserved bytes

            // Read message data
            byte[] data = new byte[size];
            buffer.get(data);

            return new HdfHeaderMessage(type, size, flags, data);
        }

        @Override
        public String toString() {
            return "HdfHeaderMessage{" +
                    "type=" + type +
                    ", size=" + size +
                    ", flags=" + flags +
                    ", data=" + java.util.Arrays.toString(data) +
                    '}';
        }
    }
}
