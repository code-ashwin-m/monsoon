package org.monsoon.sample;

import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.annotations.Entity;
import org.monsoon.framework.db.annotations.GeneratedId;
import org.monsoon.framework.db.annotations.Id;
import org.monsoon.framework.db.enums.GenerationType;


@Entity(tableName = "user")
public class User {
    @Id
    @GeneratedId(strategy = GenerationType.CUSTOM, generator = UUIDGenerator.class)
    @Column(name = "id")
    private String id;

    @Column
    private String name;

    @Column
    private int age;

    public User() {
    }

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
