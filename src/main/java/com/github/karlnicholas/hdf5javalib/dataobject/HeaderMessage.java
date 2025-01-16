package com.github.karlnicholas.hdf5javalib.dataobject;

public abstract class HeaderMessage {
    private final int type;
    private final int size;
    private final int flags;

    public HeaderMessage(int type, int size, int flags) {
        this.type = type;
        this.size = size;
        this.flags = flags;
    }

    public int getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public int getFlags() {
        return flags;
    }

    public abstract byte[] toFileData();

    @Override
    public String toString() {
        return "HeaderMessage{" +
                "type=" + type +
                ", size=" + size +
                ", flags=" + flags +
                '}';
    }
}
