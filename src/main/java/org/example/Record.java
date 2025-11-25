package org.example;

import java.io.Serializable;
import java.io.StringReader;

public class Record implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private int age;
    private double height;

    public Record(int id, String name, int age, double height) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.height = height;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public double getHeight() { return height; }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setHeight(double height) { this.height = height; }

    @Override
    public String toString() {
        return "Record{" + "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", height=" + height +
                '}';
    }

}
