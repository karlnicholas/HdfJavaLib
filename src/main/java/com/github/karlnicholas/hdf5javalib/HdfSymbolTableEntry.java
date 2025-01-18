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
        HdfFixedPoint linkNameOffset = new HdfFixedPoint(buffer, offsetSize * 8, false);
        HdfFixedPoint objectHeaderAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);

        int cacheType = Byte.toUnsignedInt(buffer.get());
        buffer.get(); // Reserved byte

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
