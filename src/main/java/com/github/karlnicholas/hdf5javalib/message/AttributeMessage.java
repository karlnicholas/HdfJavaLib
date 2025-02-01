package com.github.karlnicholas.hdf5javalib.message;

import com.github.karlnicholas.hdf5javalib.datatype.HdfDataType;
import com.github.karlnicholas.hdf5javalib.datatype.HdfString;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.github.karlnicholas.hdf5javalib.utils.HdfUtils.createMessageInstance;

public class AttributeMessage extends HdfMessage {
    private int version;
    private int nameSize;
    private int datatypeSize;
    private int dataspaceSize;
    private HdfString name;
    private HdfDataType value;

    public AttributeMessage(int version, int nameSize, int datatypeSize, int dataspaceSize, HdfString name, HdfDataType value) {
        super(12, ()->1+1+2+2+2+nameSize+(((((nameSize + 1) / 8) + 1) * 8) - nameSize)+datatypeSize+dataspaceSize+value.getSizeMessageData(), (byte)0);
        this.version = version;
        this.nameSize = nameSize;
        this.datatypeSize = datatypeSize;
        this.dataspaceSize = dataspaceSize;
        this.name = name;
        this.value = value;
    }

    public static HdfMessage parseHeaderMessage(byte flags, byte[] data, int offsetSize, int lengthSize) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        // Read the version (1 byte)
        int version = Byte.toUnsignedInt(buffer.get());;

        // Skip the reserved byte (1 byte, should be zero)
        buffer.get();

        // Read the sizes of name, datatype, and dataspace (2 bytes each)
        int nameSize = nameSize = Short.toUnsignedInt(buffer.getShort());
        int datatypeSize = datatypeSize = Short.toUnsignedInt(buffer.getShort());
        int dataspaceSize = dataspaceSize = Short.toUnsignedInt(buffer.getShort());

        // Read the name (variable size)
        byte[] nameBytes = new byte[nameSize];
        buffer.get(nameBytes);
        HdfString name = new HdfString(nameBytes, true, false);
        // get padding bytes
        int padding = ((((nameSize + 1) / 8) + 1) * 8) - nameSize;
        byte[] paddingBytes = new byte[padding];
        buffer.get(paddingBytes);

        byte[] dtBytes = new byte[datatypeSize];
        buffer.get(dtBytes);
        HdfMessage hdfDataObjectHeaderDt = createMessageInstance(3, (byte) 0, dtBytes, offsetSize, lengthSize);
        DataTypeMessage dt = (DataTypeMessage) hdfDataObjectHeaderDt;

        byte[] dsBytes = new byte[dataspaceSize];
        buffer.get(dsBytes);
        HdfMessage hdfDataObjectHeaderDs = createMessageInstance(1, (byte) 0, dsBytes, offsetSize, lengthSize);
        DataSpaceMessage ds = (DataSpaceMessage) hdfDataObjectHeaderDs;


        HdfDataType value = null;

        if ( dt.getDataTypeClass() == 3 ) {
            int dtDataSize = dt.getSize().getBigIntegerValue().intValue();
            byte[] dataBytes = new byte[dtDataSize];
            buffer.get(dataBytes);
            dt.addDataType(dataBytes);
            value = dt.getHdfDataType();
        }
        return new AttributeMessage(version, nameSize, datatypeSize, dataspaceSize, name, value);
    }

    @Override
    public String toString() {
        return "AttributeMessage{" +
                "version=" + version +
                ", nameSize=" + nameSize +
                ", datatypeSize=" + datatypeSize +
                ", dataspaceSize=" + dataspaceSize +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public void writeToByteBuffer(ByteBuffer buffer, int offsetSize) {

    }
}
