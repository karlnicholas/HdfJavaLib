package org.hdf5javalib.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class HdfReadUtils {
    public static int readIntFromFileChannel(FileChannel fileChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Assume little-endian as per HDF5 spec
        fileChannel.read(buffer);
        buffer.flip();
        return buffer.getInt();
    }

    public static void skipBytes(FileChannel fileChannel, int bytesToSkip) throws IOException {
        fileChannel.position(fileChannel.position() + bytesToSkip);
    }

    public static void reverseBytesInPlace(byte[] input) {
        int i = 0, j = input.length - 1;
        while (i < j) {
            byte temp = input[i];
            input[i] = input[j];
            input[j] = temp;
            i++;
            j--;
        }
    }
}
