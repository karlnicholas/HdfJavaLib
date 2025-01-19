package com.github.karlnicholas.hdf5javalib.datatypes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HdfCompoundDatatype {
    private final List<HdfDatatype> components = new ArrayList<>();
    private final List<String> fieldNames = new ArrayList<>();

    public void addComponent(String fieldName, HdfDatatype datatype) {
        fieldNames.add(fieldName);
        components.add(datatype);
    }

    public List<Object> readData(ByteBuffer buffer) {
        List<Object> values = new ArrayList<>();
        for (HdfDatatype component : components) {
            values.add(component.readData(buffer));
        }
        return values;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public List<HdfDatatype> getComponents() {
        return components;
    }
}
