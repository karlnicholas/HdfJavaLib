package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

import java.nio.ByteBuffer;

public class HdfSymbolTableEntry {
    private final HdfFixedPoint linkNameOffset;
    private final HdfFixedPoint objectHeaderAddress;
    private final int cacheType;
    private final HdfFixedPoint bTreeAddress;
    private final HdfFixedPoint localHeapAddress;

    public HdfSymbolTableEntry(
            HdfFixedPoint linkNameOffset,
            HdfFixedPoint objectHeaderAddress,
            int cacheType,
            HdfFixedPoint bTreeAddress,
            HdfFixedPoint localHeapAddress
    ) {
        this.linkNameOffset = linkNameOffset;
        this.objectHeaderAddress = objectHeaderAddress;
        this.cacheType = cacheType;
        this.bTreeAddress = bTreeAddress;
        this.localHeapAddress = localHeapAddress;
    }

    public static HdfSymbolTableEntry fromByteBuffer(ByteBuffer buffer, int offsetSize) {
        // Track bytes consumed manually
        int bytesConsumed = 0;

        // Read the fixed-point values
        HdfFixedPoint linkNameOffset = new HdfFixedPoint(buffer, offsetSize * 8, false);
        bytesConsumed += offsetSize;

        HdfFixedPoint objectHeaderAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);
        bytesConsumed += offsetSize;

        // Read the 4-byte cache type
        int cacheType = buffer.getInt();
        bytesConsumed += 4;

        // Skip the 4-byte reserved field
        buffer.getInt(); // No need to store
        bytesConsumed += 4;

        // Initialize addresses for cacheType 1
        HdfFixedPoint bTreeAddress = null;
        HdfFixedPoint localHeapAddress = null;

        if (cacheType == 1) {
            bTreeAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);
            bytesConsumed += offsetSize;

            localHeapAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);
            bytesConsumed += offsetSize;
        } else {
            // Skip 16 bytes for scratch-pad
            skipBytes(buffer, 16);
            bytesConsumed += 16;
        }

        return new HdfSymbolTableEntry(linkNameOffset, objectHeaderAddress, cacheType, bTreeAddress, localHeapAddress);
    }

    private static void skipBytes(ByteBuffer buffer, int count) {
        for (int i = 0; i < count; i++) {
            buffer.get(); // Consume bytes without changing buffer position
        }
    }

    public void writeToBuffer(ByteBuffer buffer, int offsetSize) {
        buffer.put(linkNameOffset.getHdfBytes(true));
        buffer.put(objectHeaderAddress.getHdfBytes(true));
        buffer.putInt(cacheType);

        if (cacheType == 1) {
            buffer.put(bTreeAddress != null ? bTreeAddress.getHdfBytes(true) : HdfFixedPoint.undefined(offsetSize * 8).getHdfBytes(true));
            buffer.put(localHeapAddress != null ? localHeapAddress.getHdfBytes(true) : HdfFixedPoint.undefined(offsetSize * 8).getHdfBytes(true));
        } else {
            buffer.put(new byte[16]); // Write placeholder bytes for scratch-pad
        }
    }

    public HdfFixedPoint getLinkNameOffset() {
        return linkNameOffset;
    }

    public HdfFixedPoint getObjectHeaderAddress() {
        return objectHeaderAddress;
    }

    public int getCacheType() {
        return cacheType;
    }

    public HdfFixedPoint getBTreeAddress() {
        return bTreeAddress;
    }

    public HdfFixedPoint getLocalHeapAddress() {
        return localHeapAddress;
    }

    @Override
    public String toString() {
        return "HdfSymbolTableEntry{" +
                "linkNameOffset=" + linkNameOffset.getBigIntegerValue() +
                ", objectHeaderAddress=" + objectHeaderAddress.getBigIntegerValue() +
                ", cacheType=" + cacheType +
                ", bTreeAddress=" + (bTreeAddress != null ? bTreeAddress.getBigIntegerValue() : "N/A") +
                ", localHeapAddress=" + (localHeapAddress != null ? localHeapAddress.getBigIntegerValue() : "N/A") +
                '}';
    }
}
