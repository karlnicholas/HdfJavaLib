package com.github.karlnicholas.hdf5javalib.util;

import com.github.karlnicholas.hdf5javalib.numeric.HdfFixedPoint;
import com.github.karlnicholas.hdf5javalib.util.HdfFixedPointFactory.HdfType;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class HdfFixedPointFactoryTest {

    @Test
    public void testSignedByte() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.SIGNED_BYTE, -5, true);
        assertEquals(new BigInteger("-5"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testUnsignedByte() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.UNSIGNED_BYTE, 255, false);
        assertEquals(new BigInteger("255"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testSignedShort() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.SIGNED_SHORT, -32768, true);
        assertEquals(new BigInteger("-32768"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testUnsignedShort() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.UNSIGNED_SHORT, 65535, true);
        assertEquals(new BigInteger("65535"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testSignedInt() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.SIGNED_INT, -2147483648, false);
        assertEquals(new BigInteger("-2147483648"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testUnsignedInt() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.UNSIGNED_INT, 4294967295L, false);
        assertEquals(new BigInteger("4294967295"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testSignedLong() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.SIGNED_LONG, -9223372036854775808L, true);
        assertEquals(new BigInteger("-9223372036854775808"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testUnsignedLong() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.UNSIGNED_LONG, new BigInteger("18446744073709551615"), true);
        assertEquals(new BigInteger("18446744073709551615"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testInvalidValueForSignedByte() {
        assertThrows(IllegalArgumentException.class, () -> {
            HdfFixedPointFactory.createFixedPoint(HdfType.SIGNED_BYTE, 128, false);
        });
    }

    @Test
    public void testInvalidValueForUnsignedByte() {
        assertThrows(IllegalArgumentException.class, () -> {
            HdfFixedPointFactory.createFixedPoint(HdfType.UNSIGNED_BYTE, -1, true);
        });
    }

    @Test
    public void testInvalidValueForSignedInt() {
        assertThrows(IllegalArgumentException.class, () -> {
            HdfFixedPointFactory.createFixedPoint(HdfType.SIGNED_INT, new BigInteger("2147483648"), true);
        });
    }

    @Test
    public void testInvalidValueForUnsignedLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            HdfFixedPointFactory.createFixedPoint(HdfType.UNSIGNED_LONG, new BigInteger("-1"), true);
        });
    }

    @Test
    public void testLittleEndianBytes() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.SIGNED_INT, 123456, true);
        byte[] hdfBytes = fixedPoint.getHdfBytes(true); // Request little-endian bytes
        byte[] expected = new byte[]{0x40, (byte) 0xE2, 0x01, 0x00}; // Little-endian representation of 123456
        assertArrayEquals(expected, hdfBytes);
    }

    @Test
    public void testBigEndianBytes() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.SIGNED_INT, 123456, false);
        byte[] hdfBytes = fixedPoint.getHdfBytes(false); // Request big-endian bytes
        byte[] expected = new byte[]{0x00, 0x01, (byte) 0xE2, 0x40}; // Big-endian representation of 123456
        assertArrayEquals(expected, hdfBytes);
    }

    @Test
    public void testSwitchLittleToBigEndianBytes() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.SIGNED_INT, 123456, true);
        byte[] hdfBytes = fixedPoint.getHdfBytes(false); // Request big-endian bytes
        byte[] expected = new byte[]{0x00, 0x01, (byte) 0xE2, 0x40}; // Big-endian representation of 123456
        assertArrayEquals(expected, hdfBytes);
    }

    @Test
    public void testSwitchBigToLittleEndianBytes() {
        HdfFixedPoint fixedPoint = HdfFixedPointFactory.createFixedPoint(HdfType.SIGNED_INT, 123456, false);
        byte[] hdfBytes = fixedPoint.getHdfBytes(true); // Request little-endian bytes
        byte[] expected = new byte[]{0x40, (byte) 0xE2, 0x01, 0x00}; // Little-endian representation of 123456
        assertArrayEquals(expected, hdfBytes);
    }
}
