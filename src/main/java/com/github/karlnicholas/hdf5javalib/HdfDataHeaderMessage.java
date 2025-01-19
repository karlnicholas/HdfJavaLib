package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.messages.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class HdfDataHeaderMessage {
    private final int type;
    private final int size;
    private final byte flags;
    private final byte[] data;
    private final HdfMessage parsedMessage;

    public HdfDataHeaderMessage(int type, int size, byte flags, byte[] data) {
        this.type = type;
        this.size = size;
        this.flags = flags;
        this.data = data;

        // Instantiate and parse the appropriate message class
        this.parsedMessage = createMessageInstance(type, data);
    }

    private HdfMessage createMessageInstance(int type, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        switch (type) {
            case 0:
                return new NullMessage().parseHeaderMessage(buffer);
            case 3:
                return new DataTypeMessage().parseHeaderMessage(buffer); // Corrected name
            case 5:
                return new FillValueMessage().parseHeaderMessage(buffer);
            case 8:
                return new DataSpaceMessage().parseHeaderMessage(buffer);
            case 18:
                return new AttributeMessage().parseHeaderMessage(buffer);
            default:
                throw new IllegalArgumentException("Unknown message type: " + type);
        }
    }

    public int getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public byte getFlags() {
        return flags;
    }

    public byte[] getData() {
        return data;
    }

    public HdfMessage getParsedMessage() {
        return parsedMessage;
    }

    @Override
    public String toString() {
        return "HdfDataHeaderMessage{" +
                "type=" + type +
                ", size=" + size +
                ", flags=" + flags +
                ", parsedMessage=" + (parsedMessage != null ? parsedMessage.toString() : "Raw Data: " + Arrays.toString(data)) +
                '}';
    }
}
