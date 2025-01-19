package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

import java.nio.ByteBuffer;

public class HdfSymbolTableMessage {
    private final HdfFixedPoint bTreeAddress;
    private final HdfFixedPoint localHeapAddress;

    public HdfSymbolTableMessage(HdfFixedPoint bTreeAddress, HdfFixedPoint localHeapAddress) {
        this.bTreeAddress = bTreeAddress;
        this.localHeapAddress = localHeapAddress;
    }

    /**
     * Parses a Symbol Table Message from a ByteBuffer.
     *
     * @param buffer     The ByteBuffer containing the Symbol Table Message data.
     * @param offsetSize The size of offsets specified in the superblock (in bytes).
     * @return A parsed HdfSymbolTableMessage instance.
     */
    public static HdfSymbolTableMessage fromByteBuffer(ByteBuffer buffer, int offsetSize) {
        HdfFixedPoint bTreeAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);
        HdfFixedPoint localHeapAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);
        return new HdfSymbolTableMessage(bTreeAddress, localHeapAddress);
    }

    public HdfFixedPoint getBTreeAddress() {
        return bTreeAddress;
    }

    public HdfFixedPoint getLocalHeapAddress() {
        return localHeapAddress;
    }

    @Override
    public String toString() {
        return "HdfSymbolTableMessage{" +
                "bTreeAddress=" + bTreeAddress.getBigIntegerValue() +
                ", localHeapAddress=" + localHeapAddress.getBigIntegerValue() +
                '}';
    }
}
