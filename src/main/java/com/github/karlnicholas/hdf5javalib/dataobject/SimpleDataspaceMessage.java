package com.github.karlnicholas.hdf5javalib.dataobject;

import java.nio.ByteBuffer;

public class SimpleDataspaceMessage extends HeaderMessage {
    private final int dimensionality;
    private final long[] dimensionSizes;
    private final long[] maxDimensions;

    public SimpleDataspaceMessage(int dimensionality, long[] dimensionSizes, long[] maxDimensions) {
        super(0x0001, calculateSize(dimensionSizes, maxDimensions), 0);
        this.dimensionality = dimensionality;
        this.dimensionSizes = dimensionSizes.clone();
        this.maxDimensions = maxDimensions.clone();
    }

    private static int calculateSize(long[] dimensionSizes, long[] maxDimensions) {
        return 4 + (dimensionSizes.length * 8) + (maxDimensions.length * 8);
    }

    @Override
    public byte[] toFileData() {
        ByteBuffer buffer = ByteBuffer.allocate(getSize());
        buffer.putInt(dimensionality);
        for (long size : dimensionSizes) buffer.putLong(size);
        for (long max : maxDimensions) buffer.putLong(max);
        return buffer.array();
    }

    public int getDimensionality() {
        return dimensionality;
    }

    public long[] getDimensionSizes() {
        return dimensionSizes.clone();
    }

    public long[] getMaxDimensions() {
        return maxDimensions.clone();
    }

    @Override
    public String toString() {
        return "SimpleDataspaceMessage{" +
                "dimensionality=" + dimensionality +
                ", dimensionSizes=" + dimensionSizes +
                ", maxDimensions=" + maxDimensions +
                '}';
    }
}
