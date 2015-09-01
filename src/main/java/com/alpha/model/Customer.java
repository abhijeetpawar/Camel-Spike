package com.alpha.model;

import com.alpha.model.vo.Name;

public class Customer {
    private Name name;
    private int age;

    public Customer(Name name, int age) {
        this.name = name;
        this.age = age;
    }

    public Name getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
