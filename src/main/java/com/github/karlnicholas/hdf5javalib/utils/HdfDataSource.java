package com.github.karlnicholas.hdf5javalib.utils;

import com.github.karlnicholas.hdf5javalib.datatype.CompoundDataType;
import com.github.karlnicholas.hdf5javalib.datatype.HdfFixedPoint;
import com.github.karlnicholas.hdf5javalib.datatype.HdfString;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HdfDataSource<T> {
    private final Class<T> clazz;
    private final Map<Field, CompoundDataType.Member> fieldToMemberMap = new HashMap<>();

    public HdfDataSource(CompoundDataType compoundDataType, Class<T> clazz) {
        this.clazz = clazz;

        // Parse fields and map them to CompoundDataType members
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            compoundDataType.getMembers().stream()
                    .filter(member -> member.getName().equals(field.getName()))
                    .findFirst()
                    .ifPresent(member -> fieldToMemberMap.put(field, member));
        }
    }

    /**
     * Populates a new instance of T with data from the buffer.
     */
    public T populateFromBuffer(ByteBuffer buffer) {
        try {
            // Create an instance of T
            T instance = clazz.getDeclaredConstructor().newInstance();

            // Populate fields using the pre-parsed map
            for (Map.Entry<Field, CompoundDataType.Member> entry : fieldToMemberMap.entrySet()) {
                Field field = entry.getKey();
                CompoundDataType.Member member = entry.getValue();

                buffer.position(member.getOffset());

                if (field.getType() == String.class && member.getType() instanceof CompoundDataType.StringMember) {
                    String value = ((CompoundDataType.StringMember) member.getType()).getInstance(buffer).getValue();
                    field.set(instance, value);
                } else if (field.getType() == BigInteger.class && member.getType() instanceof CompoundDataType.FixedPointMember) {
                    BigInteger value = ((CompoundDataType.FixedPointMember) member.getType()).getInstance(buffer).getBigIntegerValue();
                    field.set(instance, value);
                }
                // Add more type handling as needed
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Error creating and populating instance of " + clazz.getName(), e);
        }
    }
    /**
     * Writes the given instance of T into the provided ByteBuffer.
     */
    public void writeToBuffer(T instance, ByteBuffer buffer) {
        try {
            for (Map.Entry<Field, CompoundDataType.Member> entry : fieldToMemberMap.entrySet()) {
                Field field = entry.getKey();
                CompoundDataType.Member member = entry.getValue();

                // Move to the correct offset
                buffer.position(member.getOffset());

                Object value = field.get(instance);

                if (value instanceof String strValue && member.getType() instanceof CompoundDataType.StringMember stringMember) {
                    // Convert string to bytes and write to buffer
                    ByteBuffer stringBuffer = ByteBuffer.allocate(stringMember.getSize());
                    if (value != null) {
                        new HdfString(strValue.getBytes(StandardCharsets.US_ASCII))
                                .writeValueToByteBuffer(stringBuffer);
                    }
                    buffer.put(stringBuffer.array());
                } else if (value instanceof BigInteger bigIntValue && member.getType() instanceof CompoundDataType.FixedPointMember fixedPointMember) {
                    if (value == null) {
                        HdfFixedPoint.undefined(fixedPointMember.getSize()).writeValueToByteBuffer(buffer);
                    } else {
                        // Convert BigInteger to bytes and write to buffer
                        new HdfFixedPoint(bigIntValue, fixedPointMember.getSize(), fixedPointMember.isSigned(), fixedPointMember.isBigEndian())
                                .writeValueToByteBuffer(buffer);
                    }
                }
                // Add more type handling as needed

            }
        } catch (Exception e) {
            throw new RuntimeException("Error writing instance of " + clazz.getName() + " to ByteBuffer", e);
        }
    }
}
