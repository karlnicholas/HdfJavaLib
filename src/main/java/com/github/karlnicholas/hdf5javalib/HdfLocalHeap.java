package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

import java.nio.ByteBuffer;

public class HdfLocalHeap {
    private final String signature;
    private final int version;
    private final HdfFixedPoint dataSegmentSize;
    private final HdfFixedPoint freeListOffset;
    private final HdfFixedPoint dataSegmentAddress;

    public HdfLocalHeap(String signature, int version, HdfFixedPoint dataSegmentSize, HdfFixedPoint freeListOffset, HdfFixedPoint dataSegmentAddress) {
        this.signature = signature;
        this.version = version;
        this.dataSegmentSize = dataSegmentSize;
        this.freeListOffset = freeListOffset;
        this.dataSegmentAddress = dataSegmentAddress;
    }

    public static HdfLocalHeap fromByteBuffer(ByteBuffer buffer, HdfSuperblock superblock) {
        byte[] signatureBytes = new byte[4];
        buffer.get(signatureBytes);
        String signature = new String(signatureBytes);
        if (!"HEAP".equals(signature)) {
            throw new IllegalArgumentException("Invalid heap signature: " + signature);
        }

        int version = Byte.toUnsignedInt(buffer.get());
        byte[] reserved = new byte[3]; // Read 3 reserved bytes
        buffer.get(reserved);
        if (!allBytesZero(reserved)) {
            throw new IllegalArgumentException("Reserved bytes in heap header must be zero.");
        }

        // Use superblock sizes to parse length and offset fields
        int lengthSize = superblock.getSizeOfLengths();
        int offsetSize = superblock.getSizeOfOffsets();

        HdfFixedPoint dataSegmentSize = new HdfFixedPoint(buffer, lengthSize * 8, false);
        HdfFixedPoint freeListOffset = new HdfFixedPoint(buffer, lengthSize * 8, false);
        HdfFixedPoint dataSegmentAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);

        return new HdfLocalHeap(signature, version, dataSegmentSize, freeListOffset, dataSegmentAddress);
    }

    private static boolean allBytesZero(byte[] bytes) {
        for (byte b : bytes) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "HdfLocalHeap{" +
                "signature='" + signature + '\'' +
                ", version=" + version +
                ", dataSegmentSize=" + dataSegmentSize +
                ", freeListOffset=" + freeListOffset +
                ", dataSegmentAddress=" + dataSegmentAddress +
                '}';
    }

    public String getSignature() {
        return signature;
    }

    public int getVersion() {
        return version;
    }

    public HdfFixedPoint getDataSegmentSize() {
        return dataSegmentSize;
    }

    public HdfFixedPoint getFreeListOffset() {
        return freeListOffset;
    }

    public HdfFixedPoint getDataSegmentAddress() {
        return dataSegmentAddress;
    }
}
