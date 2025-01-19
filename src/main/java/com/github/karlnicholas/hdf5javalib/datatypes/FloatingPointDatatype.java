package com.github.karlnicholas.hdf5javalib.datatypes;

import java.nio.ByteBuffer;

public class FloatingPointDatatype extends DatatypeMessage {
    private final boolean littleEndian;
    private final int signBitPosition;
    private final int exponentSize;
    private final int mantissaSize;

    public FloatingPointDatatype(int version, int size, ByteBuffer buffer) {
        super(version, 1, size);
        byte flags = buffer.get();
        this.littleEndian = (flags & 0x01) == 0;
        this.signBitPosition = buffer.get();
        this.exponentSize = buffer.get();
        this.mantissaSize = buffer.get();
    }

    @Override
    public byte[] toHdfBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put((byte) ((version << 4) | typeClass));
        buffer.put((byte) (littleEndian ? 0 : 1));
        buffer.putInt(size);
        buffer.put((byte) signBitPosition);
        buffer.put((byte) exponentSize);
        buffer.put((byte) mantissaSize);
        return buffer.array();
    }

    @Override
    public String toString() {
        return String.format("FloatingPointDatatype{version=%d, size=%d, littleEndian=%b, signBitPosition=%d, exponentSize=%d, mantissaSize=%d}",
                version, size, littleEndian, signBitPosition, exponentSize, mantissaSize);
    }
}
