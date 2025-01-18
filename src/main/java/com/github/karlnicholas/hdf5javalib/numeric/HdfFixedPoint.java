package com.github.karlnicholas.hdf5javalib.numeric;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class HdfFixedPoint {
    private final byte[] bytes;
    private final int size;
    private final boolean signed;
    private final boolean littleEndian;

    // Constructor for BigInteger
    public HdfFixedPoint(BigInteger value, boolean littleEndian) {
        this.littleEndian = littleEndian;
        this.signed = value.signum() < 0; // Negative BigInteger indicates a signed type

        // Determine the size based on the bit length
        int bitLength = value.bitLength();
        if (bitLength <= 8) {
            this.size = 8;
        } else if (bitLength <= 16) {
            this.size = 16;
        } else if (bitLength <= 32) {
            this.size = 32;
        } else if (bitLength <= 64) {
            this.size = 64;
        } else {
            throw new IllegalArgumentException("BigInteger value exceeds 64 bits");
        }

        // Convert BigInteger to byte array
        this.bytes = toSizedByteArray(value, this.size / 8, littleEndian);
    }

    // Constructor for ByteBuffer
    public HdfFixedPoint(ByteBuffer buffer, int size, boolean signed) {
        this.size = size;
        this.signed = signed;
        this.littleEndian = true; // Default to little-endian for HDF5

        if (size <= 0 || size % 8 != 0) {
            throw new IllegalArgumentException("Size must be a positive multiple of 8");
        }

        this.bytes = new byte[size / 8];
        buffer.get(this.bytes);

        // Adjust byte order if needed
        if (!buffer.order().equals(java.nio.ByteOrder.LITTLE_ENDIAN)) {
            reverseBytesInPlace(this.bytes);
        }
    }

    // Constructor for ByteBuffer with explicit endianness
    public HdfFixedPoint(ByteBuffer buffer, int size, boolean signed, boolean littleEndian) {
        this.size = size;
        this.signed = signed;
        this.littleEndian = littleEndian;

        if (size <= 0 || size % 8 != 0) {
            throw new IllegalArgumentException("Size must be a positive multiple of 8");
        }

        this.bytes = new byte[size / 8];
        buffer.get(this.bytes);

        // Adjust byte order if specified endianness doesn't match buffer's order
        if (littleEndian && !buffer.order().equals(java.nio.ByteOrder.LITTLE_ENDIAN)) {
            reverseBytesInPlace(this.bytes);
        } else if (!littleEndian && buffer.order().equals(java.nio.ByteOrder.LITTLE_ENDIAN)) {
            reverseBytesInPlace(this.bytes);
        }
    }

    private byte[] toSizedByteArray(BigInteger value, int byteSize, boolean littleEndian) {
        byte[] fullBytes = value.toByteArray();
        byte[] result = new byte[byteSize];

        // Copy the least significant bytes
        int copyLength = Math.min(fullBytes.length, byteSize);
        System.arraycopy(fullBytes, fullBytes.length - copyLength, result, byteSize - copyLength, copyLength);

        // Reverse for little-endian if needed
        if (littleEndian) {
            reverseBytesInPlace(result);
        }
        return result;
    }

    public boolean isUndefined() {
        for (byte b : bytes) {
            if ((b & 0xFF) != 0xFF) {
                return false;
            }
        }
        return true;
    }

    public String getHdfType() {
        if (isUndefined()) {
            throw new IllegalStateException("FixedPoint undefined");
        }
        if (signed) {
            switch (size) {
                case 8: return "HDF5_SIGNED_BYTE";
                case 16: return "HDF5_SIGNED_SHORT";
                case 32: return "HDF5_SIGNED_INT";
                case 64: return "HDF5_SIGNED_LONG";
            }
        } else {
            switch (size) {
                case 8: return "HDF5_UNSIGNED_BYTE";
                case 16: return "HDF5_UNSIGNED_SHORT";
                case 32: return "HDF5_UNSIGNED_INT";
                case 64: return "HDF5_UNSIGNED_LONG";
            }
        }
        throw new IllegalStateException("Unsupported type");
    }

    public BigInteger getBigIntegerValue() {
        if (isUndefined()) {
            throw new IllegalStateException("FixedPoint undefined");
        }
        byte[] effectiveBytes = littleEndian ? reverseBytes(bytes) : bytes;

        if (signed) {
            return new BigInteger(effectiveBytes); // Handles signed values
        } else {
            return new BigInteger(1, effectiveBytes); // Unsigned BigInteger
        }
    }

    public byte[] getHdfBytes(boolean desiredLittleEndian) {
        if (desiredLittleEndian == littleEndian) {
            return Arrays.copyOf(bytes, bytes.length);
        }
        return reverseBytes(bytes);
    }

    private byte[] reverseBytes(byte[] input) {
        byte[] reversed = Arrays.copyOf(input, input.length);
        reverseBytesInPlace(reversed);
        return reversed;
    }

    private void reverseBytesInPlace(byte[] input) {
        int i = 0, j = input.length - 1;
        while (i < j) {
            byte temp = input[i];
            input[i] = input[j];
            input[j] = temp;
            i++;
            j--;
        }
    }

    /**
     * Returns the size in bits of this HdfFixedPoint instance.
     *
     * @return The size in bits.
     */
    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        if (isUndefined()) {
            return "\"Value undefined\"";
        }
        return getBigIntegerValue().toString();
    }
}
