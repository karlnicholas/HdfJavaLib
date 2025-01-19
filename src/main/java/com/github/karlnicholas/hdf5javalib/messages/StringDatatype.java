package com.github.karlnicholas.hdf5javalib.messages;

import java.nio.ByteBuffer;

public class StringDatatype extends DatatypeMessage {
    private final int paddingType;
    private final boolean asciiEncoding;

    public StringDatatype(int version, int size, ByteBuffer buffer) {
        super(version, 3, size);
        byte flags = buffer.get();
        this.paddingType = flags & 0x0F;
        this.asciiEncoding = (flags & 0x10) == 0;
    }

    @Override
    public byte[] toHdfBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put((byte) ((version << 4) | typeClass));
        buffer.put((byte) ((asciiEncoding ? 0 : 0x10) | paddingType));
        buffer.putInt(size);
        return buffer.array();
    }

    @Override
    public String toString() {
        return String.format("StringDatatype{version=%d, size=%d, paddingType=%d, asciiEncoding=%b}",
                version, size, paddingType, asciiEncoding);
    }
}
