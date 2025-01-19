package com.github.karlnicholas.hdf5javalib;

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

    public HdfReader(File file) {
        this.file = file;
    }

    public void readFile() throws IOException {
        // Ensure the file exists and is readable
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Cannot read the file: " + file.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fileChannel = fis.getChannel()) {

            // Allocate a buffer to read the file
            ByteBuffer buffer = ByteBuffer.allocate((int) Files.size(file.toPath()));
            fileChannel.read(buffer);
            buffer.flip(); // Prepare the buffer for reading

            // Parse the HDF Superblock
            parseSuperblock(buffer);

            // Parse the HDF Symbol Table Entry
            parseSymbolTableEntry(buffer);

            // Parse the Object Header V1
            parseObjectHeader(buffer);

            // Parse the B-tree if present
            parseBTree(buffer);

            // Read and parse until the heap start address
            int heapStartAddress = symbolTableEntry.getLocalHeapAddress().getBigIntegerValue().intValue();
            readUntilAddress(buffer, heapStartAddress);

            // Parse the local heap
            parseLocalHeap(buffer);

            // Read and store local heap contents
            parseLocalHeapContents(buffer);
        }
    }

    private void parseSuperblock(ByteBuffer buffer) {
        System.out.println("Parsing superblock...");
        this.superblock = HdfSuperblock.readFromBuffer(buffer);
        System.out.println("Superblock parsed: " + superblock);
    }

    private void parseSymbolTableEntry(ByteBuffer buffer) {
        System.out.println("Parsing symbol table entry...");

        // Use the superblock to determine the size of offsets
        int offsetSize = superblock.getSizeOfOffsets();

        // Adjust the buffer position to the base address by consuming bytes
        int baseAddress = superblock.getBaseAddress().getBigIntegerValue().intValue();
        if (baseAddress != 0) {
            readUntilAddress(buffer, baseAddress);
        }

        // Parse the symbol table entry
        this.symbolTableEntry = HdfSymbolTableEntry.fromByteBuffer(buffer, offsetSize);
        System.out.println("Symbol table entry parsed: " + symbolTableEntry);
    }

    private void parseObjectHeader(ByteBuffer buffer) {
        System.out.println("Parsing object header...");

        // Use the objectHeaderAddress from the symbol table entry
        int objectHeaderAddress = symbolTableEntry.getObjectHeaderAddress().getBigIntegerValue().intValue();
        readUntilAddress(buffer, objectHeaderAddress);

        // Use the offset size to parse the object header
        int offsetSize = superblock.getSizeOfOffsets();
        this.objectHeader = HdfObjectHeaderV1.fromByteBuffer(buffer, offsetSize);
        System.out.println("Object header parsed: " + objectHeader);
    }

    private void parseBTree(ByteBuffer buffer) {
        if (symbolTableEntry.getCacheType() == 1 && symbolTableEntry.getBTreeAddress() != null) {
            System.out.println("Parsing B-tree at address: " + symbolTableEntry.getBTreeAddress());

            // Parse the B-tree
            int bTreeAddress = symbolTableEntry.getBTreeAddress().getBigIntegerValue().intValue();
            readUntilAddress(buffer, bTreeAddress);
            HdfBTreeV1 bTreeNode = HdfBTreeV1.fromByteBuffer(buffer, superblock);

            System.out.println("B-tree parsed: " + bTreeNode);
        }
    }

    private void parseLocalHeap(ByteBuffer buffer) {
        System.out.println("Parsing local heap...");

        // Parse the local heap using the superblock for offset and length sizes
        this.localHeap = HdfLocalHeap.fromByteBuffer(buffer, superblock);

        System.out.println("Local heap parsed: " + localHeap);
    }

    private void parseLocalHeapContents(ByteBuffer buffer) {
        System.out.println("Reading local heap contents...");
        int dataSegmentAddress = localHeap.getDataSegmentAddress().getBigIntegerValue().intValue();
        readUntilAddress(buffer, dataSegmentAddress);

        // Read the heap contents into a dedicated class
        int dataSize = localHeap.getDataSegmentSize().getBigIntegerValue().intValue();
        byte[] heapData = new byte[dataSize];
        buffer.get(heapData);

        this.localHeapContents = new HdfLocalHeapContents(heapData);
        System.out.println("Local heap contents read.");
    }

    private void readUntilAddress(ByteBuffer buffer, int targetAddress) {
        int currentPosition = buffer.position();
        if (currentPosition > targetAddress) {
            throw new IllegalArgumentException("Buffer already past the target address.");
        }
        while (currentPosition < targetAddress) {
            buffer.get(); // Consume one byte at a time
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
}
