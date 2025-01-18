package com.github.karlnicholas.hdf5javalib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class HdfReader {
    private final File file;
    private HdfSuperblock superblock;
    private HdfSymbolTableEntry symbolTableEntry;

    public HdfReader(File file) {
        this.file = file;
    }

    public void readFile() throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fileChannel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size()).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            fileChannel.read(buffer);
            buffer.flip();

            // Parse the superblock
            System.out.println("Parsing superblock...");
            this.superblock = HdfSuperblock.readFromBuffer(buffer);
            System.out.println("Superblock parsed: " + superblock);

            // Parse the symbol table entry
            System.out.println("Parsing symbol table entry...");
            int offsetSize = superblock.getSizeOfOffsets();
            this.symbolTableEntry = HdfSymbolTableEntry.fromByteBuffer(buffer, offsetSize);
            System.out.println("Symbol table entry parsed: " + symbolTableEntry);
        }
    }

    public HdfSuperblock getSuperblock() {
        return superblock;
    }

    public HdfSymbolTableEntry getSymbolTableEntry() {
        return symbolTableEntry;
    }
}
