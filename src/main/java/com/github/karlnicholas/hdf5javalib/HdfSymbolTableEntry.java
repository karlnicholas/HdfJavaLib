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
        // Read the fixed-point values
        HdfFixedPoint linkNameOffset = new HdfFixedPoint(buffer, offsetSize * 8, false);
        HdfFixedPoint objectHeaderAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);

        // Read the 4-byte cache type
        int cacheType = buffer.getInt();

        // Skip the 4-byte reserved field
        buffer.getInt();

        // Initialize addresses for cacheType 1
        HdfFixedPoint bTreeAddress = null;
        HdfFixedPoint localHeapAddress = null;

        if (cacheType == 1) {
            bTreeAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);
            localHeapAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);
        } else {
            buffer.position(buffer.position() + 16); // Skip scratch-pad if not cacheType = 1
        }

        return new HdfSymbolTableEntry(linkNameOffset, objectHeaderAddress, cacheType, bTreeAddress, localHeapAddress);
    }

    public void writeToBuffer(ByteBuffer buffer, int offsetSize) {
        buffer.put(linkNameOffset.getHdfBytes(true));
        buffer.put(objectHeaderAddress.getHdfBytes(true));
        buffer.put((byte) cacheType);
        buffer.put((byte) 0); // Reserved byte

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
