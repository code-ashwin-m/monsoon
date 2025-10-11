package org.monsoon.test.webtest;

import org.monsoon.framework.web.annotations.*;

@RestController
public class HelloController {

    @RequestMapping(path = "/hello/{name}", method = "GET")
    public String hello(@PathVariable("name") String name) {
        return "Hello, " + name;
    }

    @RequestMapping(path = "/user", method = "POST")
    public UserDto createUser(@RequestBody UserDto user) {
        user.setName(user.getName().toUpperCase());
        return user;
    }
}
