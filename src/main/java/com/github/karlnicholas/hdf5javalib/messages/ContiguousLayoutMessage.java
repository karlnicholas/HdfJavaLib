package com.github.karlnicholas.hdf5javalib.messages;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

import java.nio.ByteBuffer;

public class ContiguousLayoutMessage implements HdfMessage {
    private int version;
    private HdfFixedPoint address;
    private HdfFixedPoint size;

    @Override
    public HdfMessage parseHeaderMessage(ByteBuffer buffer) {
        this.version = Byte.toUnsignedInt(buffer.get());
        this.address = new HdfFixedPoint(buffer, 64, false);
        this.size = new HdfFixedPoint(buffer, 64, false);
        return this;
    }

    @Override
    public String toString() {
        return "ContiguousLayoutMessage{" +
                "version=" + version +
                ", address=" + (address != null ? address.getBigIntegerValue() : "null") +
                ", size=" + (size != null ? size.getBigIntegerValue() : "null") +
                '}';
    }
}
