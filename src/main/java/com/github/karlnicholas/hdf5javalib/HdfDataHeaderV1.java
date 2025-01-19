package com.github.karlnicholas.hdf5javalib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HdfDataHeaderV1 {
    private final int version;                // 1 byte
    private final int totalHeaderMessages;    // 2 bytes
    private final long objectReferenceCount;  // 4 bytes
    private final long objectHeaderSize;      // 4 bytes
    private final List<HdfDataHeaderMessage> headerMessages;
    private final List<Integer> continuationOffsets; // Continuation block offsets

    public HdfDataHeaderV1(ByteBuffer buffer) {
        // Parse Version (1 byte)
        this.version = Byte.toUnsignedInt(buffer.get());

        // Reserved (1 byte, should be zero)
        byte reserved = buffer.get();
        if (reserved != 0) {
            throw new IllegalArgumentException("Reserved byte in Data Object Header is not zero.");
        }

        // Total Number of Header Messages (2 bytes, little-endian)
        this.totalHeaderMessages = Short.toUnsignedInt(buffer.getShort());

        // Object Reference Count (4 bytes, little-endian)
        this.objectReferenceCount = Integer.toUnsignedLong(buffer.getInt());

        // Object Header Size (4 bytes, little-endian)
        this.objectHeaderSize = Integer.toUnsignedLong(buffer.getInt());

        // Reserved (4 bytes, should be zero)
        int reservedInt = buffer.getInt();
        if (reservedInt != 0) {
            throw new IllegalArgumentException("Reserved integer in Data Object Header is not zero.");
        }

        // Initialize lists
        this.headerMessages = new ArrayList<>();
        this.continuationOffsets = new ArrayList<>();

        // Parse header messages
        parseHeaderMessages(buffer, (int) objectHeaderSize);
    }

    private void parseHeaderMessages(ByteBuffer buffer, int remainingBytes) {
        int bytesRead = 0;

        while (bytesRead < remainingBytes) {
            // Header Message Type (2 bytes, little-endian)
            int messageType = Short.toUnsignedInt(buffer.getShort());
            bytesRead += 2;

            // Size of Header Message Data (2 bytes, little-endian)
            int messageDataSize = Short.toUnsignedInt(buffer.getShort());
            bytesRead += 2;

            // Header Message Flags (1 byte)
            byte flags = buffer.get();
            bytesRead += 1;

            // Reserved (3 bytes, should be zero)
            int reservedBytes = (Byte.toUnsignedInt(buffer.get()) << 16) |
                    (Byte.toUnsignedInt(buffer.get()) << 8) |
                    Byte.toUnsignedInt(buffer.get());
            bytesRead += 3;
            if (reservedBytes != 0) {
                throw new IllegalArgumentException("Reserved bytes in Header Message are not zero.");
            }

            // Header Message Data
            byte[] messageData = new byte[messageDataSize];
            buffer.get(messageData);
            bytesRead += messageDataSize;

            // Handle Continuation Blocks
            if (messageType == 16) { // Object Header Continuation
                ByteBuffer continuationBuffer = ByteBuffer.wrap(messageData);
                int continuationOffset = continuationBuffer.getInt();
                continuationOffsets.add(continuationOffset);
            } else {
                // Add the message to the list
                headerMessages.add(new HdfDataHeaderMessage(messageType, messageDataSize, flags, messageData));
            }
        }
    }

    public void parseContinuationBlocks(ByteBuffer buffer) {
        for (int offset : continuationOffsets) {
            // Move to the continuation block offset
            skipToOffset(buffer, offset);

            // Read the continuation block size
            int continuationSize = buffer.getInt();

            // Parse the continuation block messages
            parseHeaderMessages(buffer, continuationSize);
        }
    }

    private void skipToOffset(ByteBuffer buffer, int targetOffset) {
        int remainingBytes = targetOffset;
        while (remainingBytes > 0) {
            buffer.get();
            remainingBytes--;
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

    public List<HdfDataHeaderMessage> getHeaderMessages() {
        return headerMessages;
    }
}
