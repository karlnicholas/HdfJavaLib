package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

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

            // Parse the HDF Object Header V1
            parseObjectHeader(buffer);
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

        // Adjust the buffer position to the base address
        int baseAddress = superblock.getBaseAddress().getBigIntegerValue().intValue();
        if (baseAddress != 0) {
            buffer.position(baseAddress);
        }

        // Parse the symbol table entry
        this.symbolTableEntry = HdfSymbolTableEntry.fromByteBuffer(buffer, offsetSize);
        System.out.println("Symbol table entry parsed: " + symbolTableEntry);
    }

    private void parseObjectHeader(ByteBuffer buffer) {
        System.out.println("Parsing object header...");

        // Get the object header address from the symbol table entry
        int objectHeaderAddress = symbolTableEntry.getObjectHeaderAddress().getBigIntegerValue().intValue();
        buffer.position(objectHeaderAddress);

        // Parse the object header
        this.objectHeader = HdfObjectHeaderV1.fromByteBuffer(buffer);
        System.out.println("Object header parsed: " + objectHeader);
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
}
