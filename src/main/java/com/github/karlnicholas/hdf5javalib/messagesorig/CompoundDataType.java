package com.github.karlnicholas.hdf5javalib.messagesorig;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CompoundDataType extends DatatypeMessage {
    private final List<MemberDefinition> members;

    public CompoundDataType(int version, int size) {
        super(version, 6, size);
        this.members = new ArrayList<>();
    }

    public CompoundDataType(ByteBuffer buffer) {
        super((buffer.get() & 0xF0) >> 4, 6, buffer.getInt());
        int numberOfMembers = buffer.getShort() & 0xFFFF;
        buffer.getShort(); // Reserved (2 bytes)
        this.members = new ArrayList<>();
        for (int i = 0; i < numberOfMembers; i++) {
            members.add(new MemberDefinition(buffer));
        }
    }

    public void addMember(String name, int byteOffset, DatatypeMessage datatype) {
        members.add(new MemberDefinition(name, byteOffset, datatype));
    }

    @Override
    public byte[] toHdfBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(calculateTotalSize());
        buffer.put((byte) ((version << 4) | typeClass));
        buffer.putInt(size);
        buffer.putShort((short) members.size());
        buffer.putShort((short) 0); // Reserved
        for (MemberDefinition member : members) {
            buffer.put(member.toHdfBytes());
        }
        return buffer.array();
    }

    private int calculateTotalSize() {
        int size = 8; // Fixed header size (1 + 4 + 2 + 2 bytes)
        for (MemberDefinition member : members) {
            size += member.calculateSize();
        }
        return size;
    }

    @Override
    public String toString() {
        return "CompoundDatatype{" +
                "version=" + version +
                ", size=" + size +
                ", members=" + members +
                '}';
    }

    public static class MemberDefinition {
        private final String name;
        private final int byteOffset;
        private final DatatypeMessage datatype;

        public MemberDefinition(String name, int byteOffset, DatatypeMessage datatype) {
            this.name = name;
            this.byteOffset = byteOffset;
            this.datatype = datatype;
        }

        public MemberDefinition(ByteBuffer buffer) {
            this.name = readNullTerminatedString(buffer);
            this.byteOffset = buffer.getInt();
            this.datatype = DatatypeMessage.createFromBuffer(buffer);
        }

        public byte[] toHdfBytes() {
            byte[] nameBytes = name.getBytes();
            int paddedNameLength = ((nameBytes.length + 1 + 7) / 8) * 8; // Null-terminated and aligned to 8 bytes
            ByteBuffer buffer = ByteBuffer.allocate(paddedNameLength + 4 + datatype.toHdfBytes().length);
            buffer.put(nameBytes);
            buffer.put((byte) 0); // Null-terminator
            while (buffer.position() % 8 != 0) {
                buffer.put((byte) 0); // Padding
            }
            buffer.putInt(byteOffset);
            buffer.put(datatype.toHdfBytes());
            return buffer.array();
        }

        private int calculateSize() {
            int nameSize = ((name.length() + 1 + 7) / 8) * 8; // Null-terminated and aligned to 8 bytes
            return nameSize + 4 + datatype.toHdfBytes().length;
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

        @Override
        public String toString() {
            return "MemberDefinition{" +
                    "name='" + name + '\'' +
                    ", byteOffset=" + byteOffset +
                    ", datatype=" + datatype +
                    '}';
        }
    }
}
