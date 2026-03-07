package org.monsoon.sample;

import java.util.List;

public interface IUserService {
    void create(UserDto userDto);
    List<UserDto> getUsers();
}
