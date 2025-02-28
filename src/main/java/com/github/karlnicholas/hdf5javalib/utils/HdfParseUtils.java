package com.github.karlnicholas.hdf5javalib.utils;

import com.github.karlnicholas.hdf5javalib.message.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class HdfParseUtils {
    public static int readIntFromFileChannel(FileChannel fileChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Assume little-endian as per HDF5 spec
        fileChannel.read(buffer);
        buffer.flip();
        return buffer.getInt();
    }

    public static void skipBytes(FileChannel fileChannel, int bytesToSkip) throws IOException {
        fileChannel.position(fileChannel.position() + bytesToSkip);
    }

    // Parse header messages
    public static void parseDataObjectHeaderMessages(FileChannel fileChannel, short objectHeaderSize, short offsetSize, short lengthSize, List<HdfMessage> headerMessages) throws IOException {
        ByteBuffer buffer  = ByteBuffer.allocate(objectHeaderSize).order(ByteOrder.LITTLE_ENDIAN);
        fileChannel.read(buffer);
        buffer.flip();


        while (buffer.hasRemaining()) {
            // Header Message Type (2 bytes, little-endian)
            MessageType type = MessageType.fromValue(buffer.getShort());
            int size = Short.toUnsignedInt(buffer.getShort());
            byte flags = buffer.get();
            buffer.position(buffer.position() + 3); // Skip 3 reserved bytes

            // Header Message Data
            byte[] messageData = new byte[size];
            buffer.get(messageData);

            // Add the message to the list
            headerMessages.add(createMessageInstance(type, flags, messageData, offsetSize, lengthSize, ()-> Arrays.copyOfRange(messageData, 8, messageData.length)));

        }
    }

    public static HdfMessage createMessageInstance(MessageType type, byte flags, byte[] data, short offsetSize, short lengthSize, Supplier<byte[]> getDataTypeData) {
        return switch (type) {
            case NilMessage -> NilMessage.parseHeaderMessage(flags, data, offsetSize, lengthSize);
            case DataspaceMessage -> DataspaceMessage.parseHeaderMessage(flags, data, offsetSize, lengthSize);
            case DatatypeMessage -> DatatypeMessage.parseHeaderMessage(flags, data, offsetSize, lengthSize, getDataTypeData.get());
            case FillValueMessage -> FillValueMessage.parseHeaderMessage(flags, data, offsetSize, lengthSize);
            case DataLayoutMessage -> DataLayoutMessage.parseHeaderMessage(flags, data, offsetSize, lengthSize);
            case AttributeMessage -> AttributeMessage.parseHeaderMessage(flags, data, offsetSize, lengthSize);
            case ObjectHeaderContinuationMessage -> ObjectHeaderContinuationMessage.parseHeaderMessage(flags, data, offsetSize, lengthSize);
            case SymbolTableMessage -> SymbolTableMessage.parseHeaderMessage(flags, data, offsetSize, lengthSize);
            case ObjectModificationTimeMessage -> ObjectModificationTimeMessage.parseHeaderMessage(flags, data, offsetSize, lengthSize);
            case BtreeKValuesMessage -> BTreeKValuesMessage.parseHeaderMessage(flags, data, offsetSize, lengthSize);
            default -> throw new IllegalArgumentException("Unknown message type: " + type);
        };
    }

    // TODO: fix recursion
    public static void parseContinuationMessage(FileChannel fileChannel, ObjectHeaderContinuationMessage objectHeaderContinuationMessage, short offsetSize, short lengthSize, List<HdfMessage> headerMessages) throws IOException {

        long continuationOffset = objectHeaderContinuationMessage.getContinuationOffset().toBigInteger().longValue();
        short continuationSize = objectHeaderContinuationMessage.getContinuationSize().toBigInteger().shortValueExact();

        // Move to the continuation block offset
        fileChannel.position(continuationOffset);

        // Parse the continuation block messages
        parseDataObjectHeaderMessages(fileChannel, continuationSize, offsetSize, lengthSize, headerMessages);
    }

}
