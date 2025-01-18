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
        this.size = determineSize(value);
        this.signed = value.signum() < 0;
        this.littleEndian = littleEndian;
        this.bytes = toSizedByteArray(value, this.size / 8, littleEndian);
    }

    // Constructor for ByteBuffer
    public HdfFixedPoint(ByteBuffer buffer, int size, boolean signed) {
        this(buffer, size, signed, true); // Default to little-endian
    }

    // Constructor for ByteBuffer with explicit endianness
    public HdfFixedPoint(ByteBuffer buffer, int size, boolean signed, boolean littleEndian) {
        validateSize(size);
        this.size = size;
        this.signed = signed;
        this.littleEndian = littleEndian;

        this.bytes = new byte[size / 8];
        buffer.get(this.bytes);

        // Adjust byte order if needed
        if (this.littleEndian && buffer.order() != java.nio.ByteOrder.LITTLE_ENDIAN) {
            reverseBytesInPlace(this.bytes);
        } else if (!this.littleEndian && buffer.order() == java.nio.ByteOrder.LITTLE_ENDIAN) {
            reverseBytesInPlace(this.bytes);
        }
    }

    // Factory method for undefined values
    public static HdfFixedPoint undefined(int size) {
        validateSize(size);
        byte[] undefinedBytes = new byte[size / 8];
        Arrays.fill(undefinedBytes, (byte) 0xFF);
        return new HdfFixedPoint(undefinedBytes, size, false, true);
    }

    // Private constructor for internal usage
    private HdfFixedPoint(byte[] bytes, int size, boolean signed, boolean littleEndian) {
        validateSize(size);
        this.bytes = Arrays.copyOf(bytes, bytes.length);
        this.size = size;
        this.signed = signed;
        this.littleEndian = littleEndian;
    }

    // Determine size based on bit length
    private int determineSize(BigInteger value) {
        int bitLength = value.bitLength();
        if (bitLength <= 8) return 8;
        if (bitLength <= 16) return 16;
        if (bitLength <= 32) return 32;
        if (bitLength <= 64) return 64;
        throw new IllegalArgumentException("BigInteger value exceeds 64 bits");
    }

    // Validate size
    private static void validateSize(int size) {
        if (size <= 0 || size % 8 != 0) {
            throw new IllegalArgumentException("Size must be a positive multiple of 8");
        }
    }

    // Convert BigInteger to a sized byte array
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

    // Check if the value is undefined
    public boolean isUndefined() {
        for (byte b : bytes) {
            if ((b & 0xFF) != 0xFF) {
                return false;
            }
        }
        return true;
    }

    // Get HDF5 data type
    public String getHdfType() {
        if (isUndefined()) {
            throw new IllegalStateException("FixedPoint undefined");
        }
        return switch (size) {
            case 8 -> signed ? "HDF5_SIGNED_BYTE" : "HDF5_UNSIGNED_BYTE";
            case 16 -> signed ? "HDF5_SIGNED_SHORT" : "HDF5_UNSIGNED_SHORT";
            case 32 -> signed ? "HDF5_SIGNED_INT" : "HDF5_UNSIGNED_INT";
            case 64 -> signed ? "HDF5_SIGNED_LONG" : "HDF5_UNSIGNED_LONG";
            default -> throw new IllegalStateException("Unsupported type");
        };
    }

    // Get BigInteger value
    public BigInteger getBigIntegerValue() {
        if (isUndefined()) {
            throw new IllegalStateException("FixedPoint undefined");
        }
        byte[] effectiveBytes = littleEndian ? reverseBytes(bytes) : bytes;

        return signed ? new BigInteger(effectiveBytes) : new BigInteger(1, effectiveBytes);
    }

    // Get bytes for HDF storage
    public byte[] getHdfBytes(boolean desiredLittleEndian) {
        if (desiredLittleEndian == littleEndian) {
            return Arrays.copyOf(bytes, bytes.length);
        }
        return reverseBytes(bytes);
    }

    // Reverse bytes for endianness
    private byte[] reverseBytes(byte[] input) {
        byte[] reversed = Arrays.copyOf(input, input.length);
        reverseBytesInPlace(reversed);
        return reversed;
    }

    // Reverse bytes in place
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

    // Get size in bits
    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return isUndefined() ? "\"Value undefined\"" : getBigIntegerValue().toString();
    }
}
