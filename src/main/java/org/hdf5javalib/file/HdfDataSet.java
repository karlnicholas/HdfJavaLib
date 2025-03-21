package org.hdf5javalib.file;

import lombok.Getter;
import org.hdf5javalib.dataclass.HdfFixedPoint;
import org.hdf5javalib.dataclass.HdfString;
import org.hdf5javalib.file.dataobject.HdfObjectHeaderPrefixV1;
import org.hdf5javalib.file.dataobject.message.*;
import org.hdf5javalib.file.dataobject.message.datatype.HdfDatatype;
import org.hdf5javalib.file.dataobject.message.datatype.StringDatatype;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Getter
public class HdfDataSet {
    private final HdfGroup hdfGroup;
    private final String datasetName;
    private final HdfDatatype hdfDatatype;
    private final List<AttributeMessage> attributes;
    private final DataspaceMessage dataSpaceMessage;
    private long dataAddress;
    private HdfObjectHeaderPrefixV1 dataObjectHeaderPrefix;

    /*
     * So, this is a group ?
     * it has a name, "Demand" off the root group of "/"
     * it has a datatype, "Compound" , with dimensions, attributes, and an address in the HdfFile
     * It should have a localHeap and LocalHeap contents, perhaps.
     */

    public HdfDataSet(HdfGroup hdfGroup, String datasetName, HdfDatatype hdfDatatype, DataspaceMessage dataSpaceMessage) {
        this.hdfGroup = hdfGroup;
        this.datasetName = datasetName;
        this.hdfDatatype = hdfDatatype;
        this.attributes = new ArrayList<>();
        this.dataSpaceMessage = dataSpaceMessage;
        computeSpaceRequirements();
    }

    public void write(Supplier<ByteBuffer> bufferSupplier) throws IOException {
        dataAddress = hdfGroup.write(bufferSupplier, this);
    }

    public void write(ByteBuffer byteBuffer) throws IOException {
        dataAddress = hdfGroup.write(byteBuffer, this);
    }

    public AttributeMessage createAttribute(String name, DatatypeMessage dt, DataspaceMessage ds, HdfString value) {
        byte[] nameBytes = new byte[name.length()];
        System.arraycopy(name.getBytes(StandardCharsets.US_ASCII), 0, nameBytes, 0, name.length());
        short attributeMessageSize = 8;
        int nameSize = name.toString().length();
        nameSize = (short) ((nameSize + 7) & ~7);
        int datatypeSize = 8; // datatypeMessage.getSizeMessageData();
        int dataspaceSize = 8; // dataspaceMessage.getSizeMessageData();
        int valueSize = value != null ? value.toString().length() : 0;
        valueSize  = (short) ((valueSize + 7) & ~7);
        attributeMessageSize += nameSize + datatypeSize + dataspaceSize + valueSize;
        AttributeMessage attributeMessage = new AttributeMessage(1,
                new HdfString(nameBytes, new StringDatatype(StringDatatype.createClassAndVersion(), StringDatatype.createClassBitField(StringDatatype.PaddingType.NULL_TERMINATE, StringDatatype.CharacterSet.ASCII), name.length()+1)),
                dt, ds, value, (byte)0, attributeMessageSize);
        attributes.add(attributeMessage);
        computeSpaceRequirements();
        return attributeMessage;
    }

