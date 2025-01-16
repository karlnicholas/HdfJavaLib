package com.github.karlnicholas.hdf5javalib.dataobject;

import java.util.ArrayList;
import java.util.List;

public class DataObjectHeader {
    private final int version;
    private final int numberOfMessages;
    private final int referenceCount;
    private final int totalHeaderSize;
    private final List<HeaderMessage> headerMessages;

    public DataObjectHeader(int version, int numberOfMessages, int referenceCount, int totalHeaderSize) {
        this.version = version;
        this.numberOfMessages = numberOfMessages;
        this.referenceCount = referenceCount;
        this.totalHeaderSize = totalHeaderSize;
        this.headerMessages = new ArrayList<>();
    }

    public void addMessage(HeaderMessage message) {
        if (headerMessages.size() >= numberOfMessages) {
            throw new IllegalStateException("Cannot add more messages than specified in the header.");
        }
        headerMessages.add(message);
    }

    public int getVersion() {
        return version;
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public int getReferenceCount() {
        return referenceCount;
    }

    public int getTotalHeaderSize() {
        return totalHeaderSize;
    }

    public List<HeaderMessage> getHeaderMessages() {
        return headerMessages;
    }

    @Override
    public String toString() {
        return "DataObjectHeader{" +
                "version=" + version +
                ", numberOfMessages=" + numberOfMessages +
                ", referenceCount=" + referenceCount +
                ", totalHeaderSize=" + totalHeaderSize +
                ", headerMessages=" + headerMessages +
                '}';
    }
}
