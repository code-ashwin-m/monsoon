package org.monsoon.example.contollers;

import org.monsoon.example.ApiResponse;
import org.monsoon.example.Orchestrator;
import org.monsoon.example.dto.UserDto;
import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.web.annotations.*;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private Orchestrator orchestrator;

    @RequestMapping(path = "/users", method = "POST")
    public ApiResponse create(@RequestBody UserDto userDto) {
        orchestrator.createUser(userDto);
        return ApiResponse.success(null);
    }

    @RequestMapping(path = "/users", method = "GET")
    public ApiResponse findAll() {
        List<UserDto> users = orchestrator.findAll();
        return ApiResponse.success(users);
    }
}
