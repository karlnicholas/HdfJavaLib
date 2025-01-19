package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HdfBTreeV1 {
    private final String signature;
    private final int nodeType;
    private final int nodeLevel;
    private final int entriesUsed;
    private final HdfFixedPoint leftSiblingAddress;
    private final HdfFixedPoint rightSiblingAddress;
    private final List<HdfFixedPoint> childPointers;
    private final List<HdfFixedPoint> keys;

    public HdfBTreeV1(
            String signature,
            int nodeType,
            int nodeLevel,
            int entriesUsed,
            HdfFixedPoint leftSiblingAddress,
            HdfFixedPoint rightSiblingAddress,
            List<HdfFixedPoint> childPointers,
            List<HdfFixedPoint> keys
    ) {
        this.signature = signature;
        this.nodeType = nodeType;
        this.nodeLevel = nodeLevel;
        this.entriesUsed = entriesUsed;
        this.leftSiblingAddress = leftSiblingAddress;
        this.rightSiblingAddress = rightSiblingAddress;
        this.childPointers = childPointers;
        this.keys = keys;
    }

    public static HdfBTreeV1 fromByteBuffer(ByteBuffer buffer, HdfSuperblock superblock) {
        int offsetSize = superblock.getSizeOfOffsets();
        int lengthSize = superblock.getSizeOfLengths();

        // Read and verify the signature
        byte[] signatureBytes = new byte[4];
        buffer.get(signatureBytes);
        String signature = new String(signatureBytes);
        if (!"TREE".equals(signature)) {
            throw new IllegalArgumentException("Invalid B-tree node signature: " + signature);
        }

        // Read metadata fields
        int nodeType = Byte.toUnsignedInt(buffer.get());
        if (nodeType != 0) {
            throw new UnsupportedOperationException("Node type " + nodeType + " is not supported.");
        }

        int nodeLevel = Byte.toUnsignedInt(buffer.get());
        int entriesUsed = Short.toUnsignedInt(buffer.getShort());

        // Read sibling addresses
        HdfFixedPoint leftSiblingAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);
        HdfFixedPoint rightSiblingAddress = new HdfFixedPoint(buffer, offsetSize * 8, false);

        // Parse keys and child pointers
        List<HdfFixedPoint> keys = new ArrayList<>();
        List<HdfFixedPoint> childPointers = new ArrayList<>();

        for (int i = 0; i < entriesUsed; i++) {
            keys.add(new HdfFixedPoint(buffer, lengthSize * 8, false));
            childPointers.add(new HdfFixedPoint(buffer, offsetSize * 8, false));
        }

        // Read the final key
        keys.add(new HdfFixedPoint(buffer, lengthSize * 8, false));

        return new HdfBTreeV1(
                signature,
                nodeType,
                nodeLevel,
                entriesUsed,
                leftSiblingAddress,
                rightSiblingAddress,
                childPointers,
                keys
        );
    }

    @Override
    public String toString() {
        return "HdfBTreeV1{" +
                "signature='" + signature + '\'' +
                ", nodeType=" + nodeType +
                ", nodeLevel=" + nodeLevel +
                ", entriesUsed=" + entriesUsed +
                ", leftSiblingAddress=" + leftSiblingAddress +
                ", rightSiblingAddress=" + rightSiblingAddress +
                ", childPointers=" + childPointers +
                ", keys=" + keys +
                '}';
    }
}
