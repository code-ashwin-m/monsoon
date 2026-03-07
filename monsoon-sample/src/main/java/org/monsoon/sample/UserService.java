package org.monsoon.sample;

import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Service;
import org.monsoon.framework.db.annotations.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserService {
    private UserRepo userRepo;

    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
        userRepo.createTableIfNotExists();
    }


    @Transactional
    public void create(UserDto userDto) {
        User user = GenericMapper.map(userDto, User.class);
        userRepo.create(user);
    }

    public List<UserDto> getUsers() {
        List<User> users = userRepo.findAll();

        List<UserDto> userDtos = new ArrayList<>();

        for (User u: users){
            userDtos.add(GenericMapper.map(u, UserDto.class));
        }

        return userDtos;
    }
}
