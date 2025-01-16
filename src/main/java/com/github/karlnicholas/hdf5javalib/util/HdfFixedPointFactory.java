package com.github.karlnicholas.hdf5javalib.util;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;

import java.math.BigInteger;

public class HdfFixedPointFactory {

    // HDF Types Enum
    public enum HdfType {
        SIGNED_BYTE(8, true),
        UNSIGNED_BYTE(8, false),
        SIGNED_SHORT(16, true),
        UNSIGNED_SHORT(16, false),
        SIGNED_INT(32, true),
        UNSIGNED_INT(32, false),
        SIGNED_LONG(64, true),
        UNSIGNED_LONG(64, false);

        private final int size;     // Bit size
        private final boolean signed;

        HdfType(int size, boolean signed) {
            this.size = size;
            this.signed = signed;
        }

        public int getSize() {
            return size;
        }

        public boolean isSigned() {
            return signed;
        }
    }

    /**
     * Constructs an HdfFixedPoint instance based on the specified HdfType and numeric value.
     *
     * @param type  The HDF type (e.g., SIGNED_INT, UNSIGNED_SHORT).
     * @param value The numeric value to store.
     * @param littleEndian Whether to use little-endian byte ordering.
     * @return A new HdfFixedPoint instance.
     */
    public static HdfFixedPoint createFixedPoint(HdfType type, Number value, boolean littleEndian) {
        BigInteger bigValue = convertToBigInteger(value);

        // Validate the value against the type's signedness and size
        validateValueFitsType(type, bigValue);

        // Convert BigInteger to byte array of appropriate size
        byte[] bytes = toSizedByteArray(bigValue, type.getSize() / 8, littleEndian);

        return new HdfFixedPoint(bytes, type.getSize(), type.isSigned(), littleEndian);
    }

    // Helper: Convert a numeric value to BigInteger
    private static BigInteger convertToBigInteger(Number value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        } else if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return BigInteger.valueOf(value.longValue());
        } else {
            throw new IllegalArgumentException("Unsupported numeric type: " + value.getClass().getName());
        }
    }

    // Helper: Validate that the value fits within the HDF type's constraints
    private static void validateValueFitsType(HdfType type, BigInteger value) {
        int bitLength = type.getSize();
        if (type.isSigned()) {
            BigInteger min = BigInteger.ONE.shiftLeft(bitLength - 1).negate(); // -2^(n-1)
            BigInteger max = BigInteger.ONE.shiftLeft(bitLength - 1).subtract(BigInteger.ONE); // 2^(n-1) - 1
            if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                throw new IllegalArgumentException("Value " + value + " exceeds range for " + type);
            }
        } else {
            BigInteger max = BigInteger.ONE.shiftLeft(bitLength).subtract(BigInteger.ONE); // 2^n - 1
            if (value.signum() < 0 || value.compareTo(max) > 0) {
                throw new IllegalArgumentException("Value " + value + " exceeds range for " + type);
            }
        }
    }

    // Helper: Convert BigInteger to a byte array of the specified size, applying little-endian if needed
    private static byte[] toSizedByteArray(BigInteger value, int byteSize, boolean littleEndian) {
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

    // Helper: Reverse byte array for little-endian storage
    private static byte[] reverseBytes(byte[] input) {
        byte[] reversed = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            reversed[i] = input[input.length - i - 1];
        }
        return reversed;
    }
}
