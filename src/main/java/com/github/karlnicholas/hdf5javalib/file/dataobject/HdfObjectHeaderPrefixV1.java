package com.github.karlnicholas.hdf5javalib.file.dataobject;

import com.github.karlnicholas.hdf5javalib.message.HdfMessage;
import com.github.karlnicholas.hdf5javalib.message.ObjectHeaderContinuationMessage;
import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static com.github.karlnicholas.hdf5javalib.utils.HdfParseUtils.parseContinuationMessage;
import static com.github.karlnicholas.hdf5javalib.utils.HdfParseUtils.parseDataObjectHeaderMessages;

@Getter
public class HdfObjectHeaderPrefixV1 {
    private final int version;                // 1 byte
    private final int totalHeaderMessages;    // 2 bytes
    private final long objectReferenceCount;  // 4 bytes
    private final long objectHeaderSize;      // 4 bytes
    // level 2A1A
    @Getter
    private final List<HdfMessage> headerMessages;

    // Constructor for application-defined values
    public HdfObjectHeaderPrefixV1(int version, int totalHeaderMessages, long objectReferenceCount, long objectHeaderSize, List<HdfMessage> headerMessages) {
        this.version = version;
        this.totalHeaderMessages = totalHeaderMessages;
        this.objectReferenceCount = objectReferenceCount;
        this.objectHeaderSize = objectHeaderSize;
        this.headerMessages = headerMessages;
    }

    // Factory method to read from a FileChannel
    public static HdfObjectHeaderPrefixV1 readFromFileChannel(FileChannel fileChannel, short offsetSize, short lengthSize) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN); // Buffer for the fixed-size header
        fileChannel.read(buffer);
        buffer.flip();

        // Parse Version (1 byte)
        int version = Byte.toUnsignedInt(buffer.get());

        // Reserved (1 byte, should be zero)
        byte reserved = buffer.get();
        if (reserved != 0) {
            throw new IllegalArgumentException("Reserved byte in Data Object Header Prefix is not zero.");
        }

        // Total Number of Header Messages (2 bytes, little-endian)
        int totalHeaderMessages = Short.toUnsignedInt(buffer.getShort());

        // Object Reference Count (4 bytes, little-endian)
        long objectReferenceCount = Integer.toUnsignedLong(buffer.getInt());

        // Object Header Size (4 bytes, little-endian)
        short objectHeaderSize = (short) buffer.getInt();

        // Reserved (4 bytes, should be zero)
        int reservedInt = buffer.getInt();
        if (reservedInt != 0) {
            throw new IllegalArgumentException("Reserved integer in Data Object Header Prefix is not zero.");
        }
        List<HdfMessage> dataObjectHeaderMessages = new ArrayList<>();
        parseDataObjectHeaderMessages(fileChannel, objectHeaderSize, offsetSize, lengthSize, dataObjectHeaderMessages);
        for ( HdfMessage hdfMessage: dataObjectHeaderMessages) {
            if (hdfMessage instanceof ObjectHeaderContinuationMessage) {
                parseContinuationMessage(fileChannel, (ObjectHeaderContinuationMessage)hdfMessage, offsetSize, lengthSize, dataObjectHeaderMessages);
                break;
            }
        }

        // Create the instance
        return new HdfObjectHeaderPrefixV1(version, totalHeaderMessages, objectReferenceCount, objectHeaderSize, dataObjectHeaderMessages);
    }

    public void writeToByteBuffer(ByteBuffer buffer) {
        // Write version (1 byte)
        buffer.put((byte) version);

        // Write reserved byte (must be 0)
        buffer.put((byte) 0);

        // Write total header messages (2 bytes, little-endian)
        buffer.putShort((short) totalHeaderMessages);

        // Write object reference count (4 bytes, little-endian)
        buffer.putInt((int) objectReferenceCount);

        // Write object header size (4 bytes, little-endian)
        buffer.putInt((int) objectHeaderSize);

        // Write reserved field (must be 0) (4 bytes)
        buffer.putInt(0);

        // Write the first message if it's a SymbolTableMessage
        for( HdfMessage hdfMessage: headerMessages) {
            hdfMessage.writeToByteBuffer(buffer);
            // set position 8 byte boundary
            int position = buffer.position();
            buffer.position((position + 7) & ~7);
        }
    }

//    public Optional<HdfFixedPoint> getDataAddress() {
//        for (HdfMessage message : headerMessages) {
//            if (message instanceof DataLayoutMessage layoutMessage) {
//                return Optional.of(layoutMessage.getDataAddress());
//            }
//        }
//        return Optional.empty();
//    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HdfObjectHeaderPrefixV1 {")
                .append(" Version: ").append(version)
                .append(" Total Header Messages: ").append(totalHeaderMessages)
                .append(" Object Reference Count: ").append(objectReferenceCount)
                .append(" Object Header Size: ").append(objectHeaderSize);
                // Parse header messages
//        dataObjectHeaderMessages.forEach(hm->builder.append("\r\n\t" + hm));
        for( HdfMessage message: headerMessages) {
            String ms = message.toString();
            builder.append("\r\n\t\t").append(ms);
        }
        builder.append("}");

        return builder.toString();
    }

//    public <T extends HdfMessage> Optional<T> findHdfSymbolTableMessage(Class<T> messageClass) {
//        for (HdfMessage message : headerMessages) {
//            if (messageClass.isInstance(message)) {
//                return Optional.of(messageClass.cast(message)); // Avoids unchecked cast warning
//            }
//        }
//        return Optional.empty();
//    }


}
