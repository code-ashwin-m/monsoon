package org.monsoon.test.dbtest;

import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.annotations.Entity;
import org.monsoon.framework.db.annotations.GeneratedId;
import org.monsoon.framework.db.annotations.Id;
import org.monsoon.framework.db.enums.GenerationType;

@Entity( tableName = "user")
public class TestUserEntity {
    @Id
    @Column( name = "id")
    @GeneratedId(strategy = GenerationType.CUSTOM, generator = UUIDGenerator.class)
    private String id;

    @Column( name = "firstName", uniqueCombo = true)
    private String firstName;

    @Column( name = "lastName", uniqueCombo = true)
    private String lastName;

    @Column( name = "email", unique = true)
    private String email;

    public TestUserEntity() {
    }

    public TestUserEntity(String id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "TestUserEntity{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
