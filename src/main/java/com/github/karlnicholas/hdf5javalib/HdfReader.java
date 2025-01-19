package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.datatypes.HdfCompoundDatatype;
import com.github.karlnicholas.hdf5javalib.datatypes.HdfDatatypeParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

public class HdfReader {
    private final File file;
    private HdfSuperblock superblock;
    private HdfSymbolTableEntry symbolTableEntry;
    private HdfObjectHeaderV1 objectHeader;
    private HdfLocalHeap localHeap;
    private HdfLocalHeapContents localHeapContents;
    private HdfCompoundDatatype compoundDatatype;
    private HdfDataHeaderV1 dataHeaderV1;

    public HdfReader(File file) {
        this.file = file;
    }

    public void readFile() throws IOException {
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Cannot read the file: " + file.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fileChannel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate((int) Files.size(file.toPath()));
            fileChannel.read(buffer);
            buffer.flip();

            parseSuperblock(buffer);
            parseSymbolTableEntry(buffer);
            parseObjectHeader(buffer);
            parseBTree(buffer);
            int heapStartAddress = symbolTableEntry.getLocalHeapAddress().getBigIntegerValue().intValue();
            readUntilAddress(buffer, heapStartAddress);
            parseLocalHeap(buffer);
            parseLocalHeapContents(buffer);
            parseDataHeaderV1(buffer);

            System.out.println("Parsing complete. NEXT: " + buffer.position());
        }
    }

    private void parseSuperblock(ByteBuffer buffer) {
        System.out.println("Parsing superblock...");
        this.superblock = HdfSuperblock.readFromBuffer(buffer);
        System.out.println("Superblock parsed: " + superblock);
    }

    private void parseSymbolTableEntry(ByteBuffer buffer) {
        System.out.println("Parsing symbol table entry...");
        int offsetSize = superblock.getSizeOfOffsets();
        int baseAddress = superblock.getBaseAddress().getBigIntegerValue().intValue();
        if (baseAddress != 0) {
            readUntilAddress(buffer, baseAddress);
        }
        this.symbolTableEntry = HdfSymbolTableEntry.fromByteBuffer(buffer, offsetSize);
        System.out.println("Symbol table entry parsed: " + symbolTableEntry);
    }

    private void parseObjectHeader(ByteBuffer buffer) {
        System.out.println("Parsing object header...");
        int offsetSize = superblock.getSizeOfOffsets();
        this.objectHeader = new HdfObjectHeaderV1(buffer, offsetSize);

        System.out.println("Object header parsed:");
        System.out.println("Version: " + objectHeader.getVersion());
        System.out.println("Total Header Messages: " + objectHeader.getTotalHeaderMessages());
        System.out.println("Object Reference Count: " + objectHeader.getObjectReferenceCount());
        System.out.println("Object Header Size: " + objectHeader.getObjectHeaderSize());

        for (HdfHeaderMessage message : objectHeader.getHeaderMessages()) {
            System.out.println(message);
        }
    }

    private void parseBTree(ByteBuffer buffer) {
        if (symbolTableEntry.getCacheType() == 1 && symbolTableEntry.getBTreeAddress() != null) {
            System.out.println("Parsing B-tree at address: " + symbolTableEntry.getBTreeAddress());
            int bTreeAddress = symbolTableEntry.getBTreeAddress().getBigIntegerValue().intValue();
            readUntilAddress(buffer, bTreeAddress);
            HdfBTreeV1 bTreeNode = HdfBTreeV1.fromByteBuffer(buffer, superblock);
            System.out.println("B-tree parsed: " + bTreeNode);
        }
    }

    private void parseLocalHeap(ByteBuffer buffer) {
        System.out.println("Parsing local heap...");
        this.localHeap = HdfLocalHeap.fromByteBuffer(buffer, superblock);
        System.out.println("Local heap parsed: " + localHeap);
    }

    private void parseLocalHeapContents(ByteBuffer buffer) {
        System.out.println("Reading local heap contents...");
        int dataSegmentAddress = localHeap.getDataSegmentAddress().getBigIntegerValue().intValue();
        readUntilAddress(buffer, dataSegmentAddress);
        int dataSize = localHeap.getDataSegmentSize().getBigIntegerValue().intValue();
        byte[] heapData = new byte[dataSize];
        buffer.get(heapData);
        this.localHeapContents = new HdfLocalHeapContents(heapData);
        System.out.println("Local heap contents read.");
    }

    private void parseDataHeaderV1(ByteBuffer buffer) {
        System.out.println("Parsing HDF Data Header V1...");

        // Parse the data header
        this.dataHeaderV1 = new HdfDataHeaderV1(buffer);

        // Display parsed details using toString
        System.out.println("Version: " + dataHeaderV1.getVersion());
        System.out.println("Total Header Messages: " + dataHeaderV1.getTotalHeaderMessages());
        System.out.println("Object Reference Count: " + dataHeaderV1.getObjectReferenceCount());
        System.out.println("Object Header Size: " + dataHeaderV1.getObjectHeaderSize());

        // Print each header message using the toString() method
        for (HdfDataHeaderMessage message : dataHeaderV1.getHeaderMessages()) {
            System.out.println(message);
        }
    }

    private void readUntilAddress(ByteBuffer buffer, int targetAddress) {
        int currentPosition = buffer.position();
        if (currentPosition > targetAddress) {
            throw new IllegalArgumentException("Buffer already past the target address.");
        }
        while (currentPosition < targetAddress) {
            buffer.get();
            currentPosition++;
        }
    }

    public HdfSuperblock getSuperblock() {
        return superblock;
    }

    public HdfSymbolTableEntry getSymbolTableEntry() {
        return symbolTableEntry;
    }

    public HdfObjectHeaderV1 getObjectHeader() {
        return objectHeader;
    }

    public HdfLocalHeap getLocalHeap() {
        return localHeap;
    }

    public HdfLocalHeapContents getLocalHeapContents() {
        return localHeapContents;
    }

    public HdfCompoundDatatype getCompoundDatatype() {
        return compoundDatatype;
    }

    public HdfDataHeaderV1 getDataHeaderV1() {
        return dataHeaderV1;
    }
}
