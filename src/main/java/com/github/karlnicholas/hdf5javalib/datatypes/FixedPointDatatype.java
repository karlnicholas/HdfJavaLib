package com.github.karlnicholas.hdf5javalib.datatypes;

import java.nio.ByteBuffer;

public class FixedPointDatatype extends DatatypeMessage {
    private final boolean littleEndian;
    private final boolean signed;

    public FixedPointDatatype(int version, int size, ByteBuffer buffer) {
        super(version, 0, size);
        byte flags = buffer.get();
        this.littleEndian = (flags & 0x01) == 0;
        this.signed = (flags & 0x08) != 0;
    }

    @Override
    public byte[] toHdfBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put((byte) ((version << 4) | typeClass));
        buffer.put((byte) ((littleEndian ? 0 : 1) | (signed ? 0x08 : 0)));
        buffer.putInt(size);
        return buffer.array();
    }

    @Override
    public String toString() {
        return String.format("FixedPointDatatype{version=%d, size=%d, littleEndian=%b, signed=%b}",
                version, size, littleEndian, signed);
    }
}
