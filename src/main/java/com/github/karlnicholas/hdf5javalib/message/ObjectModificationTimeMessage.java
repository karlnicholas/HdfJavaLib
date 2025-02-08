package com.github.karlnicholas.hdf5javalib.message;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Getter
public class ObjectModificationTimeMessage extends HdfMessage {
    private final int version;
    private final long secondsAfterEpoch;

    // Constructor to initialize all fields
    public ObjectModificationTimeMessage(int version, long secondsAfterEpoch) {
        super((short) 18, ()-> (short) 8, (byte)0);
        this.version = version;
        this.secondsAfterEpoch = secondsAfterEpoch;
    }

    /**
     * Parses the header message and returns a constructed instance.
     *
     * @param flags      Flags associated with the message (not used here).
     * @param data       Byte array containing the header message data.
     * @param offsetSize Size of offsets in bytes (not used here).
     * @param lengthSize Size of lengths in bytes (not used here).
     * @return A fully constructed `ObjectModificationTimeMessage` instance.
     */
    public static HdfMessage parseHeaderMessage(byte flags, byte[] data, int offsetSize, int lengthSize) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        // Parse version
        int version = Byte.toUnsignedInt(buffer.get());

        // Skip reserved byte
        buffer.get();

        // Parse seconds after UNIX epoch
        long secondsAfterEpoch = Integer.toUnsignedLong(buffer.getInt());

        // Return a constructed instance of ObjectModificationTimeMessage
        return new ObjectModificationTimeMessage(version, secondsAfterEpoch);
    }

    @Override
    public String toString() {
        return "ObjectModificationTimeMessage{" +
                "version=" + version +
                ", secondsAfterEpoch=" + secondsAfterEpoch +
                '}';
    }

    @Override
    public void writeToByteBuffer(ByteBuffer buffer) {
        writeMessageData(buffer);
        buffer.put((byte) version);
        // Skip reserved byte
        buffer.put((byte) 0);
        // Parse seconds after UNIX epoch
        buffer.putInt((int) secondsAfterEpoch);
    }
}
