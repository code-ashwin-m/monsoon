package org.monsoon.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClientDto {
    private int userId;
    private int id;
    private String title;
    private boolean completed;
    public ClientDto() {}

    @Override
    public String toString() {
        return "ClientDto{" +
                "userId='" + userId + '\'' +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                '}';
    }
}
