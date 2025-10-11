package org.monsoon.example.entities;

import lombok.*;
import org.monsoon.example.helper.UUIDGenerator;
import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.annotations.Entity;
import org.monsoon.framework.db.annotations.GeneratedId;
import org.monsoon.framework.db.annotations.Id;
import org.monsoon.framework.db.enums.GenerationType;

import java.time.LocalDateTime;

@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
@Entity(tableName = "users")
public class User {
    @Id
    @GeneratedId(strategy = GenerationType.CUSTOM, generator = UUIDGenerator.class)
    @Column(name = "id")
    private String id;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private String email;
    @Column
    private String phoneNumber;
    @Column
    private String password;
    @Column
    private String role;
    @Column
    private String status;
    @Column
    private String address;
    @Column
    private String city;
    @Column
    private String state;
    @Column
    private String country;
    @Column
    private String zipCode;
    @Column
    private LocalDateTime createdDateTime;
    @Column
    private LocalDateTime modifiedDateTime;
}
