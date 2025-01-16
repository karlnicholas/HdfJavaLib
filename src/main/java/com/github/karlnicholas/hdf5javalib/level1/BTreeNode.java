package com.github.karlnicholas.hdf5javalib.level1;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BTreeNode {
    private static final String NODE_SIGNATURE = "TREE";
    private final int nodeType;
    private final int nodeLevel;
    private final long leftSibling; // Added missing field
    private final long rightSibling; // Added missing field
    private final List<KeyChildPair> entries;

    // Constructor for creating a new node
    public BTreeNode(int nodeType, int nodeLevel, long leftSibling, long rightSibling) {
        this.nodeType = nodeType;
        this.nodeLevel = nodeLevel;
        this.leftSibling = leftSibling;
        this.rightSibling = rightSibling;
        this.entries = new ArrayList<>();
    }

    // Constructor for reading from ByteBuffer
    public BTreeNode(ByteBuffer buffer) {
        byte[] signature = new byte[4];
        buffer.get(signature);
        if (!NODE_SIGNATURE.equals(new String(signature))) {
            throw new IllegalArgumentException("Invalid B-tree node signature");
        }

        this.nodeType = Byte.toUnsignedInt(buffer.get());
        this.nodeLevel = Byte.toUnsignedInt(buffer.get());
        int entriesUsed = Short.toUnsignedInt(buffer.getShort());
        this.leftSibling = buffer.getLong(); // Read left sibling
        this.rightSibling = buffer.getLong(); // Read right sibling

        this.entries = new ArrayList<>();
        for (int i = 0; i < entriesUsed; i++) {
            entries.add(new KeyChildPair(buffer));
        }
    }

    public void addEntry(KeyChildPair entry) {
        entries.add(entry);
    }

    public byte[] toHdfBytes() {
        int totalSize = 16 + entries.stream().mapToInt(KeyChildPair::calculateSize).sum();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.put(NODE_SIGNATURE.getBytes());
        buffer.put((byte) nodeType);
        buffer.put((byte) nodeLevel);
        buffer.putShort((short) entries.size());
        buffer.putLong(leftSibling); // Include left sibling
        buffer.putLong(rightSibling); // Include right sibling

        for (KeyChildPair entry : entries) {
            buffer.put(entry.toHdfBytes());
        }
        return buffer.array();
    }

    public static class KeyChildPair {
        private final byte[] key; // Variable size
        private final long childPointer;

        public KeyChildPair(byte[] key, long childPointer) {
            this.key = key;
            this.childPointer = childPointer;
        }

        public KeyChildPair(ByteBuffer buffer) {
            int keySize = buffer.getInt();
            this.key = new byte[keySize];
            buffer.get(this.key);
            this.childPointer = buffer.getLong();
        }

        public byte[] toHdfBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(4 + key.length + 8);
            buffer.putInt(key.length);
            buffer.put(key);
            buffer.putLong(childPointer);
            return buffer.array();
        }

        public int calculateSize() {
            return 4 + key.length + 8;
        }
    }

    @Override
    public String toString() {
        return "BTreeNode{" +
                "nodeType=" + nodeType +
                ", nodeLevel=" + nodeLevel +
                ", leftSibling=" + leftSibling +
                ", rightSibling=" + rightSibling +
                ", entries=" + entries +
                '}';
    }
}
