package com.github.karlnicholas.hdf5javalib.datatypes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HdfCompoundDatatype extends HdfDatatype {
    private final List<HdfCompoundField> fields;

    public HdfCompoundDatatype(int size) {
        super(size);
        this.fields = new ArrayList<>();
    }

    public void addComponent(String name, HdfDatatype datatype, int offset) {
        fields.add(new HdfCompoundField(name, datatype, offset));
    }

    public List<HdfCompoundField> getFields() {
        return fields;
    }

    @Override
    public Object readData(ByteBuffer buffer) {
        throw new UnsupportedOperationException("readData not implemented for HdfCompoundDatatype");
    }
}
