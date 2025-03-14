package org.hdf5javalib.file.dataobject.message;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Getter
public class FillValueMessage extends HdfMessage {
    private final int version;              // 1 byte
    private final int spaceAllocationTime;  // 1 byte
    private final int fillValueWriteTime;   // 1 byte
    private final int fillValueDefined;     // 1 byte
    private final int size;       // Size of the Fill Value field (optional, unsigned 4 bytes)
    private final byte[] fillValue;         // Fill Value field (optional)

    // Constructor to initialize all fields
    public FillValueMessage(
            int version,
            int spaceAllocationTime,
            int fillValueWriteTime,
            int fillValueDefined,
            int size,
            byte[] fillValue
    ) {
        super(MessageType.FillValueMessage, ()-> (short) (8), (byte)1);
        this.version = version;
        this.spaceAllocationTime = spaceAllocationTime;
        this.fillValueWriteTime = fillValueWriteTime;
        this.fillValueDefined = fillValueDefined;
        this.size = size;
        this.fillValue = fillValue;
    }

    /**
     * Parses the header message and returns a constructed instance.
     *
     * @param flags      Flags associated with the message (not used here).
     * @param data       Byte array containing the header message data.
     * @param offsetSize Size of offsets in bytes (not used here).
     * @param lengthSize Size of lengths in bytes (not used here).
     * @return A fully constructed `FillValueMessage` instance.
     */
    public static HdfMessage parseHeaderMessage(byte flags, byte[] data, int offsetSize, int lengthSize) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        // Parse the first 4 bytes
        int version = Byte.toUnsignedInt(buffer.get());
        int spaceAllocationTime = Byte.toUnsignedInt(buffer.get());
        int fillValueWriteTime = Byte.toUnsignedInt(buffer.get());
        int fillValueDefined = Byte.toUnsignedInt(buffer.get());

        // Initialize optional fields
        int size = 0;
        byte[] fillValue = null;

        // Handle Version 2+ behavior and fillValueDefined flag
        if (version >= 2 && fillValueDefined == 1) {
            // Parse Size (unsigned 4 bytes, using HdfFixedPoint)
            size =buffer.getInt();

            // Parse Fill Value
            fillValue = new byte[size];
            buffer.get(fillValue);
        }

        // Return a constructed instance of FillValueMessage
        return new FillValueMessage(version, spaceAllocationTime, fillValueWriteTime, fillValueDefined, size, fillValue);
    }

    @Override
    public String toString() {
        return "FillValueMessage{" +
                "version=" + version +
                ", spaceAllocationTime=" + spaceAllocationTime +
                ", fillValueWriteTime=" + fillValueWriteTime +
                ", fillValueDefined=" + fillValueDefined +
                ", size=" + size +
                ", fillValue=" + (fillValue != null ? fillValue.length + " bytes" : "undefined") +
                '}';
    }

    @Override
    public void writeToByteBuffer(ByteBuffer buffer) {
        writeMessageData(buffer);
        // Parse the first 4 bytes
        buffer.put((byte) version);
        buffer.put((byte) spaceAllocationTime);
        buffer.put((byte) fillValueWriteTime);
        buffer.put((byte) fillValueDefined);

        // Handle Version 2+ behavior and fillValueDefined flag
        if (version >= 2 && fillValueDefined == 1) {
            // Parse Size (unsigned 4 bytes, using HdfFixedPoint)
            buffer.putInt(size);
            buffer.put(fillValue);
        }
    }
}
