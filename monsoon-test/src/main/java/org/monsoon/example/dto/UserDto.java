package org.monsoon.example.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private String role;
    private String status;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
