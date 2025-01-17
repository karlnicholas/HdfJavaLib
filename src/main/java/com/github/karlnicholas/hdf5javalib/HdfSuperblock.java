package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HdfSuperblock {
    private static final byte[] FILE_SIGNATURE = new byte[]{(byte) 0x89, 0x48, 0x44, 0x46, 0x0d, 0x0a, 0x1a, 0x0a};

    // Superblock metadata fields
    private final int version;
    private final int freeSpaceVersion;
    private final int rootGroupVersion;
    private final int sharedHeaderVersion;
    private final int sizeOfOffsets;
    private final int sizeOfLengths;
    private final int groupLeafNodeK;
    private final int groupInternalNodeK;

    // Superblock addresses
    private final HdfFixedPoint baseAddress;
    private final HdfFixedPoint freeSpaceAddress;
    private final HdfFixedPoint endOfFileAddress;
    private final HdfFixedPoint driverInformationAddress;

    public HdfSuperblock(
            int version,
            int freeSpaceVersion,
            int rootGroupVersion,
            int sharedHeaderVersion,
            int sizeOfOffsets,
            int sizeOfLengths,
            int groupLeafNodeK,
            int groupInternalNodeK,
            HdfFixedPoint baseAddress,
            HdfFixedPoint freeSpaceAddress,
            HdfFixedPoint endOfFileAddress,
            HdfFixedPoint driverInformationAddress
    ) {
        this.version = version;
        this.freeSpaceVersion = freeSpaceVersion;
        this.rootGroupVersion = rootGroupVersion;
        this.sharedHeaderVersion = sharedHeaderVersion;
        this.sizeOfOffsets = sizeOfOffsets;
        this.sizeOfLengths = sizeOfLengths;
        this.groupLeafNodeK = groupLeafNodeK;
        this.groupInternalNodeK = groupInternalNodeK;
        this.baseAddress = baseAddress;
        this.freeSpaceAddress = freeSpaceAddress;
        this.endOfFileAddress = endOfFileAddress;
        this.driverInformationAddress = driverInformationAddress;
    }

    public static HdfSuperblock readFromBuffer(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Read and verify the file signature
        byte[] signature = new byte[8];
        buffer.get(signature);
        if (!java.util.Arrays.equals(signature, FILE_SIGNATURE)) {
            throw new IllegalArgumentException("Invalid file signature");
        }

        // Read version and other metadata fields
        int version = Byte.toUnsignedInt(buffer.get());
        int freeSpaceVersion = Byte.toUnsignedInt(buffer.get());
        int rootGroupVersion = Byte.toUnsignedInt(buffer.get());
        buffer.get(); // Skip reserved (1 byte, zero)
        int sharedHeaderVersion = Byte.toUnsignedInt(buffer.get());
        int sizeOfOffsets = Byte.toUnsignedInt(buffer.get());
        int sizeOfLengths = Byte.toUnsignedInt(buffer.get());
        buffer.get(); // Skip reserved (1 byte, zero)

        // Read tree parameters
        int groupLeafNodeK = Short.toUnsignedInt(buffer.getShort());
        int groupInternalNodeK = Short.toUnsignedInt(buffer.getShort());
        buffer.getInt(); // Skip file consistency flags

        // Parse addresses using the sizeOfOffsets
        HdfFixedPoint baseAddress = new HdfFixedPoint(readBytes(buffer, sizeOfOffsets), sizeOfOffsets * 8, false);
        HdfFixedPoint freeSpaceAddress = new HdfFixedPoint(readBytes(buffer, sizeOfOffsets), sizeOfOffsets * 8, false);
        HdfFixedPoint endOfFileAddress = new HdfFixedPoint(readBytes(buffer, sizeOfOffsets), sizeOfOffsets * 8, false);
        HdfFixedPoint driverInformationAddress = new HdfFixedPoint(readBytes(buffer, sizeOfOffsets), sizeOfOffsets * 8, false);

        return new HdfSuperblock(
                version,
                freeSpaceVersion,
                rootGroupVersion,
                sharedHeaderVersion,
                sizeOfOffsets,
                sizeOfLengths,
                groupLeafNodeK,
                groupInternalNodeK,
                baseAddress,
                freeSpaceAddress,
                endOfFileAddress,
                driverInformationAddress
        );
    }

    private static byte[] readBytes(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    @Override
    public String toString() {
        return "HdfSuperblock{" +
                "version=" + version +
                ", freeSpaceVersion=" + freeSpaceVersion +
                ", rootGroupVersion=" + rootGroupVersion +
                ", sharedHeaderVersion=" + sharedHeaderVersion +
                ", sizeOfOffsets=" + sizeOfOffsets +
                ", sizeOfLengths=" + sizeOfLengths +
                ", groupLeafNodeK=" + groupLeafNodeK +
                ", groupInternalNodeK=" + groupInternalNodeK +
                ", baseAddress=" + baseAddress +
                ", freeSpaceAddress=" + freeSpaceAddress +
                ", endOfFileAddress=" + endOfFileAddress +
                ", driverInformationAddress=" + driverInformationAddress +
                '}';
    }
}
