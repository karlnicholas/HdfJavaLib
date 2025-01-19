package com.github.karlnicholas.hdf5javalib.datatypes;

import java.nio.ByteBuffer;

public class HdfDatatypeParser {
    public static HdfCompoundDatatype parseCompoundDatatype(ByteBuffer buffer) {
        // Read the compound datatype's total size (adjust as per HDF5 specification)
        int compoundSize = buffer.getInt();

        HdfCompoundDatatype compound = new HdfCompoundDatatype(compoundSize);

        // Read the number of fields
        int numberOfFields = buffer.getInt();

        for (int i = 0; i < numberOfFields; i++) {
            // Read field name
            String fieldName = readNullTerminatedString(buffer);

            // Parse datatype (e.g., fixed-point, floating-point, etc.)
            int fieldType = buffer.get();
            HdfDatatype datatype = parseDatatype(buffer, fieldType);

            // Read field offset
            int fieldOffset = buffer.getInt();

            // Add field to the compound datatype
            compound.addComponent(fieldName, datatype, fieldOffset);
        }

        return compound;
    }

    private static String readNullTerminatedString(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        while (buffer.hasRemaining()) {
            char c = (char) buffer.get();
            if (c == '\0') break;
            sb.append(c);
        }
        return sb.toString();
    }

    private static HdfDatatype parseDatatype(ByteBuffer buffer, int fieldType) {
        switch (fieldType) {
            case 1: // Fixed-point
                return new HdfFixedPoint(buffer.getInt(), false);
            case 2: // Floating-point
                return new HdfFloatingPoint(buffer.getInt());
            case 3: // String
                return new HdfString(buffer.getInt());
            default:
                throw new IllegalArgumentException("Unsupported field type: " + fieldType);
        }
    }
}
