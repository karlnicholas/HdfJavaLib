package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class HdfHeaderMessage {
    private final int type;
    private final int size;
    private final int flags;
    private final byte[] data;
    private final Object parsedMessage;

    public HdfHeaderMessage(int type, int size, int flags, byte[] data, Object parsedMessage) {
        this.type = type;
        this.size = size;
        this.flags = flags;
        this.data = data;
        this.parsedMessage = parsedMessage;
    }

    /**
     * Parses an HdfHeaderMessage from a ByteBuffer.
     *
     * @param buffer     The ByteBuffer containing the Header Message data.
     * @param offsetSize The size of offsets specified in the superblock (in bytes).
     * @return A parsed HdfHeaderMessage instance.
     */
    public static HdfHeaderMessage fromByteBuffer(ByteBuffer buffer, int offsetSize) {
        int type = Short.toUnsignedInt(buffer.getShort());
        int size = Short.toUnsignedInt(buffer.getShort());
        int flags = Byte.toUnsignedInt(buffer.get());
        buffer.position(buffer.position() + 3); // Skip 3 reserved bytes

        byte[] data = new byte[size];
        buffer.get(data);

        // Wrap the data bytes in a new ByteBuffer with little-endian order
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        dataBuffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // Parse the message based on its type
        Object parsedMessage = null;
        if (type == 0x0011) { // Symbol Table Message
            parsedMessage = HdfSymbolTableMessage.fromByteBuffer(dataBuffer, offsetSize);
        }

        return new HdfHeaderMessage(type, size, flags, data, parsedMessage);
    }

    public int getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public int getFlags() {
        return flags;
    }

    public byte[] getData() {
        return data;
    }

    public Object getParsedMessage() {
        return parsedMessage;
    }

    @Override
    public String toString() {
        String parsedMessageString = (parsedMessage != null) ? parsedMessage.toString() : "Raw Data: " + Arrays.toString(data);
        return "HdfHeaderMessage{" +
                "type=" + type +
                ", size=" + size +
                ", flags=" + flagsToString() +
                ", parsedMessage=" + parsedMessageString +
                '}';
    }

    private String flagsToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(flags).append(" (");
        if ((flags & 0x01) != 0) sb.append("Constant, ");
        if ((flags & 0x02) != 0) sb.append("Shared, ");
        if ((flags & 0x04) != 0) sb.append("Not Shareable, ");
        if ((flags & 0x08) != 0) sb.append("Fail on Unknown, ");
        if ((flags & 0x10) != 0) sb.append("Invalidate, ");
        if ((flags & 0x20) != 0) sb.append("Modified by Unknown, ");
        if ((flags & 0x40) != 0) sb.append("Shareable, ");
        if ((flags & 0x80) != 0) sb.append("Fail Always, ");
        if (sb.length() > 3) sb.setLength(sb.length() - 2); // Remove trailing comma and space
        sb.append(")");
        return sb.toString();
    }
}
