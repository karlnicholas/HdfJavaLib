package com.github.karlnicholas.hdf5javalib.messages;

import java.nio.ByteBuffer;

public class ObjectModificationTimeMessage implements HdfMessage {
    private int version;
    private long secondsAfterEpoch;

    @Override
    public HdfMessage parseHeaderMessage(ByteBuffer buffer) {
        // Parse version
        this.version = Byte.toUnsignedInt(buffer.get());

        // Skip reserved byte
        buffer.get();

        // Parse seconds after UNIX epoch
        this.secondsAfterEpoch = Integer.toUnsignedLong(buffer.getInt());

        return this;
    }

    public int getVersion() {
        return version;
    }

    public long getSecondsAfterEpoch() {
        return secondsAfterEpoch;
    }

    @Override
    public String toString() {
        return "ObjectModificationTimeMessage{" +
                "version=" + version +
                ", secondsAfterEpoch=" + secondsAfterEpoch +
                '}';
    }
}
