package org.monsoon.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommentDto {
    private int userId;
    private int id;
    private String name;
    private String email;
    private String body;
    public CommentDto() {}

    @Override
    public String toString() {
        return "CommentDto{" +
                "userId=" + userId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
