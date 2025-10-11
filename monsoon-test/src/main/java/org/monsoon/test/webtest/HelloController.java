package org.monsoon.test.webtest;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.context.ApplicationContext;
import org.monsoon.framework.web.annotations.*;

@RestController
public class HelloController {
    private ApplicationContext context = Monsoon.getInstance().getContext();
    @RequestMapping(path = "/hello/{name}", method = "GET")
    public String hello(@PathVariable("name") String name, @RequestParam("test") String test) {
        System.out.println(context);
        return "Hello, " + name + " -> " + test;
    }

    @RequestMapping(path = "/user", method = "POST")
    public UserDto createUser(@RequestBody UserDto user) {
        user.setName(user.getName().toUpperCase());
        return user;
    }
}
