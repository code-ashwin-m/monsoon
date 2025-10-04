package org.ashwin.monsoon.db.test1;

import org.ashwin.monsoon.db.annotations.Column;
import org.ashwin.monsoon.db.annotations.Entity;
import org.ashwin.monsoon.db.annotations.Id;

@Entity( tableName = "testTable")
public class TestEntity {
    @Id
    @Column(name = "id", unique = true)
    private int id;

    @Column(name = "name", unique = true, width = 25)
    private String name;

    @Column(name = "email")
    private String email;
}
