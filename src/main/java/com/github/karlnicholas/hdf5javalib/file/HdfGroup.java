package com.github.karlnicholas.hdf5javalib.file;

import com.github.karlnicholas.hdf5javalib.dataclass.HdfFixedPoint;
import com.github.karlnicholas.hdf5javalib.dataclass.HdfString;
import com.github.karlnicholas.hdf5javalib.file.dataobject.message.datatype.HdfDatatype;
import com.github.karlnicholas.hdf5javalib.file.dataobject.message.datatype.StringDatatype;
import com.github.karlnicholas.hdf5javalib.file.dataobject.HdfObjectHeaderPrefixV1;
import com.github.karlnicholas.hdf5javalib.file.infrastructure.*;
import com.github.karlnicholas.hdf5javalib.file.dataobject.message.DataspaceMessage;
import com.github.karlnicholas.hdf5javalib.file.dataobject.message.SymbolTableMessage;
import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

@Getter
public class HdfGroup {
    private final HdfFile hdfFile;
    private final String name;
    private final HdfObjectHeaderPrefixV1 objectHeader;
    private final HdfBTreeV1 bTree;
    private final HdfLocalHeap localHeap;
    private final HdfLocalHeapContents localHeapContents;
    private final HdfGroupSymbolTableNode symbolTableNode;
    private HdfDataSet dataSet;

    public HdfGroup(
            HdfFile hdfFile,
            String name,
            HdfObjectHeaderPrefixV1 objectHeader,
            HdfBTreeV1 bTree,
            HdfLocalHeap localHeap,
            HdfLocalHeapContents localHeapContents,
            HdfGroupSymbolTableNode symbolTableNode
    ) {
        this.hdfFile = hdfFile;
        this.name = name;
        this.objectHeader = objectHeader;
        this.bTree = bTree;
        this.localHeap = localHeap;
        this.localHeapContents = localHeapContents;
        this.symbolTableNode = symbolTableNode;
    }

    public HdfGroup(HdfFile hdfFile, String name, long btreeAddress, long localHeapAddress) {
        this.hdfFile = hdfFile;
        this.name = name;
        int localHeapContentsSize;
        // Define the heap data size, why 88 I don't know.
        // Initialize the heapData array
        localHeapContentsSize = 88;
        byte[] heapData = new byte[localHeapContentsSize];
        heapData[0] = (byte)0x1;
        heapData[8] = (byte)localHeapContentsSize;

        localHeap = new HdfLocalHeap(HdfFixedPoint.of(localHeapContentsSize), HdfFixedPoint.of(hdfFile.getBufferAllocation().getLocalHeapContentsAddress()));
        localHeapContents = new HdfLocalHeapContents(heapData);
        localHeap.addToHeap(new HdfString(new byte[0], StringDatatype.getStringTypeBitSet(StringDatatype.PaddingType.NULL_PAD, StringDatatype.CharacterSet.ASCII)), localHeapContents);

        // Define a B-Tree for group indexing
        bTree = new HdfBTreeV1("TREE", 0, 0, 0,
                HdfFixedPoint.undefined((short)8),
                HdfFixedPoint.undefined((short)8));

        objectHeader = new HdfObjectHeaderPrefixV1(1, 1, 1, 24,
                Collections.singletonList(new SymbolTableMessage(
                        HdfFixedPoint.of(btreeAddress),
                        HdfFixedPoint.of(localHeapAddress))));

        // Define a root group
        symbolTableNode = new HdfGroupSymbolTableNode("SNOD", 1, 0, new ArrayList<>());
    }


