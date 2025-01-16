package com.github.karlnicholas.hdf5javalib.datatype;

import java.nio.ByteBuffer;

public abstract class DatatypeMessage {
    protected final int version;
    protected final int typeClass;
    protected final int size;

    public DatatypeMessage(int version, int typeClass, int size) {
        this.version = version;
        this.typeClass = typeClass;
        this.size = size;
    }

    public int getVersion() {
        return version;
    }

    public int getTypeClass() {
        return typeClass;
    }

    public int getSize() {
        return size;
    }

    public abstract byte[] toHdfBytes();

    @Override
    public String toString() {
        return String.format("DatatypeMessage{version=%d, typeClass=%d, size=%d}", version, typeClass, size);
    }

    public static DatatypeMessage createFromBuffer(ByteBuffer buffer) {
        int typeClassAndVersion = buffer.get() & 0xFF;
        int typeClass = typeClassAndVersion & 0x0F;
        int version = (typeClassAndVersion >> 4) & 0x0F;
        int size = buffer.getInt();

        switch (typeClass) {
            case 0: return new FixedPointDatatype(version, size, buffer);
            case 1: return new FloatingPointDatatype(version, size, buffer);
            case 3: return new StringDatatype(version, size, buffer);
            // Add more cases for other type classes if needed
            default: throw new IllegalArgumentException("Unsupported type class: " + typeClass);
        }
    }
}
