package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.datatype.HdfFixedPoint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class HdfSuperblock {
    private static final byte[] FILE_SIGNATURE = new byte[]{(byte) 0x89, 0x48, 0x44, 0x46, 0x0d, 0x0a, 0x1a, 0x0a};

    private final int version;
    private final int freeSpaceVersion;
    private final int rootGroupVersion;
    private final int sharedHeaderVersion;
    private final int sizeOfOffsets;
    private final int sizeOfLengths;
    private final int groupLeafNodeK;
    private final int groupInternalNodeK;

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

    public static HdfSuperblock readFromFileChannel(FileChannel fileChannel) throws IOException {
        // Step 1: Allocate the minimum buffer size to determine the version
        ByteBuffer buffer = ByteBuffer.allocate(8 + 1); // File signature (8 bytes) + version (1 byte)
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Read the initial bytes to determine the version
        fileChannel.read(buffer);
        buffer.flip();

        // Verify file signature
        byte[] signature = new byte[8];
        buffer.get(signature);
        if (!java.util.Arrays.equals(signature, FILE_SIGNATURE)) {
            throw new IllegalArgumentException("Invalid file signature");
        }

        // Read version
        int version = Byte.toUnsignedInt(buffer.get());

        // Step 2: Determine the size of the superblock based on the version
        int superblockSize;
        if (version == 0) {
            superblockSize = 56; // Version 0 superblock size
        } else if (version == 1) {
            superblockSize = 96; // Version 1 superblock size (example value, adjust per spec)
        } else {
            throw new IllegalArgumentException("Unsupported HDF5 superblock version: " + version);
        }

        // Step 3: Allocate a new buffer for the full superblock
        buffer = ByteBuffer.allocate(superblockSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Reset file channel position to re-read from the beginning
        fileChannel.position(0);

        // Read the full superblock
        fileChannel.read(buffer);
        buffer.flip();

        // Step 4: Parse the remaining superblock fields
        buffer.position(9); // Skip the file signature
        int freeSpaceVersion = Byte.toUnsignedInt(buffer.get());
        int rootGroupVersion = Byte.toUnsignedInt(buffer.get());
        buffer.get(); // Skip reserved
        int sharedHeaderVersion = Byte.toUnsignedInt(buffer.get());
        int sizeOfOffsets = Byte.toUnsignedInt(buffer.get());
        int sizeOfLengths = Byte.toUnsignedInt(buffer.get());
        buffer.get(); // Skip reserved

        int groupLeafNodeK = Short.toUnsignedInt(buffer.getShort());
        int groupInternalNodeK = Short.toUnsignedInt(buffer.getShort());
        buffer.getInt(); // Skip consistency flags

        // Parse addresses using HdfFixedPoint
        HdfFixedPoint baseAddress = HdfFixedPoint.readFromByteBuffer(buffer, sizeOfOffsets, false);
        HdfFixedPoint freeSpaceAddress = HdfFixedPoint.checkUndefined(buffer, sizeOfOffsets) ? HdfFixedPoint.undefined(buffer, sizeOfOffsets) : HdfFixedPoint.readFromByteBuffer(buffer, sizeOfOffsets, false);
        HdfFixedPoint endOfFileAddress = HdfFixedPoint.checkUndefined(buffer, sizeOfOffsets) ? HdfFixedPoint.undefined(buffer, sizeOfOffsets) : HdfFixedPoint.readFromByteBuffer(buffer, sizeOfOffsets, false);
        HdfFixedPoint driverInformationAddress = HdfFixedPoint.checkUndefined(buffer, sizeOfOffsets) ? HdfFixedPoint.undefined(buffer, sizeOfOffsets) : HdfFixedPoint.readFromByteBuffer(buffer, sizeOfOffsets, false);

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

    public int getSizeOfOffsets() {
        return sizeOfOffsets;
    }

    public int getSizeOfLengths() {
        return sizeOfLengths;
    }

    public HdfFixedPoint getBaseAddress() {
        return baseAddress;
    }

    public int getVersion() {
        return version;
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
