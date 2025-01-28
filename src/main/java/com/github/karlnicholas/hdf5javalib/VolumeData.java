package com.github.karlnicholas.hdf5javalib;

import lombok.*;

import java.math.BigInteger;

@Getter
@ToString
public class VolumeData {
    private BigInteger Id;
    private String origCountry;
    private String origSlic;
    private BigInteger origSort;
    private String destCountry;
    private String destSlic;
    private BigInteger destIbi;
    private String destPostalCode;
    private String shipper;
    private BigInteger service;
    private BigInteger packageType;
    private BigInteger accessorials;
    private BigInteger pieces;
    private BigInteger weight;
    private BigInteger cube;
    private BigInteger committedTnt;
    private BigInteger committedDate;
}
