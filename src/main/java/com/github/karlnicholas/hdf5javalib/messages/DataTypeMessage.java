package com.github.karlnicholas.hdf5javalib.messages;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class DataTypeMessage implements HdfMessage {
    private int version;                 // Version of the datatype message
    private int dataTypeClass;           // Datatype class
    private BitSet classBitField;        // Class Bit Field (24 bits)
    private HdfFixedPoint size;          // Size of the datatype element
    private CompoundDataTypeClass compoundDataTypeClass; // Compound type (if applicable)

    @Override
    public HdfMessage parseHeaderMessage(ByteBuffer buffer) {
        // Parse Version and Datatype Class (packed into a single byte)
        byte classAndVersion = buffer.get();
        this.version = (classAndVersion >> 4) & 0x0F; // Top 4 bits
        this.dataTypeClass = classAndVersion & 0x0F;  // Bottom 4 bits

        // Parse Class Bit Field (24 bits)
        byte[] classBits = new byte[3];
        buffer.get(classBits);
        this.classBitField = BitSet.valueOf(new long[]{
                ((long) classBits[2] & 0xFF) << 16 | ((long) classBits[1] & 0xFF) << 8 | ((long) classBits[0] & 0xFF)
        });

        // Parse Size (unsigned 4 bytes)
        this.size = new HdfFixedPoint(buffer, 32, false);

        // Check if the datatype is Compound
        if (dataTypeClass == 6) { // Compound datatype
            this.compoundDataTypeClass = new CompoundDataTypeClass(classBitField);
            this.compoundDataTypeClass.parseHeaderMessage(buffer);
        } else {
            // For other datatype classes, parsing logic will be added later
            throw new UnsupportedOperationException("Datatype class " + dataTypeClass + " not yet implemented.");
        }

        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DataTypeMessage{");
        sb.append("version=").append(version);
        sb.append(", dataTypeClass=").append(dataTypeClass).append(" (").append(dataTypeClassToString(dataTypeClass)).append(")");
        sb.append(", classBitField=").append(bitSetToString(classBitField, 24));
        sb.append(", size=").append(size.getBigIntegerValue());
        if (compoundDataTypeClass != null) {
            sb.append(", compoundDataTypeClass=").append(compoundDataTypeClass);
        }
        sb.append('}');
        return sb.toString();
    }

    private String dataTypeClassToString(int dataTypeClass) {
        switch (dataTypeClass) {
            case 0: return "Fixed-Point";
            case 1: return "Floating-Point";
            case 2: return "Time";
            case 3: return "String";
            case 4: return "Bit Field";
            case 5: return "Opaque";
            case 6: return "Compound";
            case 7: return "Reference";
            case 8: return "Enumerated";
            case 9: return "Variable-Length";
            case 10: return "Array";
            default: return "Unknown";
        }
    }

    private String bitSetToString(BitSet bitSet, int numBits) {
        StringBuilder bits = new StringBuilder();
        for (int i = numBits - 1; i >= 0; i--) {
            bits.append(bitSet.get(i) ? "1" : "0");
        }
        return bits.toString();
    }
}
