package org.monsoon.example.http;

import org.monsoon.example.dto.ClientDto;
import org.monsoon.example.dto.CommentDto;
import org.monsoon.framework.web.annotations.*;

import java.util.List;

@HttpService( baseUrl = "https://jsonplaceholder.typicode.com")
public interface UserClient {
    @RequestMapping(path = "/todos/1", method = "GET")
    ClientDto httpSimple();

    @RequestMapping(path = "/comments", method = "GET")
    List<CommentDto> httpList(@QueryParam("postId") Integer postId);
}
