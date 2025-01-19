package com.github.karlnicholas.hdf5javalib;

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

        return result;
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
    public boolean hasMoreStrings() {
        return currentIndex < heapData.length;
    }
}
