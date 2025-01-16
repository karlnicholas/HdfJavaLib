package com.github.karlnicholas.hdf5javalib.dataobject;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class FillValueMessage extends HeaderMessage {
    private final byte[] fillValue;

    /**
     * Constructor for `FillValueMessage`.
     *
     * @param fillValue The fill value as a byte array.
     */
    public FillValueMessage(byte[] fillValue) {
        super(0x0004, calculateSize(fillValue), 0);
        this.fillValue = fillValue.clone(); // Defensive copy for immutability
    }

    /**
     * Calculates the size of the fill value message.
     *
     * @param fillValue The fill value byte array.
     * @return The size of the message in bytes.
     */
    private static int calculateSize(byte[] fillValue) {
        return 4 + (fillValue == null ? 0 : fillValue.length); // 4 bytes for size + data
    }

    @Override
    public byte[] toFileData() {
        ByteBuffer buffer = ByteBuffer.allocate(getSize());
        buffer.putInt(fillValue.length); // Size of the fill value
        buffer.put(fillValue);          // Fill value data
        return buffer.array();
    }

    /**
     * Retrieves the fill value.
     *
     * @return The fill value as a byte array.
     */
    public byte[] getFillValue() {
        return fillValue.clone(); // Return a defensive copy
    }

    @Override
    public String toString() {
        return "FillValueMessage{" +
                "fillValue=" + Arrays.toString(fillValue) +
                '}';
    }
}
