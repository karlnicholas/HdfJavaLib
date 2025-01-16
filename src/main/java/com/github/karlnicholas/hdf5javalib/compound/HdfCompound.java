package com.github.karlnicholas.hdf5javalib.compound;

import java.math.BigDecimal;

public class HdfCompound {

    private final String name;
    private final int age;
    private final BigDecimal salary;

    // Constructor
    public HdfCompound(String name, int age, BigDecimal salary) {
        this.name = name;
        this.age = age;
        this.salary = salary;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    @Override
    public String toString() {
        return String.format("Name: %s, Age: %d, Salary: %s", name, age, salary);
    }
}
