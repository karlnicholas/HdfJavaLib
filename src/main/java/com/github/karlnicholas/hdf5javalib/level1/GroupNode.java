package com.github.karlnicholas.hdf5javalib.level1;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GroupNode {
    private static final String NODE_SIGNATURE = "SNOD";
    private final int version;
    private final List<GroupEntry> entries;

    public GroupNode(int version) {
        this.version = version;
        this.entries = new ArrayList<>();
    }

    public GroupNode(ByteBuffer buffer) {
        byte[] signature = new byte[4];
        buffer.get(signature);
        if (!NODE_SIGNATURE.equals(new String(signature))) {
            throw new IllegalArgumentException("Invalid group node signature");
        }

        this.version = Byte.toUnsignedInt(buffer.get());
        buffer.getInt(); // Reserved
        int numberOfSymbols = buffer.getInt();

        this.entries = new ArrayList<>();
        for (int i = 0; i < numberOfSymbols; i++) {
            entries.add(new GroupEntry(buffer));
        }
    }

    public void addEntry(GroupEntry entry) {
        entries.add(entry);
    }

    public byte[] toHdfBytes() {
        int totalSize = 8 + entries.stream().mapToInt(GroupEntry::calculateSize).sum();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.put(NODE_SIGNATURE.getBytes());
        buffer.put((byte) version);
        buffer.putInt(0); // Reserved
        buffer.putInt(entries.size());

        for (GroupEntry entry : entries) {
            buffer.put(entry.toHdfBytes());
        }
        return buffer.array();
    }

    public static class GroupEntry {
        private final String name;
        private final long objectHeaderAddress;

        public GroupEntry(String name, long objectHeaderAddress) {
            this.name = name;
            this.objectHeaderAddress = objectHeaderAddress;
        }

        public GroupEntry(ByteBuffer buffer) {
            this.name = readNullTerminatedString(buffer);
            this.objectHeaderAddress = buffer.getLong();
        }

        public byte[] toHdfBytes() {
            byte[] nameBytes = name.getBytes();
            int paddedNameLength = ((nameBytes.length + 1 + 7) / 8) * 8; // Null-terminated and aligned to 8 bytes
            ByteBuffer buffer = ByteBuffer.allocate(paddedNameLength + 8);
            buffer.put(nameBytes);
            buffer.put((byte) 0); // Null-terminator
            while (buffer.position() % 8 != 0) {
                buffer.put((byte) 0); // Padding
            }
            buffer.putLong(objectHeaderAddress);
            return buffer.array();
        }

        private String readNullTerminatedString(ByteBuffer buffer) {
            StringBuilder sb = new StringBuilder();
            byte b;
            while ((b = buffer.get()) != 0) {
                sb.append((char) b);
            }
            // Align to the next 8-byte boundary
            while (buffer.position() % 8 != 0) {
                buffer.get();
            }
            return sb.toString();
        }

        public int calculateSize() {
            int nameSize = ((name.length() + 1 + 7) / 8) * 8; // Null-terminated and aligned to 8 bytes
            return nameSize + 8;
        }
    }
}
