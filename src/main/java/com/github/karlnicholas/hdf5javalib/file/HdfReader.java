package com.github.karlnicholas.hdf5javalib.file;

import com.github.karlnicholas.hdf5javalib.datatype.HdfFixedPoint;
import com.github.karlnicholas.hdf5javalib.file.dataobject.HdfObjectHeaderPrefixV1;
import com.github.karlnicholas.hdf5javalib.datatype.CompoundDataType;
import com.github.karlnicholas.hdf5javalib.file.infrastructure.*;
import com.github.karlnicholas.hdf5javalib.file.metadata.HdfSuperblock;
import com.github.karlnicholas.hdf5javalib.message.*;
import lombok.Getter;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.List;

@Getter
public class HdfReader {
    // level 0
    private HdfSuperblock superblock;
    private HdfSymbolTableEntry rootGroupSymbolTableEntry;
    private HdfObjectHeaderPrefixV1 objectHeader;
    // level 1A
    private HdfBTreeV1 bTree;
    // level 1D
    private HdfLocalHeap localHeap;
    private HdfLocalHeapContents localHeapContents;
    // level 2A1
    private HdfObjectHeaderPrefixV1 dataObjectHeaderPrefix;
    // parsed Datatype
    private CompoundDataType compoundDataType;
    private long dataAddress = 0;
    private long dimensionSize = 0;
    private long dimension = 0;

    public void readFile(FileChannel fileChannel) throws IOException {
        long fPosSave = fileChannel.position();
        System.out.print(fileChannel.position() + " = ");
        // Parse the superblock at the beginning of the file
        superblock = HdfSuperblock.readFromFileChannel(fileChannel);
        System.out.println(superblock);
        System.out.println("superblock = " + (fileChannel.position() - fPosSave));
        fPosSave = fileChannel.position();


        short offsetSize = superblock.getSizeOfOffsets();
        short lengthSize = superblock.getSizeOfLengths();

        HdfSymbolTableEntry.HdfFileOffsets fileOffsets = HdfSymbolTableEntry.fromFileChannel(fileChannel, offsetSize);

        // Get the object header address from the superblock
        long objectHeaderAddress =fileOffsets.getObjectHeaderAddress().getBigIntegerValue().longValue();
        // Parse the object header from the file using the superblock information
        System.out.print(objectHeaderAddress + " = ");
        fileChannel.position(objectHeaderAddress);
        objectHeader = HdfObjectHeaderPrefixV1.readFromFileChannel(fileChannel, offsetSize, lengthSize);
        System.out.println(objectHeader);
        System.out.println("objectHeaderPrefix = " + (fileChannel.position() - fPosSave));

        // Parse the local heap using the file channel
        long localHeapAddress = objectHeader.findHdfSymbolTableMessage(SymbolTableMessage.class)
                .orElseThrow().getLocalHeapAddress().getBigIntegerValue().longValue();
        System.out.print(localHeapAddress + " = ");
        // Read data from file channel starting at the specified position
        fileChannel.position(localHeapAddress);
        fPosSave = fileChannel.position();

// --------------------------------------
        localHeap = HdfLocalHeap.readFromFileChannel(fileChannel, superblock.getSizeOfOffsets(), superblock.getSizeOfLengths());
        System.out.println(localHeap);
        System.out.println("localHeap = " + (fileChannel.position() - fPosSave));
        fPosSave = fileChannel.position();

        int dataSize = localHeap.getDataSegmentSize().getBigIntegerValue().intValue();
        long dataSegmentAddress = localHeap.getDataSegmentAddress().getBigIntegerValue().longValue();
        fileChannel.position(dataSegmentAddress);
        System.out.print(dataSegmentAddress + " = ");
        this.localHeapContents = HdfLocalHeapContents.readFromFileChannel(fileChannel, dataSize);
        System.out.println(localHeapContents);
        System.out.println("localHeapContents = " + (fileChannel.position() - fPosSave));

        if ( superblock.getVersion() == 0 ) {
            long bTreeAddress = objectHeader.findHdfSymbolTableMessage(SymbolTableMessage.class)
                    .orElseThrow().getBTreeAddress().getBigIntegerValue().longValue();
            System.out.print(bTreeAddress + " = ");
            fileChannel.position(bTreeAddress);
            bTree = HdfBTreeV1.readFromFileChannel(fileChannel, superblock.getSizeOfOffsets(), superblock.getSizeOfLengths());
            System.out.println(bTree);
        }
        // check if any groups
        if (bTree.getEntries().size() > 0) {
            // Parse root group symbol table entry from the current position
            System.out.print(fileChannel.position() + " = ");
//        rootGroupSymbolTableEntry = HdfSymbolTableEntry.fromFileChannel(fileChannel, superblock.getSizeOfOffsets());
            rootGroupSymbolTableEntry = new HdfSymbolTableEntry(HdfFixedPoint.of(0), objectHeader, bTree, localHeap);
            System.out.println(rootGroupSymbolTableEntry);
            System.out.println("rootGroupSymbolTableEntry = " + (fileChannel.position() - fPosSave));
            fPosSave = fileChannel.position();

            long snodAddress = bTree.getEntries().get(0).getChildPointer().getBigIntegerValue().longValue();
            fileChannel.position(snodAddress);
            HdfGroupSymbolTableNode hdfGroupSymbolTableNode = HdfGroupSymbolTableNode.readFromFileChannel(fileChannel, offsetSize, List.of(rootGroupSymbolTableEntry));
            System.out.println(hdfGroupSymbolTableNode);

            // Parse the Data Object Header Prefix next in line
            fileChannel.position(fileOffsets.getObjectHeaderAddress().getBigIntegerValue().longValue());
            System.out.print(fileChannel.position() + " = ");
// --------------------------------------

            dataObjectHeaderPrefix = HdfObjectHeaderPrefixV1.readFromFileChannel(fileChannel, offsetSize, lengthSize);
            System.out.println(dataObjectHeaderPrefix);

            for (HdfMessage message : dataObjectHeaderPrefix.getHeaderMessages()) {
                if (message instanceof DatatypeMessage dataTypeMessage) {
                    // Check if the datatype is Compound
                    if (dataTypeMessage.getDataTypeClass() == 6) {
                        compoundDataType = (CompoundDataType) dataTypeMessage.getHdfDataType();
                    } else {
                        // For other datatype classes, parsing logic will be added later
                        throw new UnsupportedOperationException("Datatype class " + dataTypeMessage.getDataTypeClass() + " not yet implemented.");
                    }
                } else if (message instanceof DataLayoutMessage dataLayoutMessage) {
                    dataAddress = dataLayoutMessage.getDataAddress().getBigIntegerValue().longValue();
                    dimensionSize = dataLayoutMessage.getDimensionSizes()[0].getBigIntegerValue().longValue();
                } else if (message instanceof DataspaceMessage dataSpaceMessage) {
                    dimension = dataSpaceMessage.getDimensions()[0].getBigIntegerValue().longValue();
                }
            }
        }


//        System.out.println("DataType{" + compoundDataType + "\r\n}");

        System.out.println("Parsing complete. NEXT: " + fileChannel.position());
    }
}
