package com.github.karlnicholas.hdf5javalib.messages;

import java.nio.ByteBuffer;

public class AttributeMessage implements HdfMessage {
    private int version;
    private String name;
    private int datatypeSize;
    private int dataspaceSize;

    @Override
    public HdfMessage parseHeaderMessage(ByteBuffer buffer) {
        this.version = Byte.toUnsignedInt(buffer.get());
        int nameLength = Short.toUnsignedInt(buffer.getShort());
        this.datatypeSize = Short.toUnsignedInt(buffer.getShort());
        this.dataspaceSize = Short.toUnsignedInt(buffer.getShort());

        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        this.name = new String(nameBytes);

        return this;
    }

    @Override
    public String toString() {
        return "AttributeMessage{" +
                "version=" + version +
                ", name='" + name + '\'' +
                ", datatypeSize=" + datatypeSize +
                ", dataspaceSize=" + dataspaceSize +
                '}';
    }
}
