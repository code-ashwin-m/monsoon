package org.monsoon.example.services;

import org.monsoon.example.dto.UserDto;
import org.monsoon.example.entities.User;
import org.monsoon.example.helper.GenericMapper;
import org.monsoon.example.repo.UserRepo;
import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Service;
import org.monsoon.framework.core.annotations.Singleton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Singleton
public class UserService {
    private UserRepo userRepo;

    @Autowired
    public UserService(UserRepo userRepo){
        this.userRepo = userRepo;
        userRepo.createTableIfNotExists();
    }

    public Boolean createUser(UserDto userDto) {
        User user = GenericMapper.map(userDto, User.class);
        user.setCreatedDateTime(LocalDateTime.now());
        user.setModifiedDateTime(LocalDateTime.now());
        userRepo.create(user);
        return true;
    }

    public List<UserDto> findAll() {
        List<User> users = userRepo.findAll();
        List<UserDto> userDtos = new ArrayList<>();
        for (User user: users){
            userDtos.add(GenericMapper.map(user, UserDto.class));
        }
        return userDtos;
    }
}
