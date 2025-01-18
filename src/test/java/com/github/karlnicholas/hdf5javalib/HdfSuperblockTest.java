package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class HdfSuperblockTest {

    @Test
    void testReadAndWrite() {
        // Create a superblock with sample values
        HdfFixedPoint baseAddress = new HdfFixedPoint(ByteBuffer.allocate(8).putLong(1024L).flip(), 64, false);
        HdfFixedPoint freeSpaceAddress = new HdfFixedPoint(ByteBuffer.allocate(8).putLong(2048L).flip(), 64, false);
        HdfFixedPoint endOfFileAddress = new HdfFixedPoint(ByteBuffer.allocate(8).putLong(4096L).flip(), 64, false);
        HdfFixedPoint driverInformationAddress = new HdfFixedPoint(ByteBuffer.allocate(8).putLong(8192L).flip(), 64, false);

        HdfSuperblock superblock = new HdfSuperblock(
                0,
                1,
                1,
                1,
                8,
                8,
                16,
                16,
                baseAddress,
                freeSpaceAddress,
                endOfFileAddress,
                driverInformationAddress
        );

        // Write the superblock to a ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(56);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN); // Ensure little-endian ordering
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
        HdfSuperblock readSuperblock = HdfSuperblock.readFromBuffer(buffer);

        // Verify that the fields match
        assertEquals(superblock.getVersion(), readSuperblock.getVersion(), "Version mismatch");
        assertEquals(superblock.getBaseAddress().getBigIntegerValue(), readSuperblock.getBaseAddress().getBigIntegerValue(), "Base address mismatch");
        assertEquals(superblock.getFreeSpaceAddress().getBigIntegerValue(), readSuperblock.getFreeSpaceAddress().getBigIntegerValue(), "Free space address mismatch");
        assertEquals(superblock.getEndOfFileAddress().getBigIntegerValue(), readSuperblock.getEndOfFileAddress().getBigIntegerValue(), "End of file address mismatch");
        assertEquals(superblock.getDriverInformationAddress().getBigIntegerValue(), readSuperblock.getDriverInformationAddress().getBigIntegerValue(), "Driver information address mismatch");
    }

    @Test
    void testInvalidSignature() {
        // Create an invalid ByteBuffer with the wrong signature
        ByteBuffer buffer = ByteBuffer.allocate(56);
        buffer.put(new byte[56]); // Fill with zeroes
        buffer.flip();

        // Expect an exception due to invalid signature
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                HdfSuperblock.readFromBuffer(buffer));

        assertEquals("Invalid file signature", exception.getMessage(), "Expected exception for invalid file signature");
    }

    @Test
    void testInvalidSize() {
        // Create a ByteBuffer smaller than the required size
        ByteBuffer smallBuffer = ByteBuffer.allocate(40);

        // Expect an exception due to insufficient size
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                HdfSuperblock.readFromBuffer(smallBuffer));

        assertEquals("Invalid file signature", exception.getMessage(), "Expected exception for invalid signature with insufficient size");
    }
}
