package com.github.karlnicholas.hdf5javalib.numeric;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TestHdfFixedPoint {

    @Test
    public void testSigned32BitBigEndian() {
        byte[] bytes = new byte[]{0, 1, 0, 0};
        HdfFixedPoint fixedPoint = new HdfFixedPoint(bytes, 32, true, false);
        assertEquals(new BigInteger("65536"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testSigned32BitLittleEndian() {
        byte[] bytes = new byte[]{0, 0, 1, 0}; // Little-endian representation of 65536
        HdfFixedPoint fixedPoint = new HdfFixedPoint(bytes, 32, true, true);
        assertEquals(new BigInteger("65536"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testNegativeSigned32BitLittleEndian() {
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}; // -1 in little-endian
        HdfFixedPoint fixedPoint = new HdfFixedPoint(bytes, 32, true, true);
        assertEquals(new BigInteger("-1"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testUnsigned32BitBigEndian() {
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF};
        HdfFixedPoint fixedPoint = new HdfFixedPoint(bytes, 32, false, false);
        assertEquals(new BigInteger("4294901759"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testUnsigned32BitLittleEndian() {
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0xFF}; // Little-endian representation of 4294901759
        HdfFixedPoint fixedPoint = new HdfFixedPoint(bytes, 32, false, true);
        assertEquals(new BigInteger("4294901759"), fixedPoint.getBigIntegerValue());
    }

    @Test
    public void testDefaultConstructorLittleEndian() {
        byte[] bytes = new byte[]{0, 0, 1, 0}; // Little-endian representation of 65536
        HdfFixedPoint fixedPoint = new HdfFixedPoint(bytes, 32, true);
        assertEquals(new BigInteger("65536"), fixedPoint.getBigIntegerValue(),
                "Default constructor should use little-endian.");
    }

    @Test
    public void testGetHdfBytesBigEndian() {
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF}; // Big-endian
        HdfFixedPoint fixedPoint = new HdfFixedPoint(bytes, 32, false, false);
        byte[] hdfBytes = fixedPoint.getHdfBytes(false); // Request big-endian bytes
        assertArrayEquals(bytes, hdfBytes, "HDF bytes for big-endian should match input bytes.");
    }

    @Test
    public void testGetHdfBytesLittleEndian() {
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0xFF}; // Little-endian
        HdfFixedPoint fixedPoint = new HdfFixedPoint(bytes, 32, false, true);
        byte[] hdfBytes = fixedPoint.getHdfBytes(true); // Request little-endian bytes
        assertArrayEquals(bytes, hdfBytes, "HDF bytes for little-endian should match input bytes.");
    }

    @Test
    public void testGetHdfBytesSwitchToBigEndian() {
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0xFF}; // Little-endian input
        HdfFixedPoint fixedPoint = new HdfFixedPoint(bytes, 32, false, true);
        byte[] hdfBytes = fixedPoint.getHdfBytes(false); // Request big-endian bytes
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF}, hdfBytes,
                "HDF bytes should be reversed for big-endian output.");
    }

    @Test
    public void testGetHdfBytesSwitchToLittleEndian() {
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF}; // Big-endian input
        HdfFixedPoint fixedPoint = new HdfFixedPoint(bytes, 32, false, false);
        byte[] hdfBytes = fixedPoint.getHdfBytes(true); // Request little-endian bytes
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0xFF}, hdfBytes,
                "HDF bytes should be reversed for little-endian output.");
    }

    @Test
    public void testBigIntegerConstructorLittleEndian() {
        HdfFixedPoint fixedPoint = new HdfFixedPoint(BigInteger.valueOf(123456), true);
        assertEquals(new BigInteger("123456"), fixedPoint.getBigIntegerValue(),
                "BigInteger constructor should initialize correctly.");
        byte[] expectedLittleEndian = new byte[]{0x40, (byte) 0xE2, 0x01, 0x00}; // Little-endian bytes
        assertArrayEquals(expectedLittleEndian, fixedPoint.getHdfBytes(true),
                "Little-endian bytes should match expected representation.");
    }

    @Test
    public void testBigIntegerConstructorBigEndian() {
        HdfFixedPoint fixedPoint = new HdfFixedPoint(BigInteger.valueOf(123456), false);
        assertEquals(new BigInteger("123456"), fixedPoint.getBigIntegerValue(),
                "BigInteger constructor should initialize correctly.");
        byte[] expectedBigEndian = new byte[]{0x00, 0x01, (byte) 0xE2, 0x40}; // Big-endian bytes
        assertArrayEquals(expectedBigEndian, fixedPoint.getHdfBytes(false),
                "Big-endian bytes should match expected representation.");
    }
}
