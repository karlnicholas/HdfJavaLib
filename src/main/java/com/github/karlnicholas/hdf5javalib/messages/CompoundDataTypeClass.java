package com.github.karlnicholas.hdf5javalib.messages;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CompoundDataTypeClass implements HdfMessage {
    private int numberOfMembers;  // Number of members in the compound datatype
    private List<Member> members; // Member definitions

    @Override
    public HdfMessage parseHeaderMessage(ByteBuffer buffer) {
        // Parse Number of Members (16 bits, little-endian)
        this.numberOfMembers = Short.toUnsignedInt(buffer.getShort());

        // Initialize the list of members
        this.members = new ArrayList<>();

        // Parse each member
        for (int i = 0; i < numberOfMembers; i++) {
            // Parse member name (null-terminated string)
            StringBuilder nameBuilder = new StringBuilder();
            byte b;
            while ((b = buffer.get()) != 0) { // Null-terminated ASCII string
                nameBuilder.append((char) b);
            }
            String name = nameBuilder.toString();

            // Parse member datatype (delegating to DataTypeMessage or similar)
            DataTypeMessage memberType = new DataTypeMessage();
            memberType.parseHeaderMessage(buffer);

            // Parse member offset (unsigned 4 bytes)
            long offset = Integer.toUnsignedLong(buffer.getInt());

            // Add the member to the list
            members.add(new Member(name, memberType, offset));
        }

        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CompoundDataTypeClass{");
        sb.append("numberOfMembers=").append(numberOfMembers);
        sb.append(", members=[");
        for (Member member : members) {
            sb.append("\n    ").append(member);
        }
        sb.append("\n]}");
        return sb.toString();
    }

    private static class Member {
        private final String name;           // Member name
        private final DataTypeMessage type;  // Member type
        private final long offset;           // Member offset

        public Member(String name, DataTypeMessage type, long offset) {
            this.name = name;
            this.type = type;
            this.offset = offset;
        }

        @Override
        public String toString() {
            return "Member{name='" + name + "', type=" + type + ", offset=" + offset + "}";
        }
    }
}