    public HdfDataSet createDataSet(String datasetName, HdfDatatype hdfDatatype, DataspaceMessage dataSpaceMessage, long objectHeaderAddress) {
        HdfString hdfDatasetName = new HdfString(datasetName.getBytes(), StringDatatype.getStringTypeBitSet(StringDatatype.PaddingType.NULL_PAD, StringDatatype.CharacterSet.ASCII));
        // this poosibly changes addresses for anything after the dataGroupAddress, which includes the SNOD address.
        dataSet = new HdfDataSet(this, datasetName, hdfDatatype, dataSpaceMessage);
        int linkNameOffset = bTree.addGroup(hdfDatasetName, HdfFixedPoint.of(hdfFile.getBufferAllocation().getSnodAddress()),
                localHeap,
                localHeapContents);
        HdfSymbolTableEntry ste = new HdfSymbolTableEntry(
                HdfFixedPoint.of(linkNameOffset),
                HdfFixedPoint.of(objectHeaderAddress));
        symbolTableNode.addEntry(ste);
        return dataSet;
    }

    public void writeToBuffer(ByteBuffer buffer) {
        // Write Object Header at position found in rootGroupEntry
        int dataGroupAddress = hdfFile.getBufferAllocation().getObjectHeaderPrefixAddress();
        buffer.position(dataGroupAddress);
        objectHeader.writeToByteBuffer(buffer);

        //        System.out.println(objectHeader);
        long localHeapPosition = -1;
        long bTreePosition = -1;

        // Try getting the Local Heap Address from the Root Symbol Table Entry
        if (hdfFile.getBufferAllocation().getLocalHeapAddress() > 0) {
            localHeapPosition = hdfFile.getBufferAllocation().getLocalHeapAddress();
        }

        // If not found or invalid, fallback to Object Header's SymbolTableMessage
        Optional<SymbolTableMessage> symbolTableMessageOpt = objectHeader.findMessageByType(SymbolTableMessage.class);
        if (symbolTableMessageOpt.isPresent()) {
            SymbolTableMessage symbolTableMessage = symbolTableMessageOpt.get();

            // Retrieve Local Heap Address if still not found
            if (localHeapPosition == -1 && symbolTableMessage.getLocalHeapAddress() != null && !symbolTableMessage.getLocalHeapAddress().isUndefined()) {
                localHeapPosition = symbolTableMessage.getLocalHeapAddress().toBigInteger().longValue();
            }

            // Retrieve B-Tree Address
            if (symbolTableMessage.getBTreeAddress() != null && !symbolTableMessage.getBTreeAddress().isUndefined()) {
                bTreePosition = symbolTableMessage.getBTreeAddress().toBigInteger().longValue();
            }
        }

        // Validate B-Tree Position and write it
        if (bTreePosition != -1) {
            buffer.position((int) bTreePosition); // Move to the correct position
            bTree.writeToByteBuffer(buffer);
        } else {
            throw new IllegalStateException("No valid B-Tree position found.");
        }

        // Validate Local Heap Position and write it
        if (localHeapPosition != -1) {
            buffer.position((int) localHeapPosition); // Move to the correct position
            localHeap.writeToByteBuffer(buffer);
            buffer.position(localHeap.getDataSegmentAddress().toBigInteger().intValue());
            localHeapContents.writeToByteBuffer(buffer);
        } else {
            throw new IllegalStateException("No valid Local Heap position found.");
        }

        // need to writre the dataset
        if ( dataSet != null ) {
            buffer.position(hdfFile.getBufferAllocation().getDataGroupAddress());
            dataSet.writeToBuffer(buffer);
        }

        buffer.position(hdfFile.getBufferAllocation().getSnodAddress());
        symbolTableNode.writeToBuffer(buffer);

    }

    public long write(Supplier<ByteBuffer> bufferSupplier, HdfDataSet hdfDataSet) throws IOException {
        return hdfFile.write(bufferSupplier);
    }
    @Override
    public String toString() {
        return "HdfGroup{" +
                "name='" + name + '\'' +
                "\r\n\tobjectHeader=" + objectHeader +
                "\r\n\tbTree=" + bTree +
                "\r\n\tlocalHeap=" + localHeap +
                "\r\n\tlocalHeapContents=" + localHeapContents +
                "\r\n\tsymbolTableNode=" + symbolTableNode +
                (dataSet != null ? "\r\n\tdataSet=" + dataSet.getDataObjectHeaderPrefix() : "") +
                "}";
    }

}
