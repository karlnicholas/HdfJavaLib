package com.github.karlnicholas.hdf5javalib.messages;

import java.nio.ByteBuffer;

public interface HdfMessage {
    /**
     * Parse a header message from the given ByteBuffer.
     *
     * @param buffer ByteBuffer containing the message data.
     * @return An instance of the message class.
     */
    HdfMessage parseHeaderMessage(ByteBuffer buffer);

    /**
     * Generate a string representation of the message.
     *
     * @return A string describing the message.
     */
    @Override
    String toString();
}
