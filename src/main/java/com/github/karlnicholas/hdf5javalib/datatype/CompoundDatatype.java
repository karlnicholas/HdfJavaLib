package com.github.karlnicholas.hdf5javalib.datatype;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static com.github.karlnicholas.hdf5javalib.datatype.FixedPointDatatype.parseFixedPointType;
import static com.github.karlnicholas.hdf5javalib.datatype.FloatingPointDatatype.parseFloatingPointType;
import static com.github.karlnicholas.hdf5javalib.datatype.StringDatatype.parseStringType;

@Getter
public class CompoundDatatype implements HdfDatatype {
    private final int numberOfMembers; // Number of members in the compound datatype
    private final int size;
    private List<HdfCompoundDatatypeMember> members;     // Member definitions

    // New application-level constructor
    public CompoundDatatype(int numberOfMembers, int size, List<HdfCompoundDatatypeMember> members) {
        this.numberOfMembers = numberOfMembers;
        this.size = size;
        this.members = new ArrayList<>(members); // Deep copy to avoid external modification
    }

    public CompoundDatatype(BitSet classBitField, int size, ByteBuffer buffer) {
        this.numberOfMembers = extractNumberOfMembersFromBitSet(classBitField);
        this.size = size;
        readFromByteBuffer(buffer);
    }

//    public CompoundDatatype(BitSet classBitField, int size, byte[] data) {
//        this.numberOfMembers = extractNumberOfMembersFromBitSet(classBitField);
//        this.size = size;
//        ByteBuffer cdtcBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
//        readFromByteBuffer(cdtcBuffer);
//    }

    private short extractNumberOfMembersFromBitSet(BitSet classBitField) {
        short value = 0;
        for (int i = 0; i < classBitField.length(); i++) {
            if (classBitField.get(i)) {
                value |= (short) (1 << i);
            }
        }
        return value;
    }

    private void readFromByteBuffer(ByteBuffer buffer) {
        this.members = new ArrayList<>();
//        buffer.position(8);
        for (int i = 0; i < numberOfMembers; i++) {
            buffer.mark();
            String name = readNullTerminatedString(buffer);

            // Align to 8-byte boundary
            alignBufferTo8ByteBoundary(buffer, name.length() + 1);

            int offset = buffer.getInt();
            int dimensionality = Byte.toUnsignedInt(buffer.get());
            buffer.position(buffer.position() + 3); // Skip reserved bytes
            int dimensionPermutation = buffer.getInt();
            buffer.position(buffer.position() + 4); // Skip reserved bytes

            int[] dimensionSizes = new int[4];
            for (int j = 0; j < 4; j++) {
                dimensionSizes[j] = buffer.getInt();
            }

//            HdfDatatype type = parseMessageDataType(buffer, name);

//            String name = "get parsed";
            byte classAndVersion = buffer.get();
            byte version = (byte) ((classAndVersion >> 4) & 0x0F);
            byte dataTypeClass = (byte) (classAndVersion & 0x0F);

            byte[] classBits = new byte[3];
            buffer.get(classBits);
            BitSet classBitField = BitSet.valueOf(new long[]{
                    ((long) classBits[2] & 0xFF) << 16 | ((long) classBits[1] & 0xFF) << 8 | ((long) classBits[0] & 0xFF)
            });

            int size = buffer.getInt();
            HdfDatatype hdfDatatype = parseCompoundDataType(version, dataTypeClass, classBitField, size, buffer);
            HdfCompoundDatatypeMember hdfCompoundDatatypeMember = new HdfCompoundDatatypeMember(name, offset, dimensionality, dimensionPermutation, dimensionSizes, hdfDatatype);

            members.add(hdfCompoundDatatypeMember);
        }
    }

    public static HdfDatatype parseCompoundDataType(byte version, byte dataTypeClass, BitSet classBitField, int size, ByteBuffer buffer) {
         return switch (dataTypeClass) {
            case 0 -> parseFixedPointType(version, classBitField, size, buffer);
            case 1 -> parseFloatingPointType(version, classBitField, size, buffer );
            case 3 -> parseStringType(version, classBitField, size);
    //            case 6 -> parseCompoundDataType(version, size, classBitField, name, buffer);
            default -> throw new UnsupportedOperationException("Unsupported datatype class: " + dataTypeClass);
        };
    }

    private static String readNullTerminatedString(ByteBuffer buffer) {
        StringBuilder nameBuilder = new StringBuilder();
        byte b;
        while ((b = buffer.get()) != 0) {
            nameBuilder.append((char) b);
        }
        return nameBuilder.toString();
    }

    private static void alignBufferTo8ByteBoundary(ByteBuffer buffer, int dataLength) {
        int padding = (8 - (dataLength % 8)) % 8;
        buffer.position(buffer.position() + padding);
    }


    @Override
    public void writeDefinitionToByteBuffer(ByteBuffer buffer) {
        for (HdfCompoundDatatypeMember member: members) {
            buffer.put(member.getName().getBytes(StandardCharsets.US_ASCII));
            buffer.put((byte)0);
            int paddingSize = (8 -  ((member.getName().length()+1)% 8)) % 8;
            buffer.put(new byte[paddingSize]);
            buffer.putInt(member.getOffset());
            buffer.put((byte)member.dimensionality);
            buffer.put(new byte[3]);
            buffer.putInt(member.dimensionPermutation);
            buffer.put(new byte[4]);
            for( int ds: member.dimensionSizes) {
                buffer.putInt(ds);
            }
            member.type.writeDefinitionToByteBuffer(buffer);

        }
    }

    @Override
    public HdfDatatype.DatatypeClass getDatatypeClass() {
        return DatatypeClass.COMPOUND;
    }

    @Override
    public BitSet getClassBitBytes() {
        return new BitSet();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CompoundDatatype {")
                .append(" numberOfMembers: ").append(numberOfMembers)
                .append(", size: ").append(size)
                .append(", ");
        members.forEach(member->{
            builder.append("\r\n\t");
            builder.append(member);
        });
        return builder.toString();
    }

    @Override
    public short getSizeMessageData() {
        short size = 0;
        for(HdfCompoundDatatypeMember member: members) {
            size += member.getType().getSizeMessageData();
        }
        return size;
    }

}
