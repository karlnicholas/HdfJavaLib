package com.github.karlnicholas.hdf5javalib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HdfObjectHeaderV1 {
    private final int version;
    private final int totalHeaderMessages;
    private final long objectReferenceCount;
    private final long objectHeaderSize;
    private final List<HdfHeaderMessage> headerMessages;

    public HdfObjectHeaderV1(ByteBuffer buffer, int offsetSize) {
        // Parse Version (1 byte)
        this.version = Byte.toUnsignedInt(buffer.get());

        // Reserved byte (should be zero)
        byte reserved = buffer.get();
        if (reserved != 0) {
            throw new IllegalArgumentException("Invalid reserved byte in object header: " + reserved);
        }

        // Total Number of Header Messages (2 bytes, little-endian)
        this.totalHeaderMessages = Short.toUnsignedInt(buffer.getShort());

        // Object Reference Count (4 bytes, little-endian)
        this.objectReferenceCount = Integer.toUnsignedLong(buffer.getInt());

        // Object Header Size (4 bytes, little-endian)
        this.objectHeaderSize = Integer.toUnsignedLong(buffer.getInt());

        // Reserved field (4 bytes, should be zero)
        int reservedInt = buffer.getInt();
        if (reservedInt != 0) {
            throw new IllegalArgumentException("Invalid reserved integer in object header: " + reservedInt);
        }

        // Parse Header Messages
        this.headerMessages = new ArrayList<>();
        parseHeaderMessages(buffer, offsetSize);
    }

    private void parseHeaderMessages(ByteBuffer buffer, int offsetSize) {
        int bytesRemaining = (int) objectHeaderSize;

        for (int i = 0; i < totalHeaderMessages; i++) {
            if (bytesRemaining <= 0) {
                break;
            }

            // Parse the header message using the HdfHeaderMessage class
            HdfHeaderMessage message = HdfHeaderMessage.fromByteBuffer(buffer, offsetSize);
            headerMessages.add(message);

            // Account for message size and align to an 8-byte boundary
            int messageSize = 4 + message.getSize(); // 4 bytes for type, size, flags, and reserved
            int padding = (8 - (messageSize % 8)) % 8;
            bytesRemaining -= (messageSize + padding);

            // Consume padding bytes if present
            for (int j = 0; j < padding; j++) {
                if (bytesRemaining > 0) {
                    buffer.get();
                    bytesRemaining--;
                }
            }
        }
    }

    public int getVersion() {
        return version;
    }

    public int getTotalHeaderMessages() {
        return totalHeaderMessages;
    }

    public long getObjectReferenceCount() {
        return objectReferenceCount;
    }

    public long getObjectHeaderSize() {
        return objectHeaderSize;
    }

    public List<HdfHeaderMessage> getHeaderMessages() {
        return headerMessages;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HdfObjectHeaderV1{")
                .append("version=").append(version)
                .append(", totalHeaderMessages=").append(totalHeaderMessages)
                .append(", objectReferenceCount=").append(objectReferenceCount)
                .append(", objectHeaderSize=").append(objectHeaderSize)
                .append(", headerMessages=").append(headerMessages)
                .append('}');
        return sb.toString();
    }
}
