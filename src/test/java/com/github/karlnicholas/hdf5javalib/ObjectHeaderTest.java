package com.github.karlnicholas.hdf5javalib;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ObjectHeaderTest {

    @Test
    void testReadAndWrite() {
        // Create an object header with test values
        ObjectHeader originalHeader = new ObjectHeader(1, 8, 16, 4);

        // Write the header to bytes
        byte[] writtenBytes = originalHeader.writeToBytes();

        // Read the header from bytes
        ObjectHeader readHeader = ObjectHeader.readFromBytes(writtenBytes);

        // Assert the values are the same
        assertEquals(originalHeader.getVersion(), readHeader.getVersion());
        assertEquals(originalHeader.getHeaderSize(), readHeader.getHeaderSize());
        assertEquals(originalHeader.getChunkSize(), readHeader.getChunkSize());
        assertEquals(originalHeader.getEntryCount(), readHeader.getEntryCount());

        // Assert the byte array length is correct
        assertEquals(8, writtenBytes.length);
    }

    @Test
    void testToString() {
        ObjectHeader header = new ObjectHeader(2, 16, 32, 8);
        String expectedString = "ObjectHeader{version=2, headerSize=16, chunkSize=32, entryCount=8}";
        assertEquals(expectedString, header.toString());
    }

    @Test
    void testInvalidRead() {
        byte[] invalidBytes = new byte[4];
        assertThrows(IllegalArgumentException.class, () -> ObjectHeader.readFromBytes(invalidBytes));
    }
}
