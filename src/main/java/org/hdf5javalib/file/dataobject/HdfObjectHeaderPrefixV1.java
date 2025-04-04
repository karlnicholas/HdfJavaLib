package org.hdf5javalib.file.dataobject;

import lombok.Getter;
import org.hdf5javalib.file.dataobject.message.HdfMessage;
import org.hdf5javalib.file.dataobject.message.ObjectHeaderContinuationMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hdf5javalib.file.dataobject.message.HdfMessage.parseContinuationMessage;
import static org.hdf5javalib.file.dataobject.message.HdfMessage.readMessagesFromByteBuffer;

@Getter
public class HdfObjectHeaderPrefixV1 {
    private final int version;                // 1 byte
//    private final int totalHeaderMessages;    // 2 bytes
    private final long objectReferenceCount;  // 4 bytes
    private final long objectHeaderSize;      // 4 bytes
    // level 2A1A
    @Getter
    private final List<HdfMessage> headerMessages;

    // Constructor for application-defined values
    public HdfObjectHeaderPrefixV1(int version, long objectReferenceCount, long objectHeaderSize, List<HdfMessage> headerMessages) {
        this.version = version;
//        this.totalHeaderMessages = totalHeaderMessages;
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
        dataObjectHeaderMessages.addAll(readMessagesFromByteBuffer(fileChannel, objectHeaderSize, offsetSize, lengthSize));
        for ( HdfMessage hdfMessage: dataObjectHeaderMessages) {
            if (hdfMessage instanceof ObjectHeaderContinuationMessage) {
                dataObjectHeaderMessages.addAll(parseContinuationMessage(fileChannel, (ObjectHeaderContinuationMessage)hdfMessage, offsetSize, lengthSize));
                break;
            }
        }

        // Create the instance
        return new HdfObjectHeaderPrefixV1(version, objectReferenceCount, objectHeaderSize, dataObjectHeaderMessages);
    }

    public void writeToByteBuffer(ByteBuffer buffer) {
        // Write version (1 byte)
        buffer.put((byte) version);

        // Write reserved byte (must be 0)
        buffer.put((byte) 0);

        // Write total header messages (2 bytes, little-endian)
        buffer.putShort((short) headerMessages.size());

        // Write object reference count (4 bytes, little-endian)
        buffer.putInt((int) objectReferenceCount);

        // Write object header size (4 bytes, little-endian)
        buffer.putInt((int) objectHeaderSize);

        // Write reserved field (must be 0) (4 bytes)
        buffer.putInt(0);

        // Write messages, handling continuation as first message but splitting at 6
        Optional<ObjectHeaderContinuationMessage> optContinuationMessage = findMessageByType(ObjectHeaderContinuationMessage.class);

        for (int i = 0; i < headerMessages.size(); i++) {
            HdfMessage hdfMessage = headerMessages.get(i);
            hdfMessage.writeMessageToByteBuffer(buffer);

            // Pad to 8-byte boundary
            int position = buffer.position();
            buffer.position((position + 7) & ~7);

            // After writing 6 messages, jump to continuation offset if present
            if (i == 5 && optContinuationMessage.isPresent()) {
                buffer.position(optContinuationMessage.get().getContinuationOffset().getInstance(Long.class).intValue());
            }
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
                .append(" Total Header Messages: ").append(headerMessages.size())
                .append(" Object Reference Count: ").append(objectReferenceCount)
                .append(" Object Header Size: ").append(objectHeaderSize);
                // Parse header messages
//        dataObjectHeaderMessages.forEach(hm->builder.append("\r\n\t" + hm));
        for( HdfMessage message: headerMessages) {
            String ms = message.toString();
            builder.append("\r\n").append(ms);
        }
        builder.append("}");

        return builder.toString();
    }

    public <T extends HdfMessage> Optional<T> findMessageByType(Class<T> messageClass) {
        for (HdfMessage message : headerMessages) {
            if (messageClass.isInstance(message)) {
                return Optional.of(messageClass.cast(message)); // Avoids unchecked cast warning
            }
        }
        return Optional.empty();
    }


}
