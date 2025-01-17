package com.github.karlnicholas.hdf5javalib.level2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ObjectHeader {
    private static final int HEADER_VERSION = 1;
    private final int version;
    private final int referenceCount;
    private final int totalHeaderSize;
    private final List<ObjectHeaderMessage> messages;

    public ObjectHeader(int referenceCount) {
        this.version = HEADER_VERSION;
        this.referenceCount = referenceCount;
        this.totalHeaderSize = 0; // Will be calculated dynamically
        this.messages = new ArrayList<>();
    }

    public ObjectHeader(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Read version and reserved bytes
        this.version = Byte.toUnsignedInt(buffer.get());
        if (version != HEADER_VERSION) {
            throw new IllegalArgumentException("Unsupported Object Header version: " + version);
        }
        buffer.position(buffer.position() + 1); // Skip reserved byte

        // Read number of messages and reference count
        int numberOfMessages = Byte.toUnsignedInt(buffer.get());
        this.referenceCount = buffer.get();

        // Read total header size
        this.totalHeaderSize = buffer.getInt();

        // Read messages
        this.messages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            messages.add(ObjectHeaderMessage.readFromBuffer(buffer));
        }
    }

    public void addMessage(ObjectHeaderMessage message) {
        this.messages.add(message);
    }

    public byte[] toHdfBytes() {
        int totalSize = calculateTotalSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Write version, reserved, number of messages, and reference count
        buffer.put((byte) version);
        buffer.put((byte) 0); // Reserved
        buffer.put((byte) messages.size());
        buffer.put((byte) referenceCount);

        // Write total header size
        buffer.putInt(totalSize);

        // Write each message
        for (ObjectHeaderMessage message : messages) {
            buffer.put(message.toHdfBytes());
        }

        return buffer.array();
    }

    private int calculateTotalSize() {
        int size = 8; // Header size: 1 (version) + 1 (reserved) + 1 (number of messages) + 1 (reference count) + 4 (total size)
        for (ObjectHeaderMessage message : messages) {
            size += message.getTotalSize();
        }
        return size;
    }

    @Override
    public String toString() {
        return "ObjectHeader{" +
                "version=" + version +
                ", referenceCount=" + referenceCount +
                ", totalHeaderSize=" + totalHeaderSize +
                ", messages=" + messages +
                '}';
    }
}
