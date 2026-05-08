package org.monsoon.framework.db.actions;

import org.monsoon.framework.db.annotations.*;
import org.monsoon.framework.db.enums.GenerationType;

@Entity(tableName = "test_entities")
public class TestEntity {

    @Id
    @GeneratedId(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "name", notNull = true, defaultValue = "John Doe", width = 255)
    private String name;

    @Column(name = "age", unique = true)
    private int age;

    @Column(name = "active", defaultValue = "false")
    private boolean active;

    @Column(name = "salary", defaultValue = "0.0")
    private double salary;

    @Column(name = "big_value")
    private long bigValue;

    @Column(name = "combo1", uniqueCombo = true)
    private String combo1;

    @Column(name = "combo2", uniqueCombo = true)
    private String combo2;

    @Column(name = "parent_id", foreign = ParentEntity.class, cascadeDelete = true)
    private int parentId;

    // Getters and setters (not strictly needed for SQL generation but good
    // practice)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public long getBigValue() {
        return bigValue;
    }

    public void setBigValue(long bigValue) {
        this.bigValue = bigValue;
    }

    public String getCombo1() {
        return combo1;
    }

    public void setCombo1(String combo1) {
        this.combo1 = combo1;
    }

    public String getCombo2() {
        return combo2;
    }

    public void setCombo2(String combo2) {
        this.combo2 = combo2;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
}

@Entity(tableName = "parent_entities")
class ParentEntity {
    @Id
    @Column(name = "id")
    private int id;
}

@Entity(tableName = "table'name")
class SingleQuoteTableEntity {
    @Column(name = "id")
    private int id;
}
