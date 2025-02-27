package com.github.karlnicholas.hdf5javalib.message;

import com.github.karlnicholas.hdf5javalib.data.HdfData;
import com.github.karlnicholas.hdf5javalib.data.HdfString;
import com.github.karlnicholas.hdf5javalib.datatype.HdfDatatype;
import com.github.karlnicholas.hdf5javalib.datatype.StringDatatype;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;

import static com.github.karlnicholas.hdf5javalib.utils.HdfParseUtils.createMessageInstance;

@Getter
public class AttributeMessage extends HdfMessage {
    private final int version;
    private final HdfString name;
    private final DatatypeMessage datatypeMessage;
    private final DataspaceMessage dataspaceMessage;
    private final HdfData value;

    public AttributeMessage(int version, HdfString name, DatatypeMessage datatypeMessage, DataspaceMessage dataspaceMessage, HdfData value) {
        super(MessageType.AttributeMessage, ()-> {
            short s = 8;
            int nameSize = name.getSizeMessageData();
            nameSize = (short) ((nameSize + 7) & ~7);
            int datatypeSize = 8; // datatypeMessage.getSizeMessageData();
            int dataspaceSize = 8; // dataspaceMessage.getSizeMessageData();
            int valueSize = value != null ? value.getSizeMessageData() : 0;
            valueSize  = (short) ((valueSize + 7) & ~7);
            s += nameSize + datatypeSize + dataspaceSize + valueSize;
            return s;
        }, (byte)0);
        this.version = version;
        this.datatypeMessage = datatypeMessage;
        this.dataspaceMessage = dataspaceMessage;
        this.name = name;
        this.value = value;
    }

    public static HdfMessage parseHeaderMessage(byte ignoredFlags, byte[] data, short offsetSize, short lengthSize) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        // Read the version (1 byte)
        int version = Byte.toUnsignedInt(buffer.get());

        // Skip the reserved byte (1 byte, should be zero)
        buffer.get();

        // Read the sizes of name, datatype, and dataspace (2 bytes each)
        int nameSize = Short.toUnsignedInt(buffer.getShort());
        int datatypeSize = Short.toUnsignedInt(buffer.getShort());
        int dataspaceSize = Short.toUnsignedInt(buffer.getShort());

        // Read the name (variable size)
        byte[] nameBytes = new byte[nameSize];
        buffer.get(nameBytes);
        BitSet bitSet = StringDatatype.getStringTypeBitSet(StringDatatype.PaddingType.NULL_TERMINATE, StringDatatype.CharacterSet.ASCII);
        HdfString name = new HdfString(nameBytes, bitSet);
        // get padding bytes
        int padding = (8 - (nameSize % 8)) % 8;
        byte[] paddingBytes = new byte[padding];
        buffer.get(paddingBytes);

        byte[] dtBytes = new byte[datatypeSize];
        buffer.get(dtBytes);

        byte[] dsBytes = new byte[dataspaceSize];
        buffer.get(dsBytes);

        HdfMessage hdfDataObjectHeaderDt = createMessageInstance(MessageType.DatatypeMessage, (byte) 0, dtBytes, offsetSize, lengthSize, ()-> Arrays.copyOfRange( data, buffer.position(), data.length));
        DatatypeMessage dt = (DatatypeMessage) hdfDataObjectHeaderDt;
        HdfMessage hdfDataObjectHeaderDs = createMessageInstance(MessageType.DataspaceMessage, (byte) 0, dsBytes, offsetSize, lengthSize, null);
        DataspaceMessage ds = (DataspaceMessage) hdfDataObjectHeaderDs;

        HdfData value = null;
        if ( dt.getHdfDatatype().getDatatypeClass() == HdfDatatype.DatatypeClass.STRING ) {
            int dtDataSize = dt.getHdfDatatype().getSize();
            byte[] dataBytes = new byte[dtDataSize];
            buffer.get(dataBytes);
            value = new HdfString(dataBytes, bitSet);
        }
        return new AttributeMessage(version, name, dt, ds, value);
    }

    @Override
    public String toString() {
        return "AttributeMessage{" +
                "version=" + version +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public void writeToByteBuffer(ByteBuffer buffer) {
        writeMessageData(buffer);
        buffer.put((byte) version);

        // Skip the reserved byte (1 byte, should be zero)
        buffer.put((byte) 0);

        // Write the sizes of name, datatype, and dataspace (2 bytes each)
        short nameSize = name.getSizeMessageData();
        buffer.putShort(nameSize);
//        buffer.putShort(datatypeMessage.getSizeMessageData());
//        buffer.putShort(dataspaceMessage.getSizeMessageData());
        buffer.putShort((short) 8);
        buffer.putShort((short) 8);

        // Read the name (variable size)
        buffer.put(name.getBytes());

        // padding bytes
        buffer.put(new byte[(8 - (nameSize % 8)) % 8]);

        DatatypeMessage.writeInfoToByteBuffer(datatypeMessage, buffer);

        DataspaceMessage.writeInfoToByteBuffer(dataspaceMessage, buffer);

        // not right
        value.writeValueToByteBuffer(buffer);

    }

//    public void write(HdfFixedPoint attrType, String attributeValue) {
//        value = new HdfString(attributeValue);
//        int size = super.getSizeMessageData() + value.getSizeMessageData();
//        size = (short) ((size + 7) & ~7);
//        super.setSizeMessageData((short) size);
//    }
}
