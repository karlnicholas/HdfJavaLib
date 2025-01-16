package com.github.karlnicholas.hdf5javalib;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class Hdf5SuperblockTest {

    @Test
    void testReadAndWrite() {
        // Create a superblock with sample values
        Hdf5Superblock superblock = new Hdf5Superblock(1, 1024L, 2048L, 4096L, 8192L);

        // Write the superblock to a ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(48);
        superblock.writeToBuffer(buffer);

        // Flip the buffer to prepare it for reading
        buffer.flip();

        // Verify the file signature
        byte[] expectedSignature = new byte[]{(byte) 0x89, 0x48, 0x44, 0x46, 0x0d, 0x0a, 0x1a, 0x0a};
        byte[] actualSignature = new byte[8];
        buffer.get(actualSignature);
        assertArrayEquals(expectedSignature, actualSignature, "Invalid file signature");

        // Reset buffer position to read the superblock
        buffer.position(0);
        Hdf5Superblock readSuperblock = Hdf5Superblock.readFromBuffer(buffer);

        // Verify that the fields match
        assertEquals(superblock.getVersion(), readSuperblock.getVersion(), "Version mismatch");
        assertEquals(superblock.getBaseAddress(), readSuperblock.getBaseAddress(), "Base address mismatch");
        assertEquals(superblock.getFreeSpaceAddress(), readSuperblock.getFreeSpaceAddress(), "Free space address mismatch");
        assertEquals(superblock.getEndOfFileAddress(), readSuperblock.getEndOfFileAddress(), "End of file address mismatch");
        assertEquals(superblock.getDriverInformationAddress(), readSuperblock.getDriverInformationAddress(), "Driver information address mismatch");
    }

    @Test
    void testInvalidSignature() {
        // Create an invalid ByteBuffer with the wrong signature
        ByteBuffer buffer = ByteBuffer.allocate(48);
        buffer.put(new byte[48]); // Fill with zeroes
        buffer.flip();

        // Expect an exception due to invalid signature
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Hdf5Superblock.readFromBuffer(buffer));

        assertEquals("Invalid file signature", exception.getMessage(), "Expected exception for invalid file signature");
    }

    @Test
    void testInvalidSize() {
        // Create a ByteBuffer smaller than the required 48 bytes
        ByteBuffer smallBuffer = ByteBuffer.allocate(40);

        // Expect an exception due to insufficient size
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Hdf5Superblock.readFromBuffer(smallBuffer));

        assertEquals("Invalid file signature", exception.getMessage(), "Expected exception for invalid signature with insufficient size");
    }
}
