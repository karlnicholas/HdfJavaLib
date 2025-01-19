package com.github.karlnicholas.hdf5javalib;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

public class HdfLocalHeapContents {
    private final byte[] heapData;
    private int currentIndex;

    public HdfLocalHeapContents(byte[] heapData) {
        this.heapData = heapData;
        this.currentIndex = 0; // Start from the beginning of the heap data
    }

    /**
     * Parses the next null-terminated string from the heap data.
     *
     * @return The next string, or null if no more strings are available.
     */
    public String parseNextString() {
        if (currentIndex >= heapData.length) {
            return null; // End of heap data
        }

        int start = currentIndex;

        // Find the null terminator
        while (currentIndex < heapData.length && heapData[currentIndex] != 0) {
            currentIndex++;
        }

        // Extract the string
        String result = new String(heapData, start, currentIndex - start);

        // Move past the null terminator
        currentIndex++;

        // Align to the next 8-byte boundary
        alignTo8ByteBoundary();

        return result;
    }

    /**
     * Parses the next 64-bit fixed-point value from the heap data.
     *
     * @return The next fixed-point value, or null if no more data is available.
     */
    public HdfFixedPoint parseNextFixedPoint() {
        int fixedPointSize = 8; // Fixed-point size in bytes for 64-bit values
        if (currentIndex + fixedPointSize > heapData.length) {
            return null; // Not enough data remaining
        }

        // Create a fixed-point object from the current position
        HdfFixedPoint fixedPoint = new HdfFixedPoint(
                java.nio.ByteBuffer.wrap(heapData, currentIndex, fixedPointSize).order(java.nio.ByteOrder.LITTLE_ENDIAN),
                fixedPointSize * 8,
                false
        );

        // Advance the index by the size of the fixed-point value
        currentIndex += fixedPointSize;

        return fixedPoint;
    }

    /**
     * Resets the internal index to allow re-parsing from the beginning.
     */
    public void reset() {
        currentIndex = 0;
    }

    /**
     * Checks if there is more data to parse in the heap.
     *
     * @return True if more data is available, false otherwise.
     */
    public boolean hasMoreData() {
        return currentIndex < heapData.length;
    }

    /**
     * Aligns the current index to the next 8-byte boundary.
     */
    private void alignTo8ByteBoundary() {
        int padding = (8 - (currentIndex % 8)) % 8; // Calculate required padding
        currentIndex += padding; // Skip padding bytes
    }
}
