package com.github.karlnicholas.hdf5javalib.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AttributeMessage implements HdfMessage {
    private int version;
    private String name;
    private int datatypeSize;
    private int dataspaceSize;
    private byte[] datatype;
    private byte[] dataspace;
    private byte[] data;

    @Override
    public HdfMessage parseHeaderMessage(ByteBuffer buffer) {
        // Set to little-endian as required by the HDF5 specification
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Parse header fields
        this.version = Byte.toUnsignedInt(buffer.get()); // 1 byte
        buffer.get(); // Skip reserved byte
        int nameLength = Short.toUnsignedInt(buffer.getShort()); // 2 bytes
        this.datatypeSize = Short.toUnsignedInt(buffer.getShort()); // 2 bytes
        this.dataspaceSize = Short.toUnsignedInt(buffer.getShort()); // 2 bytes

        // Parse name
        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        this.name = new String(nameBytes);

        // Parse datatype
        this.datatype = new byte[datatypeSize];
        buffer.get(datatype);

        // Parse dataspace
        this.dataspace = new byte[dataspaceSize];
        buffer.get(dataspace);

        // Parse data (if remaining bytes exist)
        int remaining = buffer.remaining();
        if (remaining > 0) {
            this.data = new byte[remaining];
            buffer.get(data);
        } else {
            this.data = null; // No data stored
        }

        return this;
    }

    @Override
    public String toString() {
        return "AttributeMessage{" +
                "version=" + version +
                ", name='" + name + '\'' +
                ", datatypeSize=" + datatypeSize +
                ", dataspaceSize=" + dataspaceSize +
                ", datatype=" + (datatype != null ? datatype.length + " bytes" : "null") +
                ", dataspace=" + (dataspace != null ? dataspace.length + " bytes" : "null") +
                ", data=" + (data != null ? data.length + " bytes" : "null") +
                '}';
    }
}