    private void computeSpaceRequirements() {
        int currentObjectHeaderSize = hdfGroup.getHdfFile().getBufferAllocation().getDataGroupStorageSize();
        List<HdfMessage> headerMessages = new ArrayList<>();
        headerMessages.add(dataSpaceMessage);

        short dataTypeMessageSize = 8;
        dataTypeMessageSize += hdfDatatype.getSizeMessageData();
        // to 8 byte boundary
        dataTypeMessageSize = (short) ((dataTypeMessageSize + 7) & ~7);
        DatatypeMessage dataTypeMessage = new DatatypeMessage(hdfDatatype, (byte)1, dataTypeMessageSize);
        headerMessages.add(dataTypeMessage);

        // Add FillValue message
        FillValueMessage fillValueMessage = new FillValueMessage(2, 2, 0, 1,
                0,
                new byte[0], (byte)1, (short)8);
        headerMessages.add(fillValueMessage);

        // Add DataLayoutMessage (Storage format)
        HdfFixedPoint[] dimensions = dataSpaceMessage.getDimensions();
//        long recordCount = dimensions[dimensions.length-1].toBigInteger().longValue();
        long dimensionSizes = hdfDatatype.getSize();
        for(HdfFixedPoint fixedPoint : dimensions) {
            dimensionSizes *= fixedPoint.getInstance(Long.class);
        }
        HdfFixedPoint[] hdfDimensionSizes = (HdfFixedPoint[]) Array.newInstance(HdfFixedPoint.class, 1);
        hdfDimensionSizes[0] = HdfFixedPoint.of(dimensionSizes);


        short dataLayoutMessageSize = (short) 8;
        switch (1) {
            case 0: // Compact Storage
                break;

            case 1: // Contiguous Storage
                dataLayoutMessageSize += 16;
                break;

            case 2: // Chunked Storage
                break;

            default:
                throw new IllegalArgumentException("Unsupported layout class: " + 1);
        }

        DataLayoutMessage dataLayoutMessage = new DataLayoutMessage(3, 1,
                HdfFixedPoint.of(hdfGroup.getHdfFile().getBufferAllocation().getDataAddress()),
                hdfDimensionSizes,
                0, null, HdfFixedPoint.undefined((short)8), (byte)0, dataLayoutMessageSize);
        headerMessages.add(dataLayoutMessage);

        // add ObjectModification Time message
        ObjectModificationTimeMessage objectModificationTimeMessage = new ObjectModificationTimeMessage(1, Instant.now().getEpochSecond(), (byte)0, (short)8);
        headerMessages.add(objectModificationTimeMessage);

        // attribute messages at the end
        headerMessages.addAll(attributes);

        int objectReferenceCount = 1;
        int objectHeaderSize = 0;
        for( HdfMessage headerMessage: headerMessages ) {
            objectHeaderSize += headerMessage.getSizeMessageData() + 8;
        }
        if ( objectHeaderSize + 8 > currentObjectHeaderSize) {
            currentObjectHeaderSize = hdfGroup.getHdfFile().getBufferAllocation().expandDataGroupStorageSize(objectHeaderSize);
        }
        // Test whether there is space enough for a NilMessage of 0 length
        if ( objectHeaderSize > currentObjectHeaderSize ) {
            // restructure the messages
            headerMessages.clear();
            ObjectHeaderContinuationMessage objectHeaderContinuationMessage = new ObjectHeaderContinuationMessage(HdfFixedPoint.of(0), HdfFixedPoint.of(0), (byte)0, (short)16);
            headerMessages.add(objectHeaderContinuationMessage);
            // NiLMessage is now 0 size because there is no extra space
            headerMessages.add(new NilMessage(0, (byte)0, (short)0));
            headerMessages.add(dataTypeMessage);
            headerMessages.add(fillValueMessage);
            headerMessages.add(dataLayoutMessage);
            headerMessages.add(objectModificationTimeMessage);
            headerMessages.add(dataSpaceMessage);
            headerMessages.addAll(attributes);

            objectHeaderSize = 0;
            int breakPostion = 6;
            for(int i=0; i < breakPostion; ++i) {
                objectHeaderSize += headerMessages.get(i).getSizeMessageData() + 8;
            }
            int continueSize = 0;
            while (breakPostion < headerMessages.size()) {
                continueSize += headerMessages.get(breakPostion).getSizeMessageData() + 8;
                breakPostion++;
            }

            hdfGroup.getHdfFile().getBufferAllocation().setDataGroupAndContinuationStorageSize(objectHeaderSize, continueSize);
            objectHeaderContinuationMessage.setContinuationOffset(HdfFixedPoint.of(hdfGroup.getHdfFile().getBufferAllocation().getMessageContinuationAddress()));
            objectHeaderContinuationMessage.setContinuationSize(HdfFixedPoint.of(continueSize));
            // set the object header size.
        } else if (objectHeaderSize + 8 < currentObjectHeaderSize) {
            // add remaining space
            headerMessages.add(new NilMessage(currentObjectHeaderSize - 8 - objectHeaderSize, (byte)0, (short)(currentObjectHeaderSize - 8 - objectHeaderSize)));
        }
        // redo addresses already set.
        dataLayoutMessage.setDataAddress(HdfFixedPoint.of(hdfGroup.getHdfFile().getBufferAllocation().getDataAddress()));
        this.dataObjectHeaderPrefix = new HdfObjectHeaderPrefixV1(1, headerMessages.size(), objectReferenceCount, Math.max(objectHeaderSize, currentObjectHeaderSize), headerMessages);
        hdfGroup.getHdfFile().recomputeBufferAllocation(this);
    }
    public void close() {
    }
    public void writeToBuffer(ByteBuffer buffer) {
        dataObjectHeaderPrefix.writeToByteBuffer(buffer);
    }

}
