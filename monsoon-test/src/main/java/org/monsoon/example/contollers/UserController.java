package org.monsoon.example.contollers;

import org.monsoon.example.ApiResponse;
import org.monsoon.example.Orchestrator;
import org.monsoon.example.dto.ClientDto;
import org.monsoon.example.dto.CommentDto;
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

    @RequestMapping(path = "/test", method = "GET")
    public ApiResponse httpSimple() {
        ClientDto result = orchestrator.httpSimple();
        return ApiResponse.success(result);
    }

    @RequestMapping(path = "/comments", method = "GET")
    public ApiResponse httpComments(@RequestParam("postId") Integer postId) {
        List<CommentDto> result = orchestrator.httpComments(postId);
        return ApiResponse.success(result);
    }

    @RequestMapping(path = "/comments/{postId}", method = "GET")
    public ApiResponse httpComments1(@PathVariable("postId") Integer postId) {
        List<CommentDto> result = orchestrator.httpComments(postId);
        return ApiResponse.success(result);
    }
}
