package org.monsoon.example.services;

import org.monsoon.example.dto.ClientDto;
import org.monsoon.example.dto.CommentDto;
import org.monsoon.example.dto.UserDto;
import org.monsoon.example.entities.User;
import org.monsoon.example.helper.GenericMapper;
import org.monsoon.example.http.UserClient;
import org.monsoon.example.repo.UserRepo;
import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Service;
import org.monsoon.framework.core.annotations.Singleton;
import org.monsoon.framework.web.HttpServiceProxy;
import org.monsoon.framework.web.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Singleton
public class UserService {
    private UserRepo userRepo;
    private UserClient userClient;

    @Autowired
    public UserService(UserRepo userRepo){
        this.userClient = HttpServiceProxy.create(UserClient.class);
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

    public ClientDto httpSimple() {
        ResponseEntity<ClientDto> response = userClient.httpSimple();
        return response.getBody();
    }

    public List<CommentDto> httpComments(Integer postId) {
        ResponseEntity<List<CommentDto>> response = userClient.httpList(postId);
        List<CommentDto> list = response.getBody();
//        List<CommentDto> list = userClient.httpList1(postId);
        return list;
    }
}
