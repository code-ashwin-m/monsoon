package org.monsoon.example;

import org.monsoon.example.dto.ClientDto;
import org.monsoon.example.dto.CommentDto;
import org.monsoon.example.dto.UserDto;
import org.monsoon.example.services.UserService;
import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Controller;
import org.monsoon.framework.core.annotations.Singleton;

import java.util.List;

@Controller
@Singleton
public class Orchestrator {
    @Autowired
    private UserService service;

    public Boolean createUser(UserDto userDto) {
        return service.createUser(userDto);
    }

    public List<UserDto> findAll() {
        return service.findAll();
    }

    public ClientDto httpSimple() {
        return service.httpSimple();
    }

    public List<CommentDto> httpComments(Integer postId) {
        return service.httpComments(postId);
    }
}
