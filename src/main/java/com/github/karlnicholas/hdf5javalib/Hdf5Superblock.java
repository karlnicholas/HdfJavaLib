package com.github.karlnicholas.hdf5javalib;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Hdf5Superblock {
    private static final byte[] FILE_SIGNATURE = new byte[]{(byte) 0x89, 0x48, 0x44, 0x46, 0x0d, 0x0a, 0x1a, 0x0a};
    private final int version;
    private final long baseAddress;
    private final long freeSpaceAddress;
    private final long endOfFileAddress;
    private final long driverInformationAddress;

    public Hdf5Superblock(int version, long baseAddress, long freeSpaceAddress, long endOfFileAddress, long driverInformationAddress) {
        this.version = version;
        this.baseAddress = baseAddress;
        this.freeSpaceAddress = freeSpaceAddress;
        this.endOfFileAddress = endOfFileAddress;
        this.driverInformationAddress = driverInformationAddress;
    }

    public void writeToBuffer(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Write the file signature (8 bytes)
        buffer.put(FILE_SIGNATURE);

        // Write version (1 byte) and 7 bytes of padding
        buffer.put((byte) version);
        buffer.put(new byte[7]);

        // Write the addresses (each is 8 bytes)
        buffer.putLong(baseAddress);
        buffer.putLong(freeSpaceAddress);
        buffer.putLong(endOfFileAddress);
        buffer.putLong(driverInformationAddress);
    }

    public static Hdf5Superblock readFromBuffer(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Read and verify the file signature
        byte[] signature = new byte[8];
        buffer.get(signature);
        if (!java.util.Arrays.equals(signature, FILE_SIGNATURE)) {
            throw new IllegalArgumentException("Invalid file signature");
        }

        // Read version (1 byte) and skip padding (7 bytes)
        int version = Byte.toUnsignedInt(buffer.get());
        buffer.position(buffer.position() + 7);

        // Read the addresses
        long baseAddress = buffer.getLong();
        long freeSpaceAddress = buffer.getLong();
        long endOfFileAddress = buffer.getLong();
        long driverInformationAddress = buffer.getLong();

        return new Hdf5Superblock(version, baseAddress, freeSpaceAddress, endOfFileAddress, driverInformationAddress);
    }

    public int getVersion() {
        return version;
    }

    public long getBaseAddress() {
        return baseAddress;
    }

    public long getFreeSpaceAddress() {
        return freeSpaceAddress;
    }

    public long getEndOfFileAddress() {
        return endOfFileAddress;
    }

    public long getDriverInformationAddress() {
        return driverInformationAddress;
    }

    @Override
    public String toString() {
        return "Hdf5Superblock{" +
                "version=" + version +
                ", baseAddress=" + baseAddress +
                ", freeSpaceAddress=" + freeSpaceAddress +
                ", endOfFileAddress=" + endOfFileAddress +
                ", driverInformationAddress=" + driverInformationAddress +
                '}';
    }
}
