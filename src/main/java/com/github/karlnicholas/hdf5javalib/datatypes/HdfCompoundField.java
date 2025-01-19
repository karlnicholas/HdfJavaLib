package com.github.karlnicholas.hdf5javalib.datatypes;

public class HdfCompoundField {
    private final String name;
    private final HdfDatatype datatype;
    private final int offset;

    public HdfCompoundField(String name, HdfDatatype datatype, int offset) {
        this.name = name;
        this.datatype = datatype;
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public HdfDatatype getDatatype() {
        return datatype;
    }

    public int getOffset() {
        return offset;
    }
}
