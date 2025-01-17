package com.github.karlnicholas.hdf5javalib.numeric;

import java.math.BigInteger;
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

    private byte[] toSizedByteArray(BigInteger value, int byteSize, boolean littleEndian) {
        byte[] fullBytes = value.toByteArray();
        byte[] result = new byte[byteSize];

        // Copy the least significant bytes
        int copyLength = Math.min(fullBytes.length, byteSize);
        System.arraycopy(fullBytes, fullBytes.length - copyLength, result, byteSize - copyLength, copyLength);

        // Reverse for little-endian if needed
        if (littleEndian) {
            return reverseBytes(result);
        }
        return result;
    }

    public boolean isUndefined() {
        if (bytes == null || bytes.length == 0) {
            return false; // Consider an empty or null array as not all 0xFF
        }
        for (byte b : bytes) {
            if ((b & 0xFF) != 0xFF) { // Check each byte
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

    public HdfFixedPoint(byte[] bytes, int size, boolean signed) {
        this(bytes, size, signed, true); // Defaults to little-endian
    }

    public HdfFixedPoint(byte[] bytes, int size, boolean signed, boolean littleEndian) {
        if (bytes == null) {
            throw new IllegalArgumentException("Byte array cannot be null");
        }
        if (size <= 0 || size % 8 != 0) {
            throw new IllegalArgumentException("Size must be a positive multiple of 8");
        }
        if (bytes.length != size / 8) {
            throw new IllegalArgumentException(
                    "Byte array size does not match specified size. Expected: " + (size / 8) + ", Found: " + bytes.length
            );
        }
        this.bytes = Arrays.copyOf(bytes, bytes.length); // Defensive copy
        this.size = size;
        this.signed = signed;
        this.littleEndian = littleEndian;
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
        byte[] reversed = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            reversed[i] = input[input.length - i - 1];
        }
        return reversed;
    }

    @Override
    public String toString() {
        if (isUndefined()) {
            return "FixedPoint undefined";
        }
        return "HdfFixedPoint{" +
                "value=" + getBigIntegerValue() +
                ", size=" + size +
                ", signed=" + signed +
                ", littleEndian=" + littleEndian +
                ", bytes=" + Arrays.toString(bytes) +
                '}';
    }
}
