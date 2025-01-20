package com.github.karlnicholas.hdf5javalib.messages;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class CompoundDataTypeClass implements HdfMessage {
    private final int numberOfMembers; // Number of members in the compound datatype
    private List<Member> members;     // Member definitions

    public CompoundDataTypeClass(BitSet classBitField) {
        // Correctly extract the number of members from the BitSet
        // For example: Bits 0-7 represent the number of members (modify as per HDF5 spec)
        this.numberOfMembers = extractNumberOfMembersFromBitSet(classBitField);
    }

    private int extractNumberOfMembersFromBitSet(BitSet classBitField) {
        // Convert BitSet to an integer representation
        int value = 0;
        for (int i = 0; i < classBitField.length(); i++) {
            if (classBitField.get(i)) {
                value |= (1 << i);
            }
        }
        return value;
    }

    public HdfMessage parseHeaderMessage(ByteBuffer buffer) {
        // Initialize the list of members
        this.members = new ArrayList<>();

        // Parse each member
        for (int i = 0; i < numberOfMembers; i++) {
            // Parse Member Name (null-terminated ASCII string)
            StringBuilder nameBuilder = new StringBuilder();
            byte b;
            while ((b = buffer.get()) != 0) {
                nameBuilder.append((char) b);
            }
            String name = nameBuilder.toString();

            // Align buffer position to the next 8-byte boundary
            int stringLength = nameBuilder.length() + 1; // Include the null terminator
            int padding = (8 - (stringLength % 8)) % 8; // Calculate padding needed
            buffer.position(buffer.position() + padding); // Adjust buffer position

            // Parse Byte Offset of Member (4 bytes, little-endian)
            long offset = Integer.toUnsignedLong(buffer.getInt());

            // Parse Dimensionality (1 byte)
            int dimensionality = Byte.toUnsignedInt(buffer.get());

            // Parse Reserved (3 bytes, must be zero)
            byte[] reserved1 = new byte[3];
            buffer.get(reserved1);

            // Parse Dimension Permutation (4 bytes)
            int dimensionPermutation = buffer.getInt();

            // Parse Reserved (4 bytes, must be zero)
            byte[] reserved2 = new byte[4];
            buffer.get(reserved2);

            // Parse Dimension Sizes (4 required dimensions, 4 bytes each)
            int[] dimensionSizes = new int[4];
            for (int j = 0; j < 4; j++) {
                dimensionSizes[j] = buffer.getInt();
            }

            // Parse the member's datatype (example stub, replace with actual parsing logic)
            Object type = parseMemberDataType(buffer);

            // Add the parsed member to the list
            members.add(new Member(name, offset, dimensionality, dimensionPermutation, dimensionSizes, null));

            // Print the parsed details
            System.out.println("Member Name: " + name);
            System.out.println("Member Offset: " + offset);
            System.out.println("Dimensionality: " + dimensionality);
            System.out.println("Dimension Permutation: " + dimensionPermutation);
            System.out.println("Dimension Sizes: " + java.util.Arrays.toString(dimensionSizes));
        }

        return this;
    }

    private Object parseMemberDataType(ByteBuffer buffer) {
        int version;              // Version of the datatype message
        int dataTypeClass;        // Datatype class
        BitSet classBitField;     // Class Bit Field (24 bits)
        long size;                // Size of the datatype element

        // Parse Version and Datatype Class (packed into a single byte)
        byte classAndVersion = buffer.get();
        version = (classAndVersion >> 4) & 0x0F; // Top 4 bits
        dataTypeClass = classAndVersion & 0x0F;  // Bottom 4 bits

        // Parse Class Bit Field (24 bits)
        byte[] classBits = new byte[3];
        buffer.get(classBits);
        classBitField = BitSet.valueOf(new long[]{
                ((long) classBits[2] & 0xFF) << 16 | ((long) classBits[1] & 0xFF) << 8 | ((long) classBits[0] & 0xFF)
        });

        // Parse Size (unsigned 4 bytes)
        size = Integer.toUnsignedLong(buffer.getInt());

        // Handle the datatype class according to the HDF5 spec
        switch (dataTypeClass) {
            case 0: // Fixed-point
                return parseFixedPoint(buffer, size, version, classBitField);
            case 1: // Floating-point
                return parseFloatingPoint(buffer, size, version, classBitField);
            case 3: // String
                return parseString(buffer, size, version, classBitField);
            default:
                throw new UnsupportedOperationException("Unsupported datatype class: " + dataTypeClass);
        }
    }

    private FixedPointMember parseFixedPoint(ByteBuffer buffer, long size, int version, BitSet classBitField) {
        // Extract details from the bitfield
        boolean loPad = classBitField.get(1);          // Bit 1: lo_pad
        boolean hiPad = classBitField.get(2);          // Bit 2: hi_pad
        boolean signed = classBitField.get(3);         // Bit 3: signed (2's complement form if true)
        boolean bigEndian = classBitField.get(0);      // Byte order: 0 = Little-endian, 1 = Big-endian

        // Parse the next two shorts: Bit Offset and Bit Precision
        int bitOffset = Short.toUnsignedInt(buffer.getShort());
        int bitPrecision = Short.toUnsignedInt(buffer.getShort());

        // Debug information
        System.out.println("FixedPointMember - Version: " + version);
        System.out.println("BigEndian: " + bigEndian);
        System.out.println("LoPad: " + loPad);
        System.out.println("HiPad: " + hiPad);
        System.out.println("Signed: " + signed);
        System.out.println("BitOffset: " + bitOffset);
        System.out.println("BitPrecision: " + bitPrecision);

        // Return a FixedPointMember with the parsed details
        return new FixedPointMember(size, bigEndian, loPad, hiPad, signed, bitOffset, bitPrecision);
    }

    private FloatingPointMember parseFloatingPoint(ByteBuffer buffer, long size, int version, BitSet classBitField) {
        int exponentBits = buffer.getInt(); // Read exponent bits
        int mantissaBits = buffer.getInt(); // Read mantissa bits

        // Example use of version and classBitField (update as needed based on spec)
        boolean isBigEndian = classBitField.get(0); // Example: Check if data is big-endian
        System.out.println("FloatingPointMember - Version: " + version + ", BigEndian: " + isBigEndian);

        return new FloatingPointMember(size, exponentBits, mantissaBits, isBigEndian);
    }

    private StringMember parseString(ByteBuffer buffer, long size, int version, BitSet classBitField) {
        // Extract the padding type (bits 0–3)
        int paddingType = 0;
        for (int i = 0; i <= 3; i++) {
            if (classBitField.get(i)) {
                paddingType |= (1 << i);
            }
        }

        // Interpret the padding type based on the HDF5 spec
        String paddingDescription;
        switch (paddingType) {
            case 0:
                paddingDescription = "Null Terminate";
                break;
            case 1:
                paddingDescription = "Null Pad";
                break;
            case 2:
                paddingDescription = "Space Pad";
                break;
            default:
                paddingDescription = "Reserved";
                break;
        }

        // Extract the character set encoding (bits 4–7)
        int charSet = 0;
        for (int i = 4; i <= 7; i++) {
            if (classBitField.get(i)) {
                charSet |= (1 << (i - 4));
            }
        }

        // Interpret the character set encoding based on the HDF5 spec
        String charSetDescription;
        switch (charSet) {
            case 0:
                charSetDescription = "ASCII";
                break;
            case 1:
                charSetDescription = "UTF-8";
                break;
            default:
                charSetDescription = "Reserved";
                break;
        }

        // Example debug print of extracted values
        System.out.println("StringMember - Version: " + version);
        System.out.println("PaddingType: " + paddingType + " (" + paddingDescription + ")");
        System.out.println("CharacterSet: " + charSet + " (" + charSetDescription + ")");

        // Create and return the StringMember object
        return new StringMember(size, paddingType, paddingDescription, charSet, charSetDescription);
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
        private final String name;              // Member name
        private final long offset;              // Member offset
        private final int dimensionality;       // Dimensionality
        private final int dimensionPermutation; // Dimension Permutation
        private final int[] dimensionSizes;     // Dimension Sizes
        private final byte[] typeMessageBytes;  // Member Type Message bytes

        public Member(String name, long offset, int dimensionality, int dimensionPermutation, int[] dimensionSizes, byte[] typeMessageBytes) {
            this.name = name;
            this.offset = offset;
            this.dimensionality = dimensionality;
            this.dimensionPermutation = dimensionPermutation;
            this.dimensionSizes = dimensionSizes;
            this.typeMessageBytes = typeMessageBytes;
        }

        @Override
        public String toString() {
            return "Member{" +
                    "name='" + name + '\'' +
                    ", offset=" + offset +
                    ", dimensionality=" + dimensionality +
                    ", dimensionPermutation=" + dimensionPermutation +
                    ", dimensionSizes=" + java.util.Arrays.toString(dimensionSizes) +
//                    ", typeMessageBytes=" + bytesToHex(typeMessageBytes) +
                    '}';
        }

        private String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        }
    }
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static class FixedPointMember {
        private final long size;
        private final boolean bigEndian;
        private final boolean loPad;
        private final boolean hiPad;
        private final boolean signed;
        private final int bitOffset;
        private final int bitPrecision;

        public FixedPointMember(long size, boolean bigEndian, boolean loPad, boolean hiPad, boolean signed, int bitOffset, int bitPrecision) {
            this.size = size;
            this.bigEndian = bigEndian;
            this.loPad = loPad;
            this.hiPad = hiPad;
            this.signed = signed;
            this.bitOffset = bitOffset;
            this.bitPrecision = bitPrecision;
        }

        @Override
        public String toString() {
            return "FixedPointMember{" +
                    "size=" + size +
                    ", bigEndian=" + bigEndian +
                    ", loPad=" + loPad +
                    ", hiPad=" + hiPad +
                    ", signed=" + signed +
                    ", bitOffset=" + bitOffset +
                    ", bitPrecision=" + bitPrecision +
                    '}';
        }
    }

    private static class FloatingPointMember {
        private final long size;
        private final int exponentBits;
        private final int mantissaBits;
        private final boolean bigEndian; // Example use of classBitField

        public FloatingPointMember(long size, int exponentBits, int mantissaBits, boolean bigEndian) {
            this.size = size;
            this.exponentBits = exponentBits;
            this.mantissaBits = mantissaBits;
            this.bigEndian = bigEndian;
        }

        @Override
        public String toString() {
            return "FloatingPointMember{size=" + size + ", exponentBits=" + exponentBits + ", mantissaBits=" + mantissaBits + ", bigEndian=" + bigEndian + "}";
        }
    }

    private static class StringMember {
        private final long size;
        private final int paddingType;
        private final String paddingDescription;
        private final int charSet;
        private final String charSetDescription;

        public StringMember(long size, int paddingType, String paddingDescription, int charSet, String charSetDescription) {
            this.size = size;
            this.paddingType = paddingType;
            this.paddingDescription = paddingDescription;
            this.charSet = charSet;
            this.charSetDescription = charSetDescription;
        }

        @Override
        public String toString() {
            return "StringMember{" +
                    "size=" + size +
                    ", paddingType=" + paddingType +
                    ", paddingDescription='" + paddingDescription + '\'' +
                    ", charSet=" + charSet +
                    ", charSetDescription='" + charSetDescription + '\'' +
                    '}';
        }
    }

}
